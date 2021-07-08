package bot.mirai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.sql.*;

import java.util.ArrayList;

import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.Message;

import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.contact.User;

public class BotMysql {
    private final String url = "jdbc:mysql://47.102.215.193/JavaData?useSSL=true&characterEncoding=utf8";//若不设置encodoing则会导致输出为非中文字符
    private final String user = "huawei";
    private final String password = "huawei";
    private Connection conn = null;
    private BotNet net = null;
    private ArrayList subweb = null;

    BotMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);// mysql连接
            net = new BotNet();
            subweb = new ArrayList();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_swich(String args,AbstractMessageEvent event){
        String[] in = args.split(" ");
        switch(in[0]){
            case "/task":switch(in[1]){
                case "add":task_add(in,event);break;
                case "del":task_del(in,event);break;
                case "show":task_show(in,event);break;
                case "send":task_send(in,event);break;
                case "comp":task_comp(in,event);break;
                case "help":task_help(event);;break;
                case "showComp":task_showComp(in,event);break;
                default:
                    System.out.println("输入参数错误，请查看task help");
            };break;
            case "/git":switch(in[1]){
                case "init":git_init(in,event);break;
                default:
                    System.out.println("输入参数错误，请查看git help")
            }
            default:
                System.out.println("输入参数错误，请参考help");
        }
    }

    void task_add(String[] task,AbstractMessageEvent event) {// 添加任务
        User sender = event.getSender();
        try {
            if (task.length != 4){//判断如果输入参数不等于4便返回错误信息
                sender.sendMessage("ERROR:输入参数过少或过多");
                return;
            }
            String targetID = String.valueOf(sender.getId()), targetNick = sender.getNick(),targetFrom = String.valueOf(sender.getId());
            String targetGroup = "";
            if(event instanceof GroupMessageEvent){
                GroupMessageEvent events = (GroupMessageEvent)event;
                targetGroup = String.valueOf(events.getGroup().getId());
            }
            Statement stmt = conn.createStatement();
            String sql = "insert into person_task values (" + "0" + ",'" + // 编号
                    targetID + "','" + // qq号
                    targetNick + "','" + // 昵称
                    targetGroup + "','" + // 发布任务群号
                    targetFrom + "','"+
                    task[2] + "','" + // 任务名
                    task[3] + // 备注
                    "',0)";
            // System.out.println(sql);//调试用
            stmt.execute(sql);
            stmt.close();
            event.getSender().sendMessage("task add success");
        } catch (Exception e) {
            event.getSender().sendMessage("task add faild");
            e.printStackTrace();
        }
    }

    void task_help(AbstractMessageEvent event){//显示帮助
        event.getSender().sendMessage("task任务添加帮助：\n"+
        "task [option] <args...>\n"+
        "option:\n\t"+
        "add [任务] <备注(可选)> --任务添加\n\t"+
        "comp [任务ID] --任务完成\n\t"+
        "del [任务ID] --任务删除\n\t"+
        "send [QQ号] [任务] <备注(可选)> --发送任务给他人\n"
        );
    }

    void task_comp(String[] task,AbstractMessageEvent event){//完成任务
        try {
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                event.getSender().sendMessage("ERROR:输入参数过少或过多");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "update person_task set mark = 1 where num ="+targetID;
            // System.out.println(sql);//调试用
            stmt.execute(sql);
            stmt.close();
            event.getSender().sendMessage("task complete success");
        } catch (Exception e) {
            event.getSender().sendMessage("task complete faild");
            e.printStackTrace();
        }
    }

    void task_del(String[] task,AbstractMessageEvent event){//删除目标指定编号的任务
        try {
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                event.getSender().sendMessage("ERROR:输入参数过少或过多");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "delete from person_task where num = "+targetID;
            // System.out.println(sql);//调试用
            stmt.execute(sql);
            stmt.close();
            event.getSender().sendMessage("task delete success");
        } catch (Exception e) {
            event.getSender().sendMessage("task delete faild");
            e.printStackTrace();
        }
    }

    void task_show(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
        User sender = event.getSender();
        try {
            if(task.length < 3 || task.length > 4){
                sender.sendMessage("ERROR 输入参数过少或过多");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "select * from person_task where id = '"+targetID+"' and `mark` = 0";
            ResultSet R = stmt.executeQuery(sql);
            String to = new String();//new String("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark\n");
            while(R.next()){
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\t"+R.getString("mark")+"\n");
            }
            if(task.length == 4 && task[3].equals("public")){
                GroupMessageEvent events = (GroupMessageEvent)event;
                events.getGroup().sendMessage(to);
            }else if(task.length == 3 || (task.length == 4 && task[3].equals("private"))){
                sender.sendMessage(to);
            }
            stmt.close();
            System.out.println("task show success");
        } catch (Exception e) {
            System.out.println("task delete faild");
            e.printStackTrace();
        }
    }

    void task_showComp(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
        User sender = event.getSender();
        try {
            if(task.length < 3 || task.length > 4){
                sender.sendMessage("ERROR 输入参数过少或过多");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "select * from person_task where id = '"+targetID+"' and `mark` = 1";
            ResultSet R = stmt.executeQuery(sql);
            String to = new String();//new String("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark\n");
            while(R.next()){
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\t"+R.getString("mark")+"\n");
            }
            if(task.length == 4 && task[3].equals("public")){
                GroupMessageEvent events = (GroupMessageEvent)event;
                events.getGroup().sendMessage(to);
            }else if(task.length == 3 || (task.length == 4 && task[3].equals("private"))){
                sender.sendMessage(to);
            }
            stmt.close();
            System.out.println("task show success");
        } catch (Exception e) {
            System.out.println("task delete faild");
            e.printStackTrace();
        }
    }

    void task_send(String[] task,AbstractMessageEvent event){
        User sender = event.getSender();
        if(event instanceof FriendMessageEvent){
            sender.sendMessage("Group only");
            return;
        }
        try {
            if(task.length < 4 || task.length > 5){
                sender.sendMessage("输入参数过少或过多");
                return;
            }
            GroupMessageEvent events = (GroupMessageEvent)event;
            String targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0], 
            targetNick = "", targetGroup = "",targetNote="",targetFrom = "";
            if(event instanceof GroupMessageEvent){
                targetNick = events.getGroup().getOrFail(Long.valueOf(targetID)).getNick();
                targetGroup = String.valueOf(events.getGroup().getId());
                targetFrom = String.valueOf(sender.getId());
            }
            
            if(task.length == 5){
                targetNote = task[4];
            }
            
            Statement stmt = conn.createStatement();
            String sql = "insert into person_task values (0,'" + // 编号
                    targetID + "','" + // qq号
                    targetNick + "','" + // 昵称
                    targetGroup + "','" + // 发布任务群号
                    targetFrom + "','"+
                    task[3] + "','" + // 任务名
                    targetNote + // 备注
                    "',0)";
            stmt.execute(sql);
            stmt.close();
            sender.sendMessage("task send success");
        } catch (Exception e) {
            sender.sendMessage("task send faild");
            e.printStackTrace();
        }
    }

    void git_init(String[] in,AbstractMessageEvent event){
        if(in.length != 3){
            return;
        }
        try{
            net.init(in[2]);
            event.getGroup().sendMessage("git init complete");
            subweb.add(in[2]);
        }catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    Boolean git_insertLastUpdate(String[] in,JSONObject jsonObj){//add last update time to table github
        if(in.length != 4){
            return false;
        }
        try{
            Statement stmt = conn.createStatement();
            String sql = "insert into github values (0,'"+
                in[0]+"','"+
                in[1]+"','"+
                in[2]+"','"+
                in[3]+"','"+
                in[4]+"','"+
                "')";
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    String git_getLastUpdate(String[] git){//git last update time to table github
        if(git.length != 2){
            System.out.println("输入参数过少或过多");
            return null;
        }
        String lastUpdateTime = null;
        try{
            Statement stmt = conn.createStatement();
            String sql = "select * from github where `owner` = '"+git[0]+"' and `repo` = '"+git[1]+"'";
            ResultSet re = stmt.executeQuery(sql);
            while(re.next()){
                lastUpdateTime = re.getString("last_update");
            }
            re.close();
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return lastUpdateTime;
    }

    /*
    MessageChain playerMsg(Long id){
        MessageChain re = new PlainText("").plus(new PlainText(""));
        try {
            String sql =  "select * from playerInfo where `id` = "+id;
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
    */

    void close_connect() {// 关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

/*
class BotMysql{
    String url = "jdbc:mysql://127.0.0.1:3306/game";
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
*/