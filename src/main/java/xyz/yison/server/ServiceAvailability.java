package xyz.yison.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import xyz.yison.pojo.DnsVo;
import xyz.yison.pojo.ImageVo;
import xyz.yison.pojo.LinodeTypeVo;
import xyz.yison.pojo.LinodeVo;

@Slf4j
@Component
public class ServiceAvailability implements CommandLineRunner {

	@Autowired
	private Dns dns;
	@Autowired
	private Ping ping;
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
		log.info("程序启动完成，开始持续检测...");
		slack.sendMsg(String.format("检测服务启动完成，开始持续检测梯子服务状态..."));
		int count = 0;
		while (true){
			count++;
			log.info("第{}轮询检测开始...", count);
			try {
				deleteUnwantedVps();
				job();
				Thread.sleep(detectionOfConnectivityTimeIntervalSec*1000);
			} catch (Exception e) {
				log.error("发生了异常：", e);
			}
		}
	}

	/**
	 * 删除不需要的多余实例
	 */
	private void deleteUnwantedVps() {
		DnsVo dnsVo=dns.getDnsInfo(sSDomainPing);
		boolean flag=linode.deleteVpsExcludeIp(dnsVo.getContent());
		if(flag){
			log.info(String.format("删除了多余的自动创建的linode实例"));
			slack.sendMsg(String.format("删除了多余的自动创建的linode实例"));
		}
	}

	private void job() throws Exception {
    	DnsVo dnsVo=dns.getDnsInfo(sSDomainPing);
		if(ping.IsConnectionRetry(dnsVo.getContent())){
			return;
		}
		if(!ping.IsConnectionRetry(cnDomianPing)){
			return;
		}
		log.info(String.format("检测到%s域名无法ping通，开始准备创建新实例...", sSDomainPing));
		slack.sendMsg(String.format("检测到%s域名无法ping通，开始准备创建新实例...", sSDomainPing));
		LinodeTypeVo linodeTypeVo=linode.getCheapestLinodeType();
		ImageVo imageVo=linode.getRecentTimePrivateImage();
		LinodeVo newLinode=linode.createVps(imageVo.getId(), linodeTypeVo.getId());
		slack.sendMsg(String.format("新实例创建完成，ip：%s,等待实例启动完成...", newLinode.getIpv4().get(0)));
		log.info(String.format("新实例创建完成，ip：%s,等待实例启动完成...", newLinode.getIpv4().get(0)));
		if(linode.newVpsIsRunning(newLinode.getId())){
			if(ping.IsConnectionRetry(newLinode.getIpv4().get(0))){
				dns.updateDnsRecord(dnsVo.getZone_id(), dnsVo.getId(), newLinode.getIpv4().get(0));
				log.info(String.format("实例启动完成，已经将域名%s解析到新的IP：%s", sSDomainPing, newLinode.getIpv4().get(0)));
				slack.sendMsg(String.format("实例启动完成，已经将域名%s解析到新的IP：%s", sSDomainPing, newLinode.getIpv4().get(0)));
			}else{
				log.info(String.format("新实例ip：%s一直无法ping通，等待再次尝试创建新实例...", newLinode.getIpv4().get(0)));
				slack.sendMsg(String.format("新实例ip：%s一直无法ping通，等待再次尝试创建新实例...", newLinode.getIpv4().get(0)));
				job();
			}
		}
	}



}
