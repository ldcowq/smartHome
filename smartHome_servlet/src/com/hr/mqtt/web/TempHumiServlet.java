package com.hr.mqtt.web;

import com.hr.mqtt.dao.impl.SaveDataDao;
import net.sf.json.JSONObject;
import sun.net.www.protocol.https.HttpsURLConnectionImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;


/**
 * @author LDC
 * @create 2020-12-21 21:14
 */
public class TempHumiServlet extends HttpServlet {
    private SaveDataDao dao = new SaveDataDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String temp = req.getParameter("temp");
        String humi = req.getParameter("humi");
        dao.addTempHumi(temp, humi);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        String str = "";
        String wholeStr = "";
        while((str = reader.readLine()) != null){
            //一行一行的读取body体里面的内容；
            wholeStr += str;
        }
        JSONObject t= JSONObject.fromObject(wholeStr);
        String temp = (String) t.get("temp");
        String humi = (String) t.get("humi");
        dao.addTempHumi(temp, humi);
    }
}
