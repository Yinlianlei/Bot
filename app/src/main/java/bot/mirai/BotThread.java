package bot.mirai;

import java.util.*;
import java.lang.Thread;
import java.text.SimpleDateFormat;

public class BotThread extends Thread {
    Date nowTime;
    SimpleDateFormat format;
    BotThread() {
        format = new SimpleDateFormat ("yyyy-MM-dd hh:mm:ss");
    };

    public void run() {
        for (int i = 0; i < 5; i++) {
            try {nowTime = new Date();
                System.out.println(i+format.format(nowTime));
                System.out.println();
                Thread.sleep(1000*60*30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}