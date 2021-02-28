package com.hr.mqtt.web;

/**
 * @author LDC
 * @create 2020-12-24 23:55
 */
public class Mq2 {
    private int id;
    private String mq2;

    @Override
    public String toString() {
        return "Mq2{" +
                "id=" + id +
                ", mq2='" + mq2 + '\'' +
                '}';
    }

    public Mq2() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMq2() {
        return mq2;
    }

    public void setMq2(String mq2) {
        this.mq2 = mq2;
    }

    public Mq2(int id, String mq2) {
        this.id = id;
        this.mq2 = mq2;
    }
}
