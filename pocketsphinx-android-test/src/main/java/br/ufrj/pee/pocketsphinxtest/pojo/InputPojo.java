package br.ufrj.pee.pocketsphinxtest.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by limazix on 04/05/15.
 */
public class InputPojo {

    @JsonProperty("inputs")
    private Map<String, String> inputMap;

    public Map<String, String> getInputMap() {
        return inputMap;
    }

    public void setInputMap(Map<String, String> map) {
        this.inputMap = map;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("map={");

        for(Map.Entry<String, String> entry : this.inputMap.entrySet()) {
            sb.append("" + entry.getKey() + "=" + entry.getValue());
        }

        sb.append("}");

        return sb.toString();
    }
}
