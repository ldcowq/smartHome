package com.hr.smarthome.ui.mq2;

public class Mq2JavaBean {
    public Mq2JavaBean(String mq2) {
        this.mq2 = mq2;
    }

    public Mq2JavaBean() {
    }

    @Override
    public String toString() {
        return "Mq2JavaBean{" +
                "mq2='" + mq2 + '\'' +
                '}';
    }

    public String getMq2() {
        return mq2;
    }

    public void setMq2(String mq2) {
        this.mq2 = mq2;
    }

    private String mq2;

}
