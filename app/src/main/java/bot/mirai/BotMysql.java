package bot.mirai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONArray;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Message;

import net.mamoe.mirai.event.events.AbstractMessageEvent;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.UserMessageEvent;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Group;

public class BotMysql {
    private final String url = "jdbc:mysql://47.102.215.193/JavaData?useSSL=true&characterEncoding=utf8";//若不设置encodoing则会导致输出为非中文字符
    private final String user = "huawei";
    private final String password = "huawei";
    private static Connection conn = null;
    private static BotNet net = null;
    private HashMap subweb = null;
    private HashMap sub_bili = null;

    BotMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            conn = DriverManager.getConnection(url, user, password);// mysql连接
            net = new BotNet();
            git_subweb();
            bili_sub();

            System.out.println("Mysql init finished.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void close_connect() {// 关闭连接
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    final void errorMsg(Contact user,String id){
        if(user instanceof Group){
            user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id))).plus(new PlainText(" ERROR:输入参数过少或过多")));
        }else{
            user.sendMessage(" ERROR:输入参数过少或过多");
        }
    }

    final void finishMsg(Contact user,String id,String msg){
        if(user instanceof Group){
            user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id)),
            new PlainText(" "+msg)));
        }else{
            user.sendMessage(" "+msg);
        }
    }

    void Bot_switch(String[] in,AbstractMessageEvent event){
        switch(in[0]){//command switch
            case "/task":switch(in[1]){//all test complete
                case "add":task_add(in,event);break;
                case "remove":task_del(in,event);break;
                case "list":task_list(in,event);break;
                case "send":task_send(in,event);break;
                case "comp":task_comp(in,event);break;
                case "help":task_help(event);;break;
                case "listC":task_listC(in,event);break;
                default:{
                    System.out.println("输入参数错误，请查看task help");
                }
            };break;
            case "/sub":switch(in[1]){//group list //delete the sub
                case "git":switch(in[2]){//github subscribe
                    case "list":sub_git_list(event);break;
                    case "init":sub_git_init(in,event);break;
                    case "get":sub_git_getUpdate(in,event);break;
                    case "help":sub_git_help(event);break;
                    case "remove":sub_git_remove(in,event);break;
                    default:{
                        System.out.println("输入参数错误，请查看sub git help");
                    }
                };break;
                case "bili":switch(in[2]){//bilibili subscribe
                    case "init":sub_bili_init(in,event);break;
                    case "help":sub_bili_help(event);break;
                    case "list":sub_bili_list(event);break;
                    case "remove":sub_bili_remove(in,event);break;
                    case "get":sub_bili_get(in,event);break;
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
        Contact user = null;
        String targetID=null,targetNick=null,targetFrom=null,targetGroup=null;

        if(event instanceof FriendMessageEvent){
            user = (((FriendMessageEvent)event).getSender());

            targetID = String.valueOf(user.getId());
            targetNick = String.valueOf(((User)user).getNick());
            targetGroup = "";
            targetFrom = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            User sender = ((GroupMessageEvent)event).getSender();

            targetID = String.valueOf(sender.getId());
            targetNick = String.valueOf(sender.getNick());
            targetGroup = String.valueOf(user.getId());
            targetFrom = String.valueOf(sender.getId());
        }

        try {//task add title info
            if (task.length != 3 && task.length != 4){//判断如果输入参数不等于4便返回错误信息
                errorMsg(user,targetID);
                return;
            }
            Statement stmt = conn.createStatement();
            String sql = new String("insert into person_task values (" + "0" + ",'" + // 编号
                    targetID + "','" + // qq号
                    targetNick + "','" + // 昵称
                    targetGroup + "','" + // 发布任务群号
                    targetFrom + "','"+//
                    task[2] + "','" + // 任务名
                    task[3] + // 备注
                    "',0)");
            // System.out.println(sql);//调试用

            //((ArrayList)taskList.get(targetID)).add(task[2]+"-"+task[3]+"-0");
            //task-note-mark
            
            stmt.execute(sql);
            stmt.close();
            //event.getSender().sendMessage("task add success");
            finishMsg(user,targetID,"task add success");
        } catch (Exception e) {
            event.getSender().sendMessage("task add faild");
            e.printStackTrace();
        }
    }

    void task_help(AbstractMessageEvent event){//显示帮助
        Contact user = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
        }
        
        user.sendMessage("task任务添加帮助：\n"+
        "task [option] <args...>\n"+
        "option:\n\t"+
        "add [任务] <备注(可选)> --任务添加\n\t"+
        "comp [任务ID] --任务完成\n\t"+
        "remove [任务ID] --任务删除\n\t"+
        "send @[QQ号] [任务] <备注(可选)> --发送任务给他人\n"
        );
    }

    void task_comp(String[] task,AbstractMessageEvent event){//完成任务
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try {//task comp id
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                errorMsg(user,id);
                return;
            }

            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = new String("update person_task set mark = 1 where num ="+targetID);
            // System.out.println(sql);//调试用

            //((ArrayList)taskList.get(id)).remove(Integer.valueOf(targetID));

            stmt.execute(sql);
            stmt.close();
            finishMsg(user,id,"task complete success");
        } catch (Exception e) {
            finishMsg(user,id,"task complete faild");
            e.printStackTrace();
        }
    }

    void task_del(String[] task,AbstractMessageEvent event){//删除目标指定编号的任务
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try {//task del id
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                errorMsg(user,id);
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = new String("delete from person_task where `num` = "+targetID);
            // System.out.println(sql);//调试用

            //((ArrayList)taskList.get(id)).remove(Integer.valueOf(targetID));

            stmt.execute(sql);
            stmt.close();
            finishMsg(user,id,"task delete success");
        } catch (Exception e) {
            finishMsg(user,id,"task delete faild");
            e.printStackTrace();
        }
    }

    void task_list(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try {
            if(task.length != 2 && task.length != 3 && task.length != 4){
                errorMsg(user,id);
                return;
            }

            String to = new String("");//new String("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark\n");
            String targetID = null;

            //String targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0];
            
            if(task.length != 2){
                targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0];
            }else{
                targetID = id;
            }
            Statement stmt = conn.createStatement();

            String sql = new String("select * from person_task where `id` = '"+targetID+"' and `mark` = 0");
            ResultSet R = stmt.executeQuery(sql);
            
            while(R.next()){
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\n");
            }

            if(task.length == 4 && task[3].equals("public")){//task list public
                user.sendMessage(to);
            }else if(task.length == 3 || (task.length == 4 && task[3].equals("private"))){
                user.sendMessage(to);
            }else{
                user.sendMessage(to);
            }
            stmt.close();
            //finishMsg(user,id,"task show success");
        } catch (Exception e) {
            finishMsg(user,id,"task delete faild");
            e.printStackTrace();
        }
    }

    void task_listC(String[] task,AbstractMessageEvent event){//展示目标账户所分配的任务
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try { //task list @target public
            if(task.length != 2 && task.length != 3 && task.length != 4){
                errorMsg(user,id);
                return;
            }
            
            String targetID = null;
            
            if(task.length !=2)
                targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0];
            else
                targetID = id;
            
            Statement stmt = conn.createStatement();
            String sql = new String("select * from person_task where id = '"+targetID+"' and `mark` = 1");
            ResultSet R = stmt.executeQuery(sql);
            String to = new String("list:\n");//new String("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark\n");
            while(R.next()){
                to += (R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\n");
            }
            if(task.length == 4 && task[3].equals("public")){
                user.sendMessage(to);
            }else if(task.length == 3 || (task.length == 4 && task[3].equals("private"))){
                user.sendMessage(to);
            }
            stmt.close();
            //finishMsg(user,id,"task show success");
        } catch (Exception e) {
            finishMsg(user,id,"task delete faild");
            e.printStackTrace();
        }
    }

    void task_send(String[] task,AbstractMessageEvent event){//send task to member
        Contact user = null;
        String id = null;
        String targetID = event.getMessage().serializeToMiraiCode().split("mirai:at:")[1].split("]")[0], 
            targetNick = null, targetGroup = null,targetNote=null,targetFrom = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());

            User sender = ((GroupMessageEvent)event).getSender();
            targetNick = sender.getNick();
            targetGroup = String.valueOf(user.getId());
            targetFrom = String.valueOf(sender.getId());
        }
        
        try {//task send @target title note
            if(task.length != 4 && task.length != 5){
                errorMsg(user,id);
                return;
            }
            
            if(task.length == 5){
                targetNote = task[4];
            }
            
            Statement stmt = conn.createStatement();
            String sql = new String("insert into person_task values (0,'" + // 编号
                    targetID + "','" + // qq号
                    targetNick + "','" + // 昵称
                    targetGroup + "','" + // 发布任务群号
                    targetFrom + "','"+
                    task[3] + "','" + // 任务名
                    targetNote + // 备注
                    "',0)");
            stmt.execute(sql);
            stmt.close();
            finishMsg(user,id,"task send success");
        } catch (Exception e) {
            finishMsg(user,id,"task send faild");
            e.printStackTrace();
        }
    }

    //change the method for Friend 
    //subweb has change from ArrayList to HashMap<String,HashMap<String,String>>
    //maybe too complex?Yeap

    void sub_git_init(String[] in,AbstractMessageEvent event){//init the subscribed web
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try{
            if(in.length == 5){//input:/sub git init author repo
                //subweb.add(in[3]+"/"+in[4]);//add author and repo

                HashMap tmpHashMap = ((HashMap)subweb.get(id));

                if(tmpHashMap == null){//if user subscribe github at first time
                    tmpHashMap = new HashMap<Integer,String[]>();
                    subweb.put(id,tmpHashMap);
                }

                net.init("https://api.github.com/repos/"+in[3]+"/"+in[4]);
                JSONObject tmpJson = net.GetURL();

                if(tmpJson == null){
                    finishMsg(user,id,in[3]+"/"+in[4]+" not exist!");
                    return;
                }
                
                String time = tmpJson.getString("updated_at");

                git_insertGithub(tmpJson,event);//insert into mysql

                tmpJson.clear();//free the resource
                
                tmpHashMap.put(tmpHashMap.size(),new String[]{in[3]+"/"+in[4],time});
                //HashMap<String,HashMap<int,String[]>>

                finishMsg(user,id,"sub git init success:"+String.valueOf(tmpHashMap.size()-1));
            
                //need to insert subscribe reposritories into mysql//complete
            }else{
                errorMsg(user,id);
            }
        }catch(Exception e) {
            e.printStackTrace();
            return;
        }
    }

    void sub_git_list(AbstractMessageEvent event){//list the subcribed web for user
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }
        
        try{
            //Group group = ((GroupMessageEvent)event).getGroup();
            String msg = new String("list:");
            HashMap targetSubWeb = ((HashMap)subweb.get(id));

            Iterator<Integer> it = targetSubWeb.keySet().iterator();
            while(it.hasNext()){
                int key = it.next();
                String[] list = (String[])targetSubWeb.get(key);
                msg += "\n"+String.valueOf(key)+"-"+list[0]+"-"+list[1];
                //ouput:list:id-owner/repo-last_update_time
            }

            finishMsg(user,id,msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_help(AbstractMessageEvent event){//give the github subscribe for user
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try{
            //Group group = ((GroupMessageEvent)event).getGroup();
            String msg = new String("sub git help\n"+
            "sub git [option] <args>\n"+
            "option:\n"+
            "init   --\n"+
            "get    --\n"+
            "remove --\n"+
            "list   --\n"
            );
            finishMsg(user,id,msg);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_remove(String[] in,AbstractMessageEvent event){//remove the subscribe web
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }
        
        //Group group = ((GroupMessageEvent)event).getGroup();
        if(in.length != 4){//input:/sub git remove id
            errorMsg(user,id);
            return;
        }


        try{//input:/sub git remove id
            Statement stmt = conn.createStatement();
            int targetId = Integer.valueOf(in[3]);
            String sql = new String("delete from github where `url` = 'https://github.com/"+subweb.get(targetId)+"'");
            
            HashMap tmpMap = ((HashMap)subweb.get(id));
            //HashMap<String,HashMap<int,String[]>>

            tmpMap.remove(targetId);//remove targetId before mysql

            stmt.execute(sql);

            stmt.close();
            
            finishMsg(user,id,"remove "+String.valueOf(targetId)+" success");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_git_getUpdate(String[] in,AbstractMessageEvent event){
        //input:/sub git get id//use to update this repo immediately
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        /*
        if(event instanceof FriendMessageEvent){
            ((GroupMessageEvent)event).getGroup().sendMessage("Group only");
            return;
        }
        */

        //Group group = ((GroupMessageEvent)event).getGroup();
        if(in.length != 4){
            errorMsg(user,id);
            return;
        }

        try{//input:/sub git get id
            Statement stmt = conn.createStatement();
            int targetId = Integer.parseInt(in[3]);
            HashMap web = (HashMap)subweb.get(id);
            String[] list = (String[])web.get(targetId);
            String[] repo = list[0].split("/");

            String sql = new String("select * from github where `owner` = '"+
                repo[0]+"' and `repo` = '"+repo[1]+"'"),time1=null,time2=null;

            ResultSet re = stmt.executeQuery(sql);

            while(re.next()){
                time1 = re.getString("last_update");
            }

            if(time1.isEmpty() == true){//if the repo don't exist in mysql
                finishMsg(user,id,"This repo don't subscribe");
                return;
            }

            net.init("https://api.github.com/repos/"+web);
            JSONObject tmpJson = net.GetURL();

            if(tmpJson == null){//if this repo not exists or has been deleted
                finishMsg(user,id,repo[0]+"/"+repo[1]+" not exist or has been deleted");
                return;
            }

            time2 = tmpJson.getString("updated_at");            

            //if subweb contain this repo for user

            if(time1.compareTo(time2) > 0){
                git_updateGithub(time2,repo);//update this repo
                finishMsg(user,id,"最新更新时间"+time2);
                return;
            }else{
                finishMsg(user,id,"no update");
                return;
            }


            /*
            {
                String web = (String)subweb.get(Integer.parseInt(in[3]));
                String time1 = git_getGithubTime(web.split("/"));//get the last updated time 

                if(time1.isEmpty() == true){//if havn't init the web
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
            }
            */
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void git_insertGithub(JSONObject jsonObj,AbstractMessageEvent event){//add last update time to table github
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try{
            Statement stmt = conn.createStatement();
            String[] in = {
                jsonObj.getString("svn_url"),
                jsonObj.getString("full_name").split("/")[0],
                jsonObj.getString("name"),
                //subfrom,
                id,
                //group
                String.valueOf(user instanceof Group?user.getId():"0"),
                jsonObj.getString("updated_at"),
                jsonObj.getString("description")
            };
            String sql = new String("insert into github values (0,'"+//id
                in[0]+"','"+//url           --https://github.com/author/repo
                in[1]+"','"+//owner         --author
                in[2]+"','"+//repo          --repo
                in[3]+"','"+//subFrom       --subFrom
                in[4]+"','"+//from          --groupId
                in[5]+"','"+//last_update   --date
                in[6]+"')");//info          --message
            //System.out.println(sql);
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    void git_updateGithub(String time,String[] in){//get the last update time from mysql
        try{
            Statement stmt = conn.createStatement();
            String sql = new String("update github set `last_update` = '"+time+"' where `owner` = '"+in[0]+"' and `repo` = '"+in[1]+"'");
            stmt.execute(sql);
            stmt.close();
        }catch (Exception e) {
            e.printStackTrace();
            return;
        }
        return;
    }

    /*
    String git_getGithubTime(String[] git){//git last update time to table github
        if(git.length != 2){
            System.out.println("输入参数过少或过多");
            return new String("");
        }
        String lastUpdateTime = new String("");
        try{
            Statement stmt = conn.createStatement();
            String sql = new String("select * from github where `owner` = '"+git[0]+"' and `repo` = '"+git[1]+"'");
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
    */

    void git_subweb(){//init subscribe//HashMap<String,HashMap<int,String[]>>
        try{
            Statement stmt = conn.createStatement();
            String sql = new String("select distinct(subFrom) from github");
            ResultSet re = stmt.executeQuery(sql);
            subweb = new HashMap<String,HashMap<Integer,String[]>>();//struct:QQ:["{owner/repo,last_update}"]
            ArrayList userList = new ArrayList<String>();
            while(re.next()){
               userList.add(re.getString("subFrom"));
            }
            for(int i=0;i<userList.size();i++){
                String user = String.valueOf(userList.get(i));
                subweb.put(user,new HashMap<Integer,String[]>());
                sql = new String("select * from github where `subFrom` = '"+user+"'");
                
                re = stmt.executeQuery(sql);
                HashMap tmpHashMap = ((HashMap)subweb.get(user));
                while(re.next()){
                    tmpHashMap.put(i,new String[]{re.getString("owner")+"/"+re.getString("repo"),re.getString("last_update")});
                }
            }
            re.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static ArrayList subThread(){//static function for Thread
        try{//HashMap<String,HashMap<int,String[]>>
            ArrayList Re = new ArrayList<String>();
            Statement stmt = conn.createStatement();
            String sql = new String("select * from github");
            ResultSet re = stmt.executeQuery(sql);
            while(re.next()){
                Re.add(re.getString("owner")+"/"+re.getString("repo")+"*"+re.getString("last_update")+"*"+re.getString("from"));
            }
            re.close();
            
            for(int i=0;i<Re.size();i++){
                String[] strArr = String.valueOf(Re.get(i)).split("\\*");

                net.init("https://api.github.com/repos/"+strArr[0]);
                JSONObject tmpJson = net.GetURL();
                String time1 = strArr[1],time2 = tmpJson.getString("updated_at");

                if(time1.compareTo(time2) > 0){
                    Re.set(i,time2+":"+strArr[0]+"*"+strArr[2]);
                    sql = new String("update github set `last_update` = '"+time2+"' where `owner` = '"+strArr[0].split("/")[0]+"' and `repo` = '"+strArr[0].split("/")[1]+"'");
                    stmt.execute(sql);//havn't update subweb;//This is a problem
                }else{
                    Re.set(i,"No update:"+strArr[0]+"*"+strArr[2]);
                }
                Thread.sleep(10000);//sleep for 10s for github api
                tmpJson.clear();
            }
            stmt.close();
            return Re;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    void bili_sub(){//init bili subscribe
        try{
            sub_bili = new HashMap<String,ArrayList<String>>();
            Statement stmt = conn.createStatement();
            String sql = new String("select * from bili");
            ResultSet re = stmt.executeQuery(sql);
            while(re.next()){
                String sub = re.getString("subFrom");
                if(sub == null){
                    return;
                }
                if(!sub_bili.containsKey(sub)){
                    sub_bili.put(sub,new ArrayList<String>());
                }
                ((ArrayList)sub_bili.get(sub)).add(re.getString("name")+"*"+re.getString("uid")+"*"+re.getString("views")+"*"+
                re.getString("latestView")+"*"+re.getString("latestTitle")+"*"+
                re.getString("subFrom")+"*"+re.getString("UserOrGroup"));
                //sub:["name*uid*views*latestView*latestTitle*subFrom*Group"]
            }
            re.close();
            stmt.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_bili_init(String[] in,AbstractMessageEvent event){//init subscribe bilibili
        Contact user = null;
        String id = null;
        
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        if(in.length != 4){
            errorMsg(user,id);
            return;
        }
        
        try{//sub bili init uid:return UpNick/UpUid
            net.init("https://api.bilibili.com/x/space/arc/search?mid="+in[3]+"&pn=1&ps=3&jsonp=jsonp");//init sub web
            JSONObject tmpJson = net.GetURL();
            
            tmpJson = tmpJson.getJSONObject("data");

            int viewsCount = tmpJson.getJSONObject("page").getIntValue("count");
            tmpJson = tmpJson.getJSONObject("list").getJSONArray("vlist").getJSONObject(0);
            if(tmpJson == null){
                user.sendMessage("Not");
                return;
            }
            String subId = String.valueOf(event instanceof FriendMessageEvent?"0":user.getId());
            String bv = tmpJson.getString("bvid");
            String name = tmpJson.getString("author");
            String title = tmpJson.getString("title");

            String input = new String(name+"*"+in[3]+"*"+viewsCount+"*"+bv+"*"+title+"*"+id+"*"+subId);

            if(((ArrayList)sub_bili.get(id)).contains(input)){
                if(user instanceof Group){
                    user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id))).
                        plus(new PlainText(" up exist")));
                }else{
                    user.sendMessage(" up exist");
                }
                return;
            }

            Statement stmt = conn.createStatement();
            String sql = new String("insert into bili values (0,'"+    //id
                name+"','"+                                 //name
                in[3]+"',"+                                 //uid
                viewsCount+",'"+                            //views count
                bv+"','"+                                   //latest Bv
                title+"','"+                                //latest Title
                id+"','"+                                   //sub id
                subId+                                      //0:Friend,Id:Group
                "')");

            stmt.execute(sql);

            stmt.close();

            if(!sub_bili.containsKey(id)){
                //System.out.println("Not contain");
                ArrayList tmpArrayList = new ArrayList<String>();
                tmpArrayList.add(input);
                
                sub_bili.put(id,tmpArrayList);
                //sub:["name*uid*views*latestView*latestTitle*subFrom*Group"]
            }

            ((ArrayList)sub_bili.get(id)).add(name+"*"+in[3]+"*"+viewsCount+"*"+bv+"*"+title+"*"+id+"*"+subId);//add subscribe up
            //sub:[name*uid*views*latestView*latestTitle*subFrom*UserOfGroup]

            if(user instanceof Group){
                //MessageUtils.newChain(MessageUtils.newChain("Hello"), Image.fromId("{f8f1ab55-bf8e-4236-b55e-955848d7069f}.png"));
                user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id))).plus(new PlainText(" :"+name+"/"+in[3])));
            }else{
                user.sendMessage("sub Up:"+name+"/"+in[3]);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_bili_help(AbstractMessageEvent event){//for help
        Contact user = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
        }
        try{//sub bili help
            if(user instanceof Group){
                user.sendMessage(MessageUtils.newChain(new At(((GroupMessageEvent)event).getSender().getId())).plus(" bilibili Up subscribe help:\n"+
                "sub bili [option] <args>:\n"+
                "options:\n"+
                "init       --\n"+
                "list       --\n"+
                "help       --\n"+
                "remove     --\n"));
            }else{
                user.sendMessage("bilibili Up subscribe help:\n"+
                "sub bili [option] <args>:\n"+
                "options:\n"+
                "init       --\n"+
                "list       --\n"+
                "help       --\n"+
                "remove     --\n");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_bili_list(AbstractMessageEvent event){//list the subsrcibe bili list
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }

        try{//sub bili list
            String msg = new String("list:");
            ArrayList<String> re = (ArrayList)sub_bili.get(id);
            if(re == null){
                re = new ArrayList<String>();
                finishMsg(user,id,"Null");
                return;
            }
            for(int i =0;i<re.size();i++){
                //System.out.println(re.get(i));
                String[] list = ((String)re.get(i)).split("\\*");
                msg += "\n"+String.valueOf(i)+"-"+list[0]+"-"+list[4];
            }
            if(user instanceof Group){
                user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id)),new PlainText(" "+msg)));
            }else{
                user.sendMessage(msg);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_bili_get(String[] in,AbstractMessageEvent event){
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }
        if(in.length != 3){
            user.sendMessage("Error");
            return;
        }
        try{//sub bili get
            Statement stmt = conn.createStatement();
            String sql = new String("select * from bili where `subFrom` = '"+id+"' and `UserOrGroup` = "+(user instanceof Group?user.getId():"0"));
            
            ResultSet re = stmt.executeQuery(sql);
            ArrayList ret = new ArrayList<String>();
            while(re.next()){
                ret.add(re.getString("name")+"-"+re.getString("latestTitle")+"-"+re.getString("latestView"));
            }//name*uid*views*latestView*latestTitle*subFrom*Group

            String msg = new String("list:");

            for(int i=0;i<ret.size();i++){
                String[] list = ((String)ret.get(i)).split("\\*");
                msg += "\n"+String.valueOf(i)+list[0]+"-"+list[4];
            }

            if(user instanceof Group){
                user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id)),
                new PlainText(" "+msg)));
            }else{
                user.sendMessage(" "+msg);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    void sub_bili_remove(String[] in,AbstractMessageEvent event){//remove the subscribed up
        Contact user = null;
        String id = null;
        if(event instanceof FriendMessageEvent){
            user = ((FriendMessageEvent)event).getSender();
            id = String.valueOf(user.getId());
        }else if(event instanceof GroupMessageEvent){
            user = ((GroupMessageEvent)event).getGroup();
            id = String.valueOf(((GroupMessageEvent)event).getSender().getId());
        }
        try{//sub bili remove id
            String[] list = ((String)((ArrayList)sub_bili.get(id)).get(Integer.valueOf(in[3]))).split("\\*");
            ArrayList target = ((ArrayList)sub_bili.get(id));
            System.out.println(Integer.valueOf(in[3]));
            target.remove(Integer.valueOf(in[3]));
            target.clear();

            Statement stmt = conn.createStatement();
            
            String sql = new String("delete from bili where `uid` = '"+
                list[1]+"' and `subFrom` = '"+list[5]+"'");
            //name*uid*views*latestView*latestTitle*subFrom*Group

            stmt.execute(sql);

            if(user instanceof Group){
                user.sendMessage(MessageUtils.newChain(new At(Long.valueOf(id)),
                new PlainText(" Remove success")));
            }else{
                user.sendMessage("Remove success");
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void biliUpdateThread(){//for thread to update data
        try{
            Statement stmt = conn.createStatement();
            String sql = new String("select * from bili");
            ResultSet re = stmt.executeQuery(sql);
            ArrayList updateList = new ArrayList<String>();//update up list
            while(re.next()){
                updateList.add(re.getString("name")+"-"+re.getString("uid")+"-"+
                    re.getString("views")+"-"+re.getString("latestTitle"));
            }//name-uid-viewCount-latestTitle
            re.close();

            for(int i = 0;i<updateList.size();i++){
                String[] list = ((String)updateList.get(i)).split("-");
                net.init("https://api.bilibili.com/x/space/arc/search?mid="+list[1]+"&pn=1&ps=1&jsonp=jsonp");
                JSONObject tmpJson = net.GetURL();
                tmpJson = tmpJson.getJSONObject("data");

                int newCount = tmpJson.getJSONObject("page").getIntValue("count");

                if(newCount <= Integer.valueOf(list[2])){
                    continue;
                }

                tmpJson = tmpJson.getJSONObject("list").getJSONArray("vlist").getJSONObject(0);
                String newViewTitle = tmpJson.getString("title");
                String newViewBv = tmpJson.getString("bvid");

                sql = new String("update bili set `views` = "+newCount+
                    ",`latestView` = '"+newViewBv+
                    "',`latestTitle` = '"+newViewTitle+"' where `uid` = '"+
                    list[1]+"'");

                stmt.execute(sql);
            }

            //net.init("https://api.bilibili.com/x/space/arc/search?mid="+in[3]+"&pn=1&ps=3&jsonp=jsonp");//init sub web
            //JSONObject tmpJson = net.GetURL();

            //sub:[name*uid*views*latestView*latestTitle*subFrom*UserOfGroup]
        }catch(Exception e){
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
        MessageChain re = MessageUtils.newChain("").plus(MessageUtils.newChain(""));
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