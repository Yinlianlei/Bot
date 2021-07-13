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
import net.mamoe.mirai.contact.Group;

public class BotMysql {
    private final String url = "jdbc:mysql://47.102.215.193/JavaData?useSSL=true&characterEncoding=utf8";//若不设置encodoing则会导致输出为非中文字符
    private final String user = "huawei";
    private final String password = "huawei";
    private Connection conn = null;
    private BotNet net = null;
    private ArrayList subweb = null;
    private ArrayList subbilibili = null;
    private BotThread bt = null;

    BotMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);// mysql连接
            net = new BotNet();
            subweb = git_subweb();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void Bot_switch(String args,AbstractMessageEvent event){
        String[] in = args.split(" ");
        switch(in[0]){//command switch
            case "/task":switch(in[1]){//all test complete
                case "add":task_add(in,event);break;
                case "del":task_del(in,event);break;
                case "list":task_list(in,event);break;
                case "send":task_send(in,event);break;
                case "comp":task_comp(in,event);break;
                case "help":task_help(event);;break;
                case "listC":task_listC(in,event);break;
                default:{
                    System.out.println("输入参数错误，请查看task help");
                }
            };break;
            case "/sub":switch(in[1]){//group list
                case "git":switch(in[2]){
                    case "list":sub_list(event);break;
                    case "init":sub_git_init(in,event);break;
                    case "get":sub_git_getUpdate(in,event);break;
                    case "help":sub_git_help(event);break;
                    case "remove":sub_git_remove(in,event);break;
                    default:{
                        System.out.println("输入参数错误，请查看sub git help");
                    }
                };break;
                case "bili":switch(in[2]){
                    case "init":break;
                    default:{
                        System.out.println("输入参数错误，请查看sub bili help");
                    }
                };break;
                default:{
                    System.out.println("输入参数错误，请查看sub help");
                }
            }break;
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
        "send @[QQ号] [任务] <备注(可选)> --发送任务给他人\n"
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

    void task_list(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
        User sender = event.getSender();
        try {
            if(task.length < 3 || task.length > 4){
                sender.sendMessage("ERROR 输入参数过少或过多");
                return;
            }
            String targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0];
            Statement stmt = conn.createStatement();
            String sql = "select * from person_task where id = '"+targetID+"' and `mark` = 0";
            ResultSet R = stmt.executeQuery(sql);
            String to = new String("");//new String("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark\n");
            while(R.next()){
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\n");
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

    void task_listC(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
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
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\n");
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

    void task_send(String[] task,AbstractMessageEvent event){//send task to member
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

    void sub_git_list(AbstractMessageEvent event){
        if(event instanceof FriendMessageEvent){
            ((GroupMessageEvent)event).sendMessage("Group only");
            return;
        }
        try{
            Group group = ((GroupMessageEvent)event).getGroup();
            String msg = new String("list:");
            for(int i=0;i<subweb.size();i++){
                msg += "\n"+String.valueOf(i)+"-"+subweb.get(i);
            }
            group.sendMessage(msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_help(AbstractMessageEvent event){
        if(event instanceof FriendMessageEvent){
            ((GroupMessageEvent)event).sendMessage("Group only");
            return;
        }
        try{
            Group group = ((GroupMessageEvent)event).getGroup();
            String msg = new String("sub git help\n"+
            "sub git [option] <args>\n"+
            "option:\n"+
            "init   --"+
            "get    --"+
            "remove --"+
            "list   --"
            );
            group.sendMessage(msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_remove(String[] in,AbstractMessageEvent event){
        Group group = ((GroupMessageEvent)event).getGroup();
        if(in.length != 4){///sub git remove id
            group.sendMessage("argement error");
            return;
        }
        try{
            Statement stmt = conn.createStatement();
            int id = Integer.valueOf(in[3]);
            String sql = new String("delete from github where `url` = 'https://github.com/"+subweb.get(id)+"'");
            
            stmt.execute(sql);

            stmt.close();
            
            subweb.remove(id);
            group.sendMessage("remove "+String.valueOf(id)+" success");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_init(String[] in,AbstractMessageEvent event){
        if(event instanceof FriendMessageEvent){
            ((GroupMessageEvent)event).sendMessage("Group only");
            return;
        }
        if(in.length != 5){
            ((GroupMessageEvent)event).getGroup().sendMessage("sub git init failed");
            return;
        }
        try{
            if(in.length == 5){//input:/sub git init author repo
                subweb.add(in[3]+"/"+in[4]);//add author and repo
                ((GroupMessageEvent)event).getGroup().sendMessage("sub git init success"+String.valueOf(subweb.size()));
            }
        }catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    void sub_git_getUpdate(String[] in,AbstractMessageEvent event){//input:/sub git get id
        if(event instanceof FriendMessageEvent){
            ((GroupMessageEvent)event).sendMessage("Group only");
            return;
        }
        Group group = ((GroupMessageEvent)event).getGroup();
        if(in.length != 4){
            group.sendMessage("argement error");
            return;
        }
        try{
            String web = (String)subweb.get(Integer.parseInt(in[3]));
            String time1 = git_getGithubTime(web.split("/"));//get the last updated time 

            if(time1.compareTo("") == 0){//if havn't init the web
                int i = Integer.parseInt(in[3]);
                net.init((String)subweb.get(i));
                JSONObject tmpJson = net.GetURL();
                git_insertGithub(tmpJson,String.valueOf(group.getId()));
                group.sendMessage("init sub web success");
                return;
            }

            net.init("https://api.github.com/repos/"+web);//init sub web
            JSONObject tmpJson = net.GetURL();

            if(tmpJson == null){
                group.sendMessage("ERROR!");
            }

            String time2 = tmpJson.getString("updated_at");//get the newest update time

            if(time1.compareTo(time2) == 0){
                group.sendMessage("Not update");
                return;
            }

            git_updateGithub(time2,web.split("/"));//update

            //System.out.println(time1+" "+time2);

            //无意义
            //net.init("https://api.github.com/repos/"+web+"/commits?since="+time1);
            //tmpJson = net.GetURL();

            group.sendMessage("最新更新时间"+time2);
            net.Clear();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void git_insertGithub(JSONObject jsonObj,String group){//add last update time to table github
        try{
            Statement stmt = conn.createStatement();
            String[] in = {
                (String)jsonObj.get("svn_url"),
                ((String)(jsonObj.get("full_name"))).split("/")[0],
                (String)jsonObj.get("name"),
                group,
                (String)jsonObj.get("updated_at"),
                (String)jsonObj.get("description")
            };
            String sql = "insert into github values (0,'"+//id
                in[0]+"','"+//url           --https://github.com/author/repo
                in[1]+"','"+//owner         --author
                in[2]+"','"+//repo          --repo
                in[3]+"','"+//from          --groupId
                in[4]+"','"+//last_update   --date
                in[5]+"')";//info          --message
            //System.out.println(sql);
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    void git_updateGithub(String time,String[] in){
        try{
            Statement stmt = conn.createStatement();
            String sql = "update github set `last_update` = '"+time+"' where `owner` = '"+in[0]+"' and `repo` = '"+in[1]+"'";
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    String git_getGithubTime(String[] git){//git last update time to table github
        if(git.length != 2){
            System.out.println("输入参数过少或过多");
            return new String("");
        }
        String lastUpdateTime = new String("");
        try{
            Statement stmt = conn.createStatement();
            String sql = "select * from github where `owner` = '"+git[0]+"' and `repo` = '"+git[1]+"'";
            ResultSet re = stmt.executeQuery(sql);
            while(re.next()){
                lastUpdateTime += re.getString("last_update");
            }
            re.close();
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return new String("");
        }
        return lastUpdateTime;
    }

    ArrayList git_subweb(){//init sub
        ArrayList ret = new ArrayList<String>();
        try{
            Statement stmt = conn.createStatement();
            String sql = "select * from github";
            ResultSet re = stmt.executeQuery(sql);
            while(re.next()){
                ret.add(re.getString("owner")+"/"+re.getString("repo"));
            }
            re.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return ret;
    }

    void close_connect() {// 关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static ArrayList subThread(){
        try{
            ArrayList Re = new ArrayList<string>();
            Statement stmt = conn.createStatement();
            String sql = "select * from github";
            ResultSet re = sql.executeQuery(sql);
            while(re.next()){
                Re.add(re.getString("owner")+"/"+re.getString("repo")+"-"+re.getString("last_update")+"-"+re.getString("from"));
            }
            re.close();
            stmt.close();
            for(int i=0;i<Re.size();i++){
                String[] strArr = Re.get(i).split("-");
                net.init("https://api.github.com/repos/"+strArr[0]);
                JSONObject tmpJson = net.GetURL();
                String time1 = strArr[1];
                String time2 = tmpJson.getString("updated_at");
                if(time1.compareTo(time2) != 0){
                    Re.set(i,time2+":"+strArr[0]+"-"+strArr[2]);
                }else{
                    Re.set(i,"No update:"+strArr[0]+"-"+strArr[2]);
                }
                
            }
            return Re;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
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