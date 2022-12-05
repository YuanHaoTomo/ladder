package xyz.yison.server;

import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.yison.pojo.DnsVo;

import java.util.HashMap;
import java.util.List;

@Component
public class Dns {

    @Value("${cloudflare.baseUrl}")
    private String baseUrl;
    @Value("${cloudflare.ticket}")
    private String ticket;
    @Value("${cloudflare.zoneId}")
    private String zoneId;
    @Value("${cloudflare.dnsName}")
    private String dnsName;


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + ticket);
        headers.add("Content-Type", "application/json");
        return headers;
    }

    public JSONObject updateDnsRecord(String zoneId, String dnsId, String ip) {
        String url = baseUrl+"/zones/"+zoneId+"/dns_records/"+dnsId;
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        param.put("type", "A");
        param.put("content", ip);
        param.put("name", dnsName);
        param.put("ttl", "60");
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, JSONObject.class);
        return responseEntity.getBody();
    }

    public DnsVo getDnsInfo(String dnsName) {
        String url = baseUrl+"/zones/"+zoneId+"/dns_records?name="+dnsName;
        HttpHeaders headers = getHttpHeaders();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(null, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        List<DnsVo> dnsVoList= responseEntity.getBody().getList("result", DnsVo.class);
        for (DnsVo dnsVo: dnsVoList) {
            if(dnsVo.getName().equals(dnsName)){
                return dnsVo;
            }
        }
        return new DnsVo();
    }

}
