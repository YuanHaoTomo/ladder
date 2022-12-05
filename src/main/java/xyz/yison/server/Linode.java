package xyz.yison.server;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import xyz.yison.pojo.ImageVo;
import xyz.yison.pojo.LinodeTypeVo;
import xyz.yison.pojo.LinodeVo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Linode {

    @Value("${lonode.baseUrl}")
    private String baseUrl;
    @Value("${lonode.ticket}")
    private String ticket;
    @Value("${lonode.region}")
    private String region;
    @Value("${lonode.root_pass}")
    private String root_pass;


    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + ticket);
        return headers;
    }

    public List<LinodeVo> getVpsList() {
        String url = baseUrl+"/linode/instances";
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        List<LinodeVo> linodeVoList= responseEntity.getBody().getList("data", LinodeVo.class);
        return linodeVoList;
    }

    public LinodeVo getVpsById(String id) {
        String url = baseUrl+"/linode/instances/"+id;
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        LinodeVo linodeVo= responseEntity.getBody().toJavaObject(LinodeVo.class);
        return linodeVo;
    }


    public List<ImageVo> getImageList() {
        String url = baseUrl+"/images";
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        List<ImageVo> linodeVoList= responseEntity.getBody().getList("data", ImageVo.class);
        return linodeVoList;
    }

    public ImageVo getRecentTimePrivateImage() {
        List<ImageVo> imageVos= getImageList();
        ImageVo imageVoResult=null;
        for (ImageVo imageVo: imageVos) {
            if(!imageVo.getId().startsWith("private") || !imageVo.getType().equals("manual")){
                continue;
            }
            if(imageVoResult==null){
                imageVoResult =  imageVo;
                continue;
            }
            if(imageVo.getCreated().after(imageVoResult.getCreated())){
                imageVoResult =  imageVo;
                continue;
            }
        }
        if(imageVoResult!=null){
            return imageVoResult;
        }
        return new ImageVo();
    }

    public LinodeTypeVo getCheapestLinodeType() {
        String url = baseUrl+"/linode/types";
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        List<LinodeTypeVo> linodeTypeVos= responseEntity.getBody().getList("data", LinodeTypeVo.class);
        LinodeTypeVo linodeTypeResult=null;
        for (LinodeTypeVo linodeTypeVo: linodeTypeVos) {
            if(linodeTypeResult==null){
                linodeTypeResult =  linodeTypeVo;
                continue;
            }
            if(linodeTypeVo.getPrice().get("monthly")<linodeTypeResult.getPrice().get("monthly")){
                linodeTypeResult =  linodeTypeVo;
                continue;
            }
        }
        if(linodeTypeResult!=null){
            return linodeTypeResult;
        }
        return new LinodeTypeVo();
    }

    public LinodeVo createVps(String imageId, String linodeTypeId) {
        String url = baseUrl+"/linode/instances";
        HttpHeaders headers = getHttpHeaders();
        LocalDateTime localDateTime=LocalDateTime.now();
        String label= "auto-"+localDateTime.getYear()+"-"+localDateTime.getMonthValue()+"-"+localDateTime.getDayOfMonth()+"-"+localDateTime.getHour()+"_"+localDateTime.getMinute();
        HashMap<String, Object> param=new HashMap<>();
        param.put("image", imageId);
        param.put("type", linodeTypeId);
        param.put("label", label);
        param.put("region", region);
        param.put("root_pass", root_pass);
        HashMap<String, String> interfacesParam=new HashMap<>();
        interfacesParam.put("purpose", "public");
//        param.put("interfaces", Arrays.asList(interfacesParam));
        HttpEntity httpEntity = new HttpEntity(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
        LinodeVo linodeVo= responseEntity.getBody().toJavaObject(LinodeVo.class);
        return linodeVo;

    }

    public void getRegionsList() {
        String url = baseUrl+"/regions";
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.GET, httpEntity, JSONObject.class);
        List<ImageVo> linodeVoList= responseEntity.getBody().getList("data", ImageVo.class);
    }

    public void deleteVps(String excludeVpsId) {
        List<LinodeVo> linodeVoList= getVpsList();
        for (LinodeVo linode: linodeVoList) {
            if(!linode.getLabel().startsWith("auto")){
                continue;
            }
            if(!excludeVpsId.equals(linode.getId())){
                deleteVpsById(linode.getId());
            }
        }
    }


    private void deleteVpsById(String id) {
        String url = baseUrl+"/linode/instances/"+id;
        HttpHeaders headers = getHttpHeaders();
        HashMap<String, String> param=new HashMap<>();
        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(param, headers);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<JSONObject> responseEntity = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, JSONObject.class);
    }

    @Retryable(value = Exception.class, maxAttempts = 10, backoff = @Backoff(delay = 60000) )
	public boolean newVpsIsRunning(String id) throws Exception {
    	LinodeVo linodeVo=getVpsById(id);
    	if(linodeVo.getStatus()==null || !linodeVo.getStatus().equals("running")){
    		throw new Exception("新的实例尚未启动完成");
		}
    	return true;
	}
}
