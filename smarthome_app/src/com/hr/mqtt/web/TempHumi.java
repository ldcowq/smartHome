package com.hr.mqtt.web;

/**
 * @author LDC
 * @create 2020-12-24 21:59
 */
public class TempHumi {
    private int id;
    private String temp;
    private String humi;

    public TempHumi(int id, String temp, String humi) {
        this.id = id;
        this.temp = temp;
        this.humi = humi;
    }

    public TempHumi() {
    }

    @Override
    public String toString() {
        return "TempHumi{" +
                "id=" + id +
                ", temp='" + temp + '\'' +
                ", humi='" + humi + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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
}
