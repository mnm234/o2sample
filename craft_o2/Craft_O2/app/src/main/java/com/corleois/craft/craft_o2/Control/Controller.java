/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.corleois.craft.craft_o2.Control;

import android.app.Activity;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.corleois.craft.craft_o2.AudioTrackCtrl;
import com.corleois.craft.craft_o2.PlaybackQueue.AudioFileQueueRecord;
import com.corleois.craft.craft_o2.PlaybackQueue.PlayOrder;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;



/**
 *
 * @author 2150087
 * コントローラーです
 */
public class Controller /*extends Activity*/{

    private static Controller controller = new Controller();

    private static String status;

	//何か
    AudioFileQueueRecord afqr = null;
	
	//再生キューリストのオブジェクト。
	//コントローラ専用のメソッドをオブジェクトの生成によって利用できる。
	private static PlaybackQueue pbq = new PlaybackQueue();
	
    //再生ステータスのオブジェクト
    PlaybackStatus pbs;

    //再生制御のオブジェクト
    private static AudioTrackCtrl atc = new AudioTrackCtrl();
//    private AudioTrackCtrl atc = new AudioTrackCtrl();

    private static String NowFilePath = "";


    /**
	 * コンストラクタだよ～ん
	 */
/*	public Controller(){
		afqr = null;
		pbs = new PlaybackStatus();
		pbq = new PlaybackQueue();
        atc = new AudioTrackCtrl();
	}
*/

    /**
     * コンストラクタの代用品だよ～ん
     */
    public static Controller getController(){
        return controller;
    }
	
    //次に再生するファイルパスを取得して、PlayBackオブジェクトに返す。
    public String GetNextFilePath(){
        String nextFilePath = pbq.GetNextFilePath();
        if(nextFilePath != null) {
            NowFilePath = nextFilePath;
//        }else{
//            NowFilePath = null;
        }
        return nextFilePath;
    }
	
    //次に再生するファイルパスを取得して、PlayBackオブジェクトに返す。
    public String GetPreviewtFilePath(){
        String previewFilePath = pbq.GetPreviewFilePath();
        if(previewFilePath != null){
            NowFilePath = previewFilePath;
//        }else {
//            NowFilePath = null;
        }
        return previewFilePath;
    }

    //今再生している曲のファイルパスを返す
    public String GetNowFilePath() {
        return NowFilePath;
    }

	/**
	 * 一曲ループがあるかどうかを返すメソッド。Boolean
	 * @return 
	 */
	public boolean GetLoop_SingleFlag(){
		return PlaybackQueue.GetPlayOrder().contains(PlayOrder.LOOP_SINGLE);
	}

    /**
     * キューされている曲数を返します
     * @return
     */
	public int GetTrackCount(){
        return PlaybackQueue.Count();
    }
	
    //PlayBackオブジェクトにボリュームを返す。
    public int GetVolume(){
        return pbs.volume;
    }
    
    //PlayBackオブジェクトにPlay.Pause.Stopを返す。
    public String GetPlayPauseStop(){
        return pbs.playpausestop;
    }
    
    //PlayBackオブジェクトにStereo.Monoを返す。
    public String GetStereoMono(){
        return pbs.stereomono;
    }
    
    //
    //public GetPan(){}
    //
    //public GetEQ(){}
    
    //曲の長さを受け取る
    //public SendMusicLength(int length){pbs.musiclength = length} 
    //曲の現在の再生位置を受け取る
    //public SendNowPosition(int nowposition){pbs.seekbar = nowposition}
    //曲のシークバーを受け取る
    //public SendSeekbar(int seekbar){pbs.seekbar = seekbar}
    //曲の再生状態を受け取る
    //public SendPlayStatus(String status){pbs.playstatus = status}
    
    //再生画面への再生ステータスの送信
    public boolean SendPbSToPlayScreen(){
        boolean flg = true;
        
        return flg;
        
    }
    //再生オブジェクトへの再生制御指示
    public boolean OrderToPlayObject(){
        boolean flg = true;
        
        return flg;
    }
    //再生オブジェクトからの再生ステータスの受取
    public boolean GetPbSFromPlayObject(){
        //pbs = 
        boolean flg = true;
        
        return flg;
    }


    //再生画面からの再生制御指示(その１)：再生・停止
    public boolean OrderFromPlayScreen(String order){
        //処理が実行されたかどうかのフラグ
        boolean flg = false;
//        NowFilePath = path;

        //int playState = atc.playState();//audioTrackの再生状態を取得
        int playState;
        //操作要求が再生開始の時
        if(order.equals("OrderStart")){
            Log.d("PlayOrder","再生要求");

            if(atc.audioTrackIsNull()){
                if(NowFilePath == null) {
                    boolean flg2 = playNextPrevTrack(false,null);
                    if(flg2){
                        flg = atc.audioTrackStartOperation();
                    }
                }else{
                    if(! atc.setAudioTrack(NowFilePath)){
                        int tryCount = 1;
                        int countSize = pbq.Count();
                        do{
                            NowFilePath = GetNextFilePath();
                            if(NowFilePath == null){
                                continue;
                            }
                            tryCount++;
                        }while(!atc.setAudioTrack(NowFilePath) && tryCount <= countSize);
                        //do-whileを抜けた条件がキューのリストを１周したからだった時は、
                        //再生できる曲がなかったので処理終了
                        if(tryCount > countSize){
                            return false;
                        }
                    }
                    flg = atc.audioTrackStartOperation();
                }
            }else {
                playState = atc.playState();
                //再生状態が停止中の時
                if (playState == AudioTrack.PLAYSTATE_STOPPED) {
                    //再生開始要求
                    flg = atc.audioTrackStartOperation();
                }
            }
        //操作要求が再生停止の時
        }else if(order.equals("OrderStop")) {
            Log.d("PlayOrder","停止要求");
            playState = atc.playState();
            //再生状態が再生中の時
            if(playState == AudioTrack.PLAYSTATE_PLAYING) {
                //再生停止要求
                flg = atc.audioTrackStopOperation();
            }
        }
        return flg;
    }

    //次の曲・前の曲を再生する指示
    public boolean playNextPrevTrack(boolean prevCheck,String skip) {
        boolean check = false;  //←指示が通ったかどうかのフラグでいいのかな
        Log.d("QueueCount", String.valueOf(pbq.Count()));
        //前の曲または次の曲のファイルパスを取得し格納
        String filePath;
        if(prevCheck) {
            filePath = GetPreviewtFilePath();
        }else {
            if(skip == null) {
                Log.d("selectFileNext","次の曲を再生");
                filePath = GetNextFilePath();
            }else{

                do {
                    filePath = GetNextFilePath();
                    if (filePath == null) {
                        do {
                            filePath = GetPreviewtFilePath();
                            if(filePath == null){
                                break;
                            }
                        } while (!filePath.equals(skip));
                    }
                    //大量の楽曲をキューに追加したとき、処理時間のおよそ半分を↓の処理がとっているので、コメントアウトしました
//                    Log.d("selectFileSkip", String.valueOf(filePath.equals(skip)));
                } while (!filePath.equals(skip));
            }
        }
        //渡された値がnullの場合、処理終了する
        if(filePath == null){
            return false;
        }
        //以下、audioTrackの状態を確認するときnullの可能性があるので注意！！！

        //一応audioTrackがnullかどうか
        if(!atc.audioTrackIsNull()) {
            //audioTrackがnullではないとき、現在の再生状態を保持
            int playState = atc.playState();
            //再生中の場合一旦再生を停止する
            if (playState == AudioTrack.PLAYSTATE_PLAYING) {
                atc.audioTrackStopOperation();
            }

            //再生する曲をセットし、再生処理を行う
            if(! atc.setAudioTrack(filePath)){
                int tryCount = 1;
                int countSize = pbq.Count();
                //セットに成功するか、再生キュー全曲を試行するまで繰り返す
                do{
                    filePath = GetNextFilePath();
                    if(filePath == null){
                        continue;
                    }
                    tryCount++;
                    Log.d("filepath / count(273)",filePath +" / " + tryCount);
                }while(!atc.setAudioTrack(filePath) && tryCount <= countSize);
                //do-whileを抜けた条件がキューのリストを１周したからだった時は、
                //再生できる曲がなかったので処理終了
                if(tryCount >= countSize){
                    return false;
                }
            }
            //曲送り実行時に再生状態なら再生する。
            if(playState == AudioTrack.PLAYSTATE_PLAYING) {
                //もともと再生中の場合、再生開始
                check = atc.audioTrackStartOperation();
                Log.d("コントローラ251","check:"+check);
            }else {
                //そうでなければ再生しない
                Log.d("コントローラ253","true");
                return true;
            }
        }else{
            //audioTrackがnullだった時はセットのみ
            if(! atc.setAudioTrack(filePath)){
                int tryCount = 1;
                int countSize = pbq.Count();
                //セットに成功するか、再生キュー全曲を試行するまで繰り返す
                do{
                    filePath = GetNextFilePath();
                    if(filePath == null){
                        continue;
                    }
                    tryCount++;
                    Log.d("filepath / count（302）",filePath +" / " + tryCount);
                }while(!atc.setAudioTrack(filePath) && tryCount <= countSize);
                //do-whileを抜けた条件がキューのリストを１周したからだった時は、
                //再生できる曲がなかったので処理終了
                if(tryCount > countSize){
                    return false;
                }
            }
            check = atc.audioTrackStartOperation();
            Log.d("コントローラ259","check:"+check);
            return check;
        }
        return check;
    }

    //再生画面からの操作制御指示(その２)：再生位置変更
    public boolean OrderFromPlayScreen(String order, int position){
        boolean flg = false;

        long time = position*1000;

        if(order.equals("OrderSeekTo")){
            flg = atc.audioTrackSeekOperation(time);
        }

        return flg;
    }

    public void SkipFilePath(String skipToFilePath) {

        String path = (NowFilePath == null) ? "" : NowFilePath;

        while(!(path.equals(skipToFilePath))){
            path = GetNextFilePath();
        }
    }

    //現在の再生状況を取得する
    public int getPlayState(){
        if(!atc.audioTrackIsNull()){
            return atc.playState();
        }
        //取得できなかった時はaudioTrackは停止しているということでｗ
        return AudioTrack.PLAYSTATE_STOPPED;
    }

    public int getMaxLength(){
        return atc.playLength();
    }

    //audioTrackCtrlにMusicPlayerActivityから受け取ったコンテキストとループの初期値を渡す
    public void setProperty(Activity activity, int loop) {
        atc.setATCProperty(activity,loop);
    }

    //ループの設定を変更する
    public void changeProperty(int loop) {
        atc.setLoopProperty(loop);
    }

    //今の再生時間を取得し返す
    public long getNowTime() {
        return atc.playTime();
    }

    //サンプリングレートを取得
    public int getSampleRate(){
        return atc.getRate();
    }

    /**
     * 適用するイコライザの状態設定を更新します。
     * @param enable
     * @param settings
     */
    public void updateEQSetting(boolean enable, Equalizer.Settings settings){
        //　＞＞＞＞atcのメソッドへGO！＜＜＜＜＜
        atc.setEQsettings(enable, settings);
    }

    public void updateEar(int oneEarMode) {
        atc.setEarProperty(oneEarMode);
    }

    public void SetNowFileNull() {
        NowFilePath = null;
    }

    public boolean UnSetAudioTrack(){
        if(!atc.audioTrackIsNull()){
            if(atc.audioTrackStopOperation()){
                atc.unSetAudioTrack();
                return true;
            }
            return false;
        }
        return false;

    }

    public void StopService() {
        atc.finish();
    }


    public int getQueueSize() {
        return pbq.Count();
    }
}

class PlaybackStatus{
    public String FilePath;
    public int volume;
    public String playpausestop;
    public String stereomono;
    public int musiclength;
    //public int position;
    public int seekbar;
    public String playstatus;
}
