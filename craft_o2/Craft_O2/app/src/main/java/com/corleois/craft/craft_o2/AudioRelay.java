package com.corleois.craft.craft_o2;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;

import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Activities.fragments.MusicPlayerFragment;
import com.corleois.craft.craft_o2.Control.Controller;

/**
 * Created by corleois on 2017/06/24.
 */

//画面への表示変更要求を中継（Handlerへmessageを渡す）するクラス
public class AudioRelay {
    //再生画面
    private static final int CHANGE_PLAYINGTIME = 1;
    private static final int CHANGE_PLAYTIME = 2;
    private static final int CHANGE_TIME_INFO = 0;
    private static final int CHANGE_SEEK_POSITION = 3;
    private static final int CHANGE_SEEK_MAX = 4;
    private static final int CHANGE_BUTTON = 5;

//    private Thread thread = new Thread(this);
//    public boolean running = false;
//    private boolean playing = false;//停止中：false　再生中：true
//    private String NowFilePath = "";
//    private int countPlayingTime = 0;


    private MusicSelectionActivity MSActivity;
    private MusicPlayerFragment MPFragment;

    private Controller ctr = Controller.getController();
    private static Handler handler;    //再生状態変更の通知を行うハンドラ

    public AudioRelay(Activity activity){
        MSActivity = (MusicSelectionActivity) activity;
        MPFragment = (MusicPlayerFragment) MSActivity.getSupportFragmentManager().findFragmentById(R.id.playerFragment);
//        running = true;
//        thread.start();
    }

    //AudioTrackCtrlからの画面変更要求を受け取り、MusicPlayerActivityへ送る
//    public void RelayToMPA(int order) {
//        Message message = new Message();
//        message.what = order;
//        MPFragment.handler.sendMessage(message);
//        ((MusicPlayerFragment) MSActivity.getSupportFragmentManager().findFragmentById(R.id.playerFragment)).handler.sendMessage(message);
//    }

    public String RelayGetPath() {
        return ctr.GetNextFilePath();
    }

    /**
     * 画面に対して、再生状態が変わったことの通知を、設定されたハンドラに対して行います
     */
    public void notifyPlayStateChangeToFragment(){
        //ハンドラがnullだったら通知しても意味がないので終了
        if (handler == null) {
            return;
        }
        //メッセージ送信
        Message message = Message.obtain();
        message.what = MusicPlayerFragment.NOTIFY_PLAYSTATE;
        handler.sendMessage(message);
    }

    /**
     * サービスの再生状態が変わったことを通知する先のハンドラを設定します。
     * nullが設定されると、通知されません。
     * @param handler1
     */
    public static void resistPlayStateChangeReceiveHandler(Handler handler1){
        handler = handler1;
    }

    public int getSize() {
        return ctr.getQueueSize();
    }


//    @Override
//    public void run() {
//        while(running){
//            //再生している曲を問い合わせる
//            String NowPlayingFilePath = ctr.GetNowFilePath();
//            //自分が認識している情報と相違があれば
//            if(!NowFilePath.equals(NowPlayingFilePath)) {
//                //情報の更新
//                NowFilePath = NowPlayingFilePath;
//                //秒数のカウントリセット
//                countPlayingTime = 0;
//                MPFragment.countPlayingTime = countPlayingTime;
//                Message msg = new Message();
//                msg.what = CHANGE_TIME_INFO;
//                MPFragment.handler.sendMessage(msg);
//                try {
//                    thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                continue;
//            }
//            //現在の再生状態を問い合わせる
//            if(ctr.getPlayState() != AudioTrack.PLAYSTATE_PLAYING){
//                //再生中ではなかった場合、画面が保持している再生状態を停止中に変更し、ボタンの表示を変更する
//                if(MPFragment.playing) {
//                    MPFragment.playing = false;
//                    Message msg = new Message();
//                    msg.what = CHANGE_BUTTON;
//                    MPFragment.handler.sendMessage(msg);
//                }
//            }
//
//            //再生中の場合
//            if(MPFragment.playing) {
//                //秒数をﾒｯｾｰｼﾞに入れてハンドラに送る
//                Message msg = new Message();
//                msg.what = CHANGE_PLAYINGTIME;
//                MPFragment.handler.sendMessage(msg);
//                //秒数を１秒ずつカウント
//                countPlayingTime += 1000;
//                MPFragment.countPlayingTime = countPlayingTime;
//                //1秒休み
//                try {
//                    thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
//
//        }
//    }
}
