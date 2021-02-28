package com.hr.smarthome.ui.light;

public class LightJavaBean {
    private String light;

    @Override
    public String toString() {
        return "LightJavaBean{" +
                "light='" + light + '\'' +
                '}';
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public LightJavaBean() {
    }

    public LightJavaBean(String light) {
        this.light = light;
    }
}
