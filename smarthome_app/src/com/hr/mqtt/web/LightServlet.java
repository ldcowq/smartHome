package com.hr.mqtt.web;

import com.hr.mqtt.dao.impl.SaveDataDao;
import net.sf.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


/**
 * @author LDC
 * @create 2020-12-21 21:14
 */
public class LightServlet extends HttpServlet {
    private SaveDataDao dao = new SaveDataDao();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String light = req.getParameter("light");
        dao.addLight(light);
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
        String light = (String) t.get("light");
        dao.addLight(light);
    }
}
