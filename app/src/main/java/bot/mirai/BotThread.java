package bot.mirai;

import java.util.*;
import java.lang.Thread;
import java.text.SimpleDateFormat;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;

public class BotThread extends Thread {
    private Date time1=null,time2=null;
    private long sleepTime=0;
    SimpleDateFormat format;
    BotThread() {//init
        format = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss");//init format
        daily_init();
    };

    void daily_init(){
        String[] t = format.format(new Date(time1.getTime()+24*60*60*1000)).split(" ");
        time2 = format.parse(t[0]+" 07:00:00");//init send message time
    }

    void excute(Bot bot){
        time1 = new Date();
        if(time1.compareTo(time2) > 0){
            daily_init();
            Arraylist re = BotMirai.subThread();
            for(int i=0;i<re.size();i++){
                String[] strArr = re.get(i).split("-");//updateTime:owner/repo-groupId
                Group group = bot.getGroupOrFail(Long.valueOf(strArr[1]));
                group.sendMessage(strArr[0]);
            }
            Thread.sleep(15000);//sleep for 15s
        }
        sleepTime = time2.getTime() - time1.getTime();
        Thread.sleep(sleepTime);
    }

    public void run() {
    }
}