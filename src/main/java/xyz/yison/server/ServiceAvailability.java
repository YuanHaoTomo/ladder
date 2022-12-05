package xyz.yison.server;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.yison.pojo.DnsVo;
import xyz.yison.pojo.ImageVo;
import xyz.yison.pojo.LinodeTypeVo;
import xyz.yison.pojo.LinodeVo;

@Component
public class ServiceAvailability implements CommandLineRunner {

	@Autowired
	private Dns dns;
	@Autowired
	private Domian domian;
	@Autowired
	private Linode linode;
	@Autowired
	private Slack slack;

    @Value("${cnDomianPing}")
    private String cnDomianPing;

    @Value("${sSDomainPing}")
    private String sSDomainPing;

    @Value("${detectionOfConnectivityTimeIntervalSec}")
    private Integer detectionOfConnectivityTimeIntervalSec;


    @Override
    public void run(String... args) {
    	slack.sendMsg(String.format("检测服务启动完成，开始持续检测梯子服务状态..."));
		while (true){
			try {
				Thread.sleep(detectionOfConnectivityTimeIntervalSec*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			job();
		}
	}

	@SneakyThrows
	private void job(){
		if(domian.isConnection(sSDomainPing)){
			return;
		}
		if(!domian.isConnection(cnDomianPing)){
			return;
		}
		slack.sendMsg(String.format("检测到%s域名无法ping通，开始准备创建新实例...", sSDomainPing));
		LinodeTypeVo linodeTypeVo=linode.getCheapestLinodeType();
		ImageVo imageVo=linode.getRecentTimePrivateImage();
		LinodeVo newLinode=linode.createVps(imageVo.getId(), linodeTypeVo.getId());
		slack.sendMsg(String.format("新实例创建完成，ip：%s,等待实例启动完成...", newLinode.getIpv4().get(0)));
		if(linode.newVpsIsRunning(newLinode.getId())){
			if(domian.IsConnectionRetry(newLinode.getIpv4().get(0))){
				DnsVo dnsVo=dns.getDnsInfo(sSDomainPing);
				dns.updateDnsRecord(dnsVo.getZone_id(), dnsVo.getId(), newLinode.getIpv4().get(0));
				slack.sendMsg(String.format("实例启动完成，已经将域名%s解析到新的IP：%s", sSDomainPing, newLinode.getIpv4().get(0)));

				linode.deleteVps(newLinode.getId());
				slack.sendMsg(String.format("删除了多余的自动创建的linode实例"));
			}else{
				slack.sendMsg(String.format("新实例ip：%s一直无法ping通，等待再次尝试创建新实例...", newLinode.getIpv4().get(0)));
				job();
			}


		}
	}



}
