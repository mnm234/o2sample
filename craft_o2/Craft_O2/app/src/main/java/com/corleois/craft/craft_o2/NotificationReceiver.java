package com.corleois.craft.craft_o2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;

/**
 * Created by corleois on 2017/06/27.
 */


//通知領域からの操作を受け付けるクラス
public class NotificationReceiver extends BroadcastReceiver {
    public static final String CLICK_NOTIFICATION = "click_notification";
    public static final String DELETE_NOTIFICATION = "delete_notification";
    public static final String CLICK_NEXT = "click_next";
    public static final String CLICK_PREVIOUS = "click_previous";
    public static final String CLICK_PAUSE = "click_pause";
    public static final String CLICK_PLAY = "click_play";

    Controller ctr = Controller.getController();

    /**
     * 通知領域でにイベントを受け取ったら……
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();

        switch (action){
            case CLICK_NOTIFICATION:
                //通知タップ時のイベント
                Intent new_intent = new Intent(context,MusicSelectionActivity.class);
                new_intent.setFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(new_intent);
                break;
            case DELETE_NOTIFICATION:
                //通知削除時のイベント
                ctr.StopService();
                break;
            case CLICK_NEXT:
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }
                ctr.playNextPrevTrack(false,null);
                break;
            case CLICK_PAUSE:
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }
                //コントローラに対してaudioTrackへの処理を要求する
                if(!ctr.OrderFromPlayScreen("OrderStop")){
                    Toast.makeText(context,"再生ができないファイルがありました。",Toast.LENGTH_SHORT).show();
                }
                break;
            case CLICK_PLAY:
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }
                //コントローラに対してaudioTrackへの処理を要求する
                if(!ctr.OrderFromPlayScreen("OrderStart")){
                    Toast.makeText(context,"再生ができないファイルがありました。",Toast.LENGTH_SHORT).show();
                }
                break;
            case CLICK_PREVIOUS:
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }
                ctr.playNextPrevTrack(true,null);
                break;
        }
    }
}
