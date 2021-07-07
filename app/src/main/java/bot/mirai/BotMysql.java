package bot.mirai;

import java.sql.*;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.Message;

public class BotMysql {
    private final String url = "jdbc:mysql://47.102.215.193/JavaData?useSSL=true&characterEncoding=utf8";//若不设置encodoing则会导致输出为非中文字符
    private final String user = "huawei";
    private final String password = "huawei";
    private Connection conn = null;

    BotMysql() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(url, user, password);// mysql连接
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_swich(String args){
        String[] in = args.split(" ");
        switch(in[0]){
            case "task":switch(in[1]){
                case "add":task_add(in);break;
                case "del":task_del(in);break;
                case "show":task_show(in);break;
                case "send":task_send(in);break;
                case "comp":task_comp(in);break;
                case "help":task_help();;break;
                default:
                    System.out.println("输入参数错误，请查看task help");
            };break;
            default:
                System.out.println("输入参数错误，请参考help");
        }
    }

    void task_add(String[] task) {// 添加任务
        try {
            if (task.length != 4){//判断如果输入参数不等于4便返回错误信息
                System.out.println("ERROR:输入参数过少");
                return;
            }
            String targetID = "1114561520", targetNick = "无序常量", targetGroup = "112",targetFrom = "20191739";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_help(){//显示帮助
        System.out.println("task任务添加帮助：\n"+
        "task [option] <args...>\n"+
        "option:\n\t"+
        "add [任务] <备注(可选)> --任务添加\n\t"+
        "comp [任务ID] --任务完成\n\t"+
        "del [任务ID] --任务删除\n\t"+
        "send [QQ号] [任务] <备注(可选)> --发送任务给他人\n"
        );
    }

    void task_comp(String[] task){//完成任务
        try {
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                System.out.println("ERROR:输入参数过少");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "update person_task set mark = 1 where num ="+targetID;
            // System.out.println(sql);//调试用
            stmt.execute(sql);
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_del(String[] task){//删除目标指定编号的任务
        try {
            if (task.length != 3){//判断如果输入参数不等于4便返回错误信息
                System.out.println("ERROR:输入参数过少");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "delete from person_task where num = "+targetID;
            // System.out.println(sql);//调试用
            stmt.execute(sql);
            stmt.close();
            } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_show(String[] task){//展示目标账户所分配的任务
        try {
            if(task.length != 3){
                System.out.println("ERROR 输入参数过少");
                return;
            }
            String targetID = task[2];
            Statement stmt = conn.createStatement();
            String sql = "select * from person_task where id = '"+targetID+"'";
            ResultSet R = stmt.executeQuery(sql);
            System.out.println("num\tid\t\tnick\t\tgroup\t\tfrom\ttask\tnote\tmark");
            while(R.next()){
                System.out.println(R.getString("num")+"\t"+R.getString("id")+"\t"+R.getString("nick")+"\t"+R.getString("group")+"\t"+R.getString("fromId")+"\t"+R.getString("task")+"\t"+R.getString("note")+"\t"+R.getString("mark"));
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void task_send(String[] task){
        try {
            if(task.length < 4 || task.length > 5){
                System.out.println("输入参数过少或过多");
                return;
            }
            String targetID = task[2], targetNick = "无序常量", targetGroup = "112",targetNote="",targetFrom = "20191739";
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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