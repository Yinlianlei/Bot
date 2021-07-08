package bot.mirai;

import java.net.HttpURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

public class BotNet {
    URL targetUrl = null;
    InputStream in = null;
    String target = null;
    JSONObject jsonBot = null;

    BotNet() {
    };

    BotNet(String target) {
        // botNet tar = new botNet("https://api.github.com/repos/yinlianlei/Work");
        this.target = target;
    }

    void init(String in){
        target = in;
    }

    JSONObject getLastUpdate(String time){
        target += "?since="+time;
        GetURL();
        return jsonBot;
    }

    void GetURL() {// GET 方式进行操作
        try {
            // System.out.println("Init net System");
            targetUrl = new URL(target);
            // 打开URL之间的连接
            HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();// 使用HttpURLConnection实现
            // 设置请求模式
            connection.setRequestMethod("GET");
            // 设置超时
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(10000);// 单位毫秒

            // 进行连接
            connection.connect();

            int code = connection.getResponseCode();
            // System.out.println("Ready to read"+String.valueOf(code));
            if (code == 200) {
                in = connection.getInputStream();// 设置输出流
                while (in.available() != 0) {
                    String tmp = new String(in.readAllBytes());
                    tmp = tmp.substring(1,tmp.length()-1);
                    //System.out.println(tmp);
                    jsonBot = JSON.parseObject(tmp);// 对目标返回json格式进行输出
                }
                //System.out.println(jsonBot.jsonObj);
            } else {
                System.out.println("ERROR");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void close() {
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

/*
        //jsonBot = (JSONObject)(jsonBot.get("commit"));
        //System.out.println(jsonBot.toJSONString());
        //jsonBot = (JSONObject)(jsonBot.get("committer"));
        //System.out.println(jsonBot.toJSONString());
        //((String)jsonBot.get("date"));
*/