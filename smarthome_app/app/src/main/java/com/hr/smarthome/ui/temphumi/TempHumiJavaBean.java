package com.hr.smarthome.ui.temphumi;

public class TempHumiJavaBean {
    private String temp;
    private String humi;

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }

    public String getHumi() {
        return humi;
    }

    public void setHumi(String humi) {
        this.humi = humi;
    }

    public TempHumiJavaBean() {

    }

    public TempHumiJavaBean(String temp, String humi) {
        this.temp = temp;
        this.humi = humi;
    }
}
