package com.corleois.craft.craft_o2.Activities.Interfaces;

/**
 * Created by StarLink on 2017/08/12.
 */

import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * テキスト通知をリレーするクラスです
 */
public class TextNotificationRelay {
    private static Handler notify;

    /**
     * 実装したクラス1つと関係づけます
     * @param notification
     */
    public static void setNotification(Handler notification){
        notify = notification;
    }

    /**
     * 通知領域にメッセージを送ります
     * @param message
     */
    public static void sendText(String message){
        if(notify == null){
            return;
        }
        //メッセージ転送
        Message message1 = Message.obtain();
        message1.obj = message;
        notify.sendMessage(message1);

    }

}
