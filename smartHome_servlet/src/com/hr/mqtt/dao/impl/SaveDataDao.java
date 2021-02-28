package com.hr.mqtt.dao.impl;

/**
 * @author LDC
 * @create 2020-12-21 23:16
 */
public class SaveDataDao extends BaseDao {
    public int addMq2(String mq2) {
        String sql = "insert into mq2_tb(mq2) values(?)";
        return update(sql,mq2);
    }

    public int addLight(String light) {
        String sql = "insert into light_tb(light) values(?)";
        return update(sql,light);
    }

    public int addTempHumi(String temp,String humi) {
        String sql = "insert into temphumi_tb(temp,humi) values(?,?)";
        return update(sql,temp,humi);
    }
}
