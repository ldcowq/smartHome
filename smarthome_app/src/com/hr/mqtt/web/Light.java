package com.hr.mqtt.web;

/**
 * @author LDC
 * @create 2020-12-24 23:55
 */
public class Light {
    private int id;
    private String light;

    @Override
    public String toString() {
        return "Light{" +
                "id=" + id +
                ", light='" + light + '\'' +
                '}';
    }

    public Light() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLight() {
        return light;
    }

    public void setLight(String light) {
        this.light = light;
    }

    public Light(int id, String light) {
        this.id = id;
        this.light = light;
    }
}
