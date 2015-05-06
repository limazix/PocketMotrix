package br.ufrj.pee.pocketsphinxtest.pojo;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by limazix on 04/05/15.
 */
public class ScenarioPojo {

    @JsonProperty("scenario")
    private List<InputPojo> scenario;

    public ScenarioPojo() {
    }

    public List<InputPojo> getScenario() {
        return scenario;
    }

    public void setScenario(List<InputPojo> scenario) {
        this.scenario = scenario;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("{");

        for(InputPojo input : scenario) {
            sb.append(input.toString());
            sb.append(",");
        }

        sb.append("}");

        return sb.toString();
    }
}
