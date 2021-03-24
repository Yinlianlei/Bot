package bot.mirai;

import java.io.*;
import java.util.*;
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

class BotMysql{
    String url = "jdbc:mysql://127.0.0.1:3306/test";
    //var driver: String = ""
    String user = "Yinlianlei";
    String password = "1114561520";
    Connection conn = null;
    BotMysql(){
        try{
            conn = DriverManager.getConnection(url,user,password);
        }catch(SQLException e){
            e.printStackTrace();
        }
    };

    MessageChain playerMsg(Long id){
        MessageChain re = new PlainText("").plus(new PlainText(""));
        try {
            String sql =  "select * from player where `id` = "+id;
            Statement statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()) {
                // 选择Name这列数据
                String playerId = ((rs.getString("id")));
                String playerNick = ((rs.getString("nick")));
                re = re.plus(playerId).plus("-").plus(playerNick);
            }
            rs.close();
            statement.close();
        }
        catch(SQLException e) {
            e.printStackTrace();
        }finally{
            return re;
        }
    }
}

class BotMirai{
    private BotMysql sql;
    private Listener listenerFriend;
    private Listener listenerGroup;
    private Listener listenerStranger;
    private Listener listenerStranger2;
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
            String msg = event.getMessage().contentToString();
            if(msg.contains("?")){
                event.getSubject().sendMessage("Hello");
            }else if(msg.contains("ljz")){
                event.getSubject().sendMessage("屑");
            }
        });
        listenerFriend = GlobalEventChannel.INSTANCE.subscribeAlways(FriendMessageEvent.class, event -> {
            String msg = event.getMessage().contentToString();
            Friend fri = event.getFriend();
            if(msg.contains("ljz")){
                event.getSubject().sendMessage("屑");
            }else if(msg.contains("data")){
                event.getSubject().sendMessage(sql.playerMsg(fri.getId()));
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
            Vector.listen();
        }catch(Exception e){
            Vector.stop();
        }
        
    }
}

