package com.hr.mqtt.dao.impl;

import com.hr.mqtt.web.Light;
import com.hr.mqtt.web.Mq2;
import com.hr.mqtt.web.TempHumi;

import java.util.List;

/**
 * @author LDC
 * @create 2020-12-21 23:16
 */
public class QueryDataDao extends BaseDao {
    public List<Mq2> queryMq2(String pagers) {
        int i = Integer.parseInt(pagers) - 1;
        String sql = "select * from mq2_tb order by id desc limit ?,?";
        return queryForList(Mq2.class,sql,i*24,24);
    }

    public List<Light> queryLight(String pagers) {
        int i = Integer.parseInt(pagers) - 1;
        String sql = "select * from light_tb order by id desc limit ?,?";
        return queryForList(Light.class,sql,i*24,24);
    }

    public List<TempHumi> queryTempHumi(String pagers) {
        int i = Integer.parseInt(pagers) - 1;
        String sql = "select * from temphumi_tb order by id desc limit ?,?";
        return queryForList(TempHumi.class,sql,i*24,24);
    }
}
