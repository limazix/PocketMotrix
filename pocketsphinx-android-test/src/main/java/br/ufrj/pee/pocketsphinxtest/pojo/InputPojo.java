package br.ufrj.pee.pocketsphinxtest.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Created by limazix on 04/05/15.
 */
public class InputPojo {

    @JsonProperty("floatInputs")
    private Map<String, Float> floatInputMap;

    @JsonProperty("integerInputs")
    private Map<String, Integer> integerInputMap;

    @JsonProperty("booleanInputs")
    private Map<String, Boolean> booleanInputMap;

    @JsonProperty("stringInputs")
    private Map<String, String> stringInputMap;

    public Map<String, String> getStringInputMap() {
        return stringInputMap;
    }

    public void setStringInputMap(Map<String, String> map) {
        this.stringInputMap = map;
    }

    public Map<String, Integer> getIntegerInputMap() {
        return integerInputMap;
    }

    public void setIntegerInputMap(Map<String, Integer> intergerInputMap) {
        this.integerInputMap = intergerInputMap;
    }

    public Map<String, Boolean> getBooleanInputMap() {
        return booleanInputMap;
    }

    public void setBooleanInputMap(Map<String, Boolean> booleanInputMap) {
        this.booleanInputMap = booleanInputMap;
    }

    public Map<String, Float> getFloatInputMap() {
        return floatInputMap;
    }

    public void setFloatInputMap(Map<String, Float> floatInputMap) {
        this.floatInputMap = floatInputMap;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("map={");

        for(Map.Entry<String, String> entry : this.stringInputMap.entrySet()) {
            sb.append("" + entry.getKey() + "=" + entry.getValue());
        }

        sb.append("}");

        return sb.toString();
    }
}
