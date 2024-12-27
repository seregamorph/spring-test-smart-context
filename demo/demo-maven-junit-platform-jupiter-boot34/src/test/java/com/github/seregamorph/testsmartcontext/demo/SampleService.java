package com.github.seregamorph.testsmartcontext.demo;

public class SampleService {

    private final SampleBean sampleBean;

    public SampleService(SampleBean sampleBean) {
        this.sampleBean = sampleBean;
    }

    public String getValue() {
        return sampleBean.getValue();
    }
}
