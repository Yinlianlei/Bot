package bot.mirai;

import java.io.*;
import java.util.ArrayList;
//import java.text.SimpleDateFormat;
import java.sql.*;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.events.BotEvent;
import net.mamoe.mirai.event.events.NewFriendRequestEvent;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.event.events.StrangerMessageEvent;
import net.mamoe.mirai.event.events.TempMessageEvent;
import net.mamoe.mirai.event.Listener;

import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.Message;

import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.contact.Group;

import com.alibaba.fastjson.JSONArray;


class BotMirai{
    private BotMysql sql;
    private Listener listenerFriend;
    private Listener listenerGroup;
    private Listener listenerStranger;
    private Bot bot;
    BotMirai(){
        bot = BotFactory.INSTANCE.newBot(2683380854L, "60746877hun", new BotConfiguration() {{
        // 配置，例如：
        fileBasedDeviceInfo();
        setWorkingDir(new File("./Data"));
        setCacheDir(new File(""));
        fileBasedDeviceInfo("myDeviceInfo.json");
        noNetworkLog();
        //noBotLog()
        setProtocol(MiraiProtocol.ANDROID_PAD);
        }});
        bot.login();

        sql = new BotMysql();
    };
    void listen()throws Exception {
        listenerGroup = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, event -> {
            String msg = event.getMessage().serializeToMiraiCode();
            //if(msg.contains("mirai:at:")){//Get QQ id
                //System.out.println(msg.split("mirai:at:")[1].split("]")[0]);
            //}
            System.out.println(msg);
            //System.out.println(event.getMessage().contentToString());
            Group group = event.getSubject();
            if(msg.charAt(0) == '/'){
                if(msg.contains("task")){
                    sql.Bot_switch(msg,event);
                }else if(msg.contains("sub")){
                    sql.Bot_switch(msg,event);
                }else if(msg.contains("菜单")){
                    group.sendMessage(
                    "命令格式: \n"+
                    "/交易 @玩家 物品 数量 [和某位玩家进行交易]\n"+
                    "/技能 @玩家 技能名称 [给某位玩家加Buff]\n"+
                    "/攻击 @玩家 [攻击某位玩家]\n"+
                    "/论道 @玩家 [和某位玩家论道]"
                    );
                }else if(msg.contains("/交易")){
                    group.sendMessage("未开放");
                }else if(msg.contains("/技能")){
                    group.sendMessage("未开放");
                }else if(msg.contains("/攻击")){
                    group.sendMessage("未开放");
                }else if(msg.contains("/论道")){
                    group.sendMessage("未开放");
                }
                
            }
        });
        listenerFriend = GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, event -> {
            String msg = event.getMessage().contentToString();
            //event.getSubject().sendMessage(String.valueOf(event.getTime()));
            //Friend fri = event.getFriend();
            User friend = event.getSubject();
            if(msg.charAt(0) == '/'){
                if(msg.contains("task")){
                    sql.Bot_switch(msg,event);
                }else if(msg.contains("sub")){
                    sql.Bot_switch(msg,event);
                }else if(msg.contains("菜单")){
                    friend.sendMessage(
                    "命令格式: \n"+
                    "/交易 @玩家 物品 数量 [和某位玩家进行交易]\n"+
                    "/技能 @玩家 技能名称 [给某位玩家加Buff]\n"+
                    "/攻击 @玩家 [攻击某位玩家]\n"+
                    "/论道 @玩家 [和某位玩家论道]"
                    );
                }else if(msg.contains("/交易")){
                    friend.sendMessage("未开放");
                }else if(msg.contains("/技能")){
                    friend.sendMessage("未开放");
                }else if(msg.contains("/攻击")){
                    friend.sendMessage("未开放");
                }else if(msg.contains("/论道")){
                    friend.sendMessage("未开放");
                }
                
            }
        });
        listenerStranger = GlobalEventChannel.INSTANCE.subscribeAlways(NewFriendRequestEvent.class, event -> {
            event.accept();
        });
    };
    void stop(){
        listenerGroup.complete();
        listenerFriend.complete();
        listenerStranger.complete();
    }
}

public class App {
    public static void main(String[] args) {
        BotMirai Vector = new BotMirai();
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            //Vector.listen();
            //BotNet net = new BotNet("https://api.github.com/repos/yinlianlei/Bot/commits");
            
            //System.out.println(net.GetURL());
        }catch(Exception e){
            Vector.stop();
        }
        return;
    }
}

/*
//BotJson js = new BotJson("{\"233\":\"666\"}");
            //System.out.println(js.getString());
            //System.out.println(js.getJson().get("233"));
            //net.init("https://api.github.com/repos/yinlianlei/Bot/commits");
            //String[] in = {"Yinlianlei","repo"};
            //Date Time = sql.github_getLastUpdate(in);
            //BotJson tm = new BotJson();
            //System.out.println(tm.ret());
            
            //System.out.println(net.getLastUpdate(""));
            //System.out.println(Time);

            //net.init("https://api.github.com/repos/yinlianlei/Bot/commits");
            //String in = "2021-06-01T14:30:30Z";
            //System.out.println(net.getLastUpdate(in));
            //BotJson tm = new BotJson();
            //System.out.println(tm.ret());

             BotNet net = new BotNet();
        BotMysql sql = new BotMysql();
*/