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
public class Slack {

    @Value("${slack.baseUrl}")
    private String baseUrl;
    @Value("${slack.ticket}")
    private String ticket;


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json; ");
        return headers;
    }

    public void sendMsg(String msg) {
        String url = "https://hooks.slack.com/services/"+ticket;
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        param.put("text", msg);
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        try{
            restTemplate.exchange(url, HttpMethod.POST, httpEntity, Object.class);
        }catch (Exception e){
        }
    }

}
