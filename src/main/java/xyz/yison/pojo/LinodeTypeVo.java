package xyz.yison.pojo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class LinodeTypeVo {

    private String id;
    private Map<String, Integer> price;
    private Integer memory;

    public Map<String, Integer> getPrice() {
        return price;
    }

    public void setPrice(Map<String, Integer> price) {
        this.price = price;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }
}
