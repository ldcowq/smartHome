package com.hr.mqtt.web;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.hr.mqtt.dao.impl.QueryDataDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;

/**
 * @author LDC
 * @create 2020-12-24 20:02
 */

public class QueryTempHumiServlet extends HttpServlet {
    private QueryDataDao dao = new QueryDataDao();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pagers = request.getParameter("pagers");
        List<TempHumi> resultList = dao.queryTempHumi(pagers);
        PrintWriter writer = response.getWriter();
        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        for (TempHumi th : resultList) {
           buffer.append("{\"temp\":").append(th.getTemp()).append(",")
           .append("\"humi\":\"").append(th.getHumi()).append("\"},");
           }
        //去掉最后一个逗号
        buffer.deleteCharAt(buffer.length() - 1);
        buffer.append("]");
        writer.println(buffer);
    }
}
