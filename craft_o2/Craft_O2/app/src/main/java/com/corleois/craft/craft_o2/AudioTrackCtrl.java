package com.corleois.craft.craft_o2;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaFormat;
import android.media.audiofx.Equalizer;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.corleois.craft.craft_o2.Activities.Interfaces.CoreDecorderNotyfication;
import com.corleois.craft.craft_o2.CoreAudioParts.CoreDecoder;
import com.corleois.craft.craft_o2.CoreAudioParts.MonoralGenerator;
import com.corleois.craft.craft_o2.CoreAudioParts.ChannelConverter;
import com.corleois.craft.craft_o2.CraftLibrary.ApplicationContextHolder;
import com.corleois.craft.craft_o2.EqualizerSettings.EQConfig;
import com.corleois.craft.craft_o2.EqualizerSettings.EQsetting;

import java.util.ArrayList;

import static android.R.drawable.ic_media_next;
import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.R.drawable.ic_media_previous;

public class AudioTrackCtrl extends Service implements AudioTrack.OnPlaybackPositionUpdateListener, CoreDecorderNotyfication {
    //サービスから画面の変更を中継してくれるクラス
    private AudioRelay audioRelay;

    private static AudioTrack audioTrack; //これで音楽を再生する
    private static CoreDecoder decoder;//デコーダー
    public static Equalizer equalizer;//イコライザー
    private static AssetManager assetManager;
    private static AssetFileDescriptor assetFileDescriptor;
    boolean EOF = false;//ファイルの再生が終了していればtrue
    private PlaybackThread thread;//再生するデータを別スレッドで書き込む

    private int loopCheck;//ループ再生の情報を持つ
    private static final int NOT_LOOP = 0;//ループなし
    private static final int ONE_MUSIC = 1;//現在の1曲のみループ
    private static final int ALL_MUSIC = 2;//再生リストの全曲ループ


    /**
     * 0:ステレオ再生
     * 1:片耳右側モノラル
     * 2:片耳左側モノラル
     * 3:両耳モノラル
     */
    private int earSetting;
    private static final int EAR_BOTH = 0;
    private static final int EAR_LEFT = 1;
    private static final int EAR_RIGHT = 2;

    private static Equalizer.Settings temp;//イコライザの適用中の値を格納するクラス

    private Activity activity;//画面から渡されたコンテキストを格納する変数
    private long position = 0;//再生を開始する場所を指定するための変数
    private String filePath;
//    private String nextFilePath;

    //サービス化に必要な宣言
//    private NotificationManager mNM;
    private final IBinder mBinder = new AudioTrackCtrlBinder();

    /**
     * 出力フォーマットが変わったら
     */
    @Override
    public void onOutPutFormatChanged(MediaFormat format) {
        //再生レートを変更する
        if(audioTrack!=null){
            audioTrack.setPlaybackRate(format.getInteger(MediaFormat.KEY_SAMPLE_RATE) * format.getInteger(MediaFormat.KEY_CHANNEL_COUNT));
        }
    }

//    NotificationCompat.Builder builder;

    //↓↓↓ここからサービス化↓↓↓
    // Binderクラスを継承した内部クラス
    public class AudioTrackCtrlBinder extends Binder {
        public AudioTrackCtrl getService(){
            //自分自身を返す
            return AudioTrackCtrl.this;
        }
    }


    //サービスのインスタンス生成時(複数回startServiceを実行した場合初回のみ)
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("AudioService","onCreate");
//        Toast.makeText(getApplicationContext(),"Service起動",Toast.LENGTH_SHORT).show();

        //サービスが初回起動したときnullで初期化
        decoder = null;
        equalizer = null;
        assetFileDescriptor = null;
        audioTrack = null;
    }

    //startService()実行時（複数回実行したら実行した回数分処理される）
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("AudioService", "onStartCommand"+String.valueOf(START_STICKY));
//        Intent intent3 = new Intent(this,MusicPlayerActivity.class);
//
//        String packageName = "com.corleois.craft.crafto2";
//        String className = "com.corleonis.craft.crafto2.MusicPlayerActivity";
//        intent3.setClassName(packageName, className);
//
//        startActivity(intent3);
       showNotificationWithBroadcast(true);

        return START_NOT_STICKY;
    }

    //サービスにバインドされた時
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("AudioService","onBind");
//        Toast.makeText(getApplicationContext(),"Service Bind",Toast.LENGTH_SHORT).show();


//        //サービスをフォアグラウンドでの実行に切り替える
//        startForeground(R.string.app_name,builder.build());

        return mBinder;
//        return _messenger.getBinder();
    }

    //サービスとのバインドが解除された時
    @Override
    public boolean onUnbind(Intent intent){
        Log.d("AudioService","onUnBind");
//        Toast.makeText(getApplicationContext(),"Service unBind",Toast.LENGTH_SHORT).show();
        return true;
    }

    //サービスと再びバインドされた時
    @Override
    public void onRebind(Intent intent) {
        Log.d("AudioService","onReBind");
        super.onRebind(intent);
    }

    //サービスが終了するときに呼び出される
    @Override
    public void onDestroy(){
        Log.d("AudioService","onDestroy");
        if(audioTrack != null) {
            audioTrack.stop();
            audioTrack.flush();
            audioTrack.release();
        }
        unSetAudioTrack();
        assetManager = null;
        position = 0;
        threadOff();
        thread = null;
//        Toast.makeText(getApplicationContext(),"Serviceが停止しました",Toast.LENGTH_SHORT).show();
    }

    //通知を出してフォアグラウンド再生をする
    private void showNotificationWithBroadcast(boolean b){
        String artist = "Craft_O2";
        String title = "Choose play track...";
        if(filePath != null && !filePath.equals("")){
        SelectMusicInfoFromDB selection = new SelectMusicInfoFromDB(filePath,activity);
            artist = selection.getArtistInfo();
            title = selection.getTitleInfo();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ApplicationContextHolder.getApplicationContext());

        Notification notification;
        //もし、再生中ならtrue
        if (!audioTrackIsNull() && playState() == AudioTrack.PLAYSTATE_PLAYING) {
             notification = builder
                    .setTicker("Craft_O2")
                    .setContentTitle(artist)
                    .setContentText(title)
                    .setSmallIcon(ic_media_play)
                    .addAction(ic_media_previous,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_PREVIOUS))
                    .addAction(ic_media_pause,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_PAUSE))
                    .addAction(ic_media_next,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_NEXT))
                    .setContentIntent(getPendingIntentWithBroadcast(NotificationReceiver.CLICK_NOTIFICATION))
                    .setDeleteIntent(getPendingIntentWithBroadcast(NotificationReceiver.DELETE_NOTIFICATION))
                    .build();
        }else {
            //それ以外ならこっち
            notification = builder
                    .setTicker("Craft_O2")
                    .setContentTitle(artist)
                    .setContentText(title)
                    .setSmallIcon(ic_media_play)
                    .addAction(ic_media_previous,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_PREVIOUS))
                    .addAction(ic_media_play,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_PLAY))
                    .addAction(ic_media_next,"",getPendingIntentWithBroadcast(NotificationReceiver.CLICK_NEXT))
                    .setContentIntent(getPendingIntentWithBroadcast(NotificationReceiver.CLICK_NOTIFICATION))
                    .setDeleteIntent(getPendingIntentWithBroadcast(NotificationReceiver.DELETE_NOTIFICATION))
                    .build();
        }


        NotificationManager manager = (NotificationManager) ApplicationContextHolder.getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
        manager.notify(R.string.app_name, notification);

        //Fragment側の再生ボタンも更新しちゃう
        callPlayStopChanged();

        //サービスをフォアグラウンドでの実行に切り替える
        if(b) {
            Log.d("setForeGround","フォアグラウンド再生にする");
            startForeground(R.string.app_name, notification);
        }
    }

    /**
     * 再生・停止の状態が変わったことを、AudioRelayを介して、画面に伝えます
     */
    public void callPlayStopChanged(){
        //nullならバイバイ
        if (audioRelay == null) {
            return;
        }
        audioRelay.notifyPlayStateChangeToFragment();
    }

    //通知のアクション
    private PendingIntent getPendingIntentWithBroadcast(String action){
        return PendingIntent.getBroadcast(ApplicationContextHolder.getApplicationContext(),0,new Intent(action),0);
    }

    //↑↑↑サービス化ここまで↑↑↑

    ///ここから再生の制御///
    //再生開始する処理
    public boolean audioTrackStartOperation() {
        //再生・一時停止ボタンに関する操作
  //      Log.d("logstate", String.valueOf(audioTrack.getPlayState()));
        //再生が止まっているとき,またはaudioTrackがnullの時
//        Log.d("nextnext", String.valueOf(audioTrack.getState()));
        if (audioTrack == null || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
            //audioTrackがnullならば
            if(audioTrack == null){
                //audioTrackを作成し、再生する曲をセットする

                //何か問題があればtrue
                if(!setAudioTrack(filePath)){
                    //ファイルが開けなかったときは開けるまで次の曲を順に試す
                    int tryCount = 1;
                    int countSize = audioRelay.getSize();
                    do{
                        filePath = audioRelay.RelayGetPath();
                        if(filePath == null){
                            continue;
                        }
                        tryCount++;
                    }while(setAudioTrack(filePath) || tryCount > countSize);
                    if(tryCount > countSize){
                        //エラー処理
                        return false;
                    }

                }
            }
            //前の曲の再生が終了していれば
            if(EOF){
                //再生終了のフラグと再生ポジションを初期値に戻す
                EOF = false;
                position = 0;
                //スレッドを停止
                threadOff();
            }


            //再生可能な状態にする
            audioTrack.play();

            //スレッドを作成
            if(thread == null) {
                threadOn();
                //再生を開始する
                thread.start();
                Log.d("AudioTrackControl290","スレッドを作成");
            }else{
                Log.d("AudioTrackControl292","スレッドは作成済み");
            }

            //一時停止されていたりで再生ポジションが初期値ではなかった場合
            if(position > 0){
                //指定された時間にシークさせる
                decoder.seekTo(position);
            }

            showNotificationWithBroadcast(false);
            audioRelay.notifyPlayStateChangeToFragment();
            //リスナーを解除
//            audioTrack.setPlaybackPositionUpdateListener(null);
            //リスナーをセット
//            setListener();
            return true;
        }
        return false;
    }

    //再生停止する処理
    public boolean audioTrackStopOperation() {
        //再生がされているとき
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            //現在の再生時間を取得
            position = decoder.getSampleTime();
            Log.d("positionTime", String.valueOf(position));
//            Log.d("position", String.valueOf(audioTrack.getPositionNotificationPeriod()));

            //再生を停止
            audioTrack.stop();
            Log.d("logstate", String.valueOf(audioTrack.getPlayState()));
            showNotificationWithBroadcast(false);
            audioRelay.notifyPlayStateChangeToFragment();

            //現在のスレッドを破棄
            threadOff();
            return true;
        }
        return false;
    }

    //再生位置を変更された時の処理
    public boolean  audioTrackSeekOperation(long setPosition){
        //シーク先の時間を格納
        position = setPosition;
        Log.d("loopSeek", String.valueOf(position));
        //再生中の場合
        if(AudioTrack.PLAYSTATE_PLAYING == playState()) {
            //シークさせる
            audioTrack.stop();
            audioTrack.play();
            decoder.seekTo(position);
        }
        //一時停止中の場合は再生ボタンを押した時にシークしてくれる
        return true;
    }

    //audioTrackのリスナーを登録
    private void setListener() {

        audioTrack.setPlaybackPositionUpdateListener(this);
        //マーカー位置をフレーム単位数で指定(再生するmByteArray.lengthをステレオ（2）＊　16bit（2）で割る)
        //メタデータから再生時間(ms)を取得できれば（久留島のコーディング部分）と結合予定
//        audioTrack.setNotificationMarkerPosition((int) assetFileDescriptor.getLength());

        //通知の更新周期をフレーム数で指定（再生楽曲のサンプリングレートを引数で渡す）
        audioTrack.setPositionNotificationPeriod(decoder.getSampleRate()/2);
    }

    //スレッド作成
    private void threadOn() {
        //何かスレッドが生きてたら
        threadOff();
        thread = new PlaybackThread();
    }
    //スレッドを破棄
    private void threadOff(){
        //何かスレッドが生きてたら
        if (thread != null) {
            thread.abort();
            thread = null;
        }
    }


    /**
     * audioTrackの生成、再生する曲のセット
     * 返り値は、ファイルを正常に開けたかどうかです
     * @param file
     * @return なんらかの問題が発生した:false 開くことができた:true
     */
    public boolean setAudioTrack(String file){
        Log.d("getFilePath!","ﾊﾟｽは渡されたよ！");

//        Log.d("playfile",file);
        position = 0;
        //再生する曲のファイルﾊﾟｽを格納
        filePath = file;

        try {
             //assetの音声データを開く
//            assetFileDescriptor = assetManager.openFd(filePath);
//            decoder = new CoreDecoder(assetFileDescriptor);


            decoder = new CoreDecoder(filePath);
            //テストで認識できた最も最初のトラックを選択する
            //内部で同じ初期化はされているのでしなくてもいいけど一応テストで
            decoder.selectTrackNumber(decoder.getMaxValidTrackNumber());

            //デコード処理開始
            decoder.startDecode();

//            if(!audioTrackIsNull()) {
//                audioTrack.stop();
//                audioTrack.flush();
//
//            }

            //AudioTrackも準備
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,//再生するAudioTrackがどのストリームに属するか指定
                    decoder.getSampleRate(),//サンプリングレート数を指定
                    AudioFormat.CHANNEL_OUT_STEREO,  //モノラルかステレオか、それ以外か
                    AudioFormat.ENCODING_PCM_16BIT,//1サンプル当たりのデータサイズ
                    AudioTrack.getMinBufferSize(decoder.getSampleRate(), 2, AudioFormat.ENCODING_PCM_16BIT),
                    AudioTrack.MODE_STREAM
            );
            audioTrack.setPlaybackRate(decoder.getSampleRate());

            //イコライザオブジェクト作成
            if(equalizer != null) equalizer.release();
            equalizer = new Equalizer(0, audioTrack.getAudioSessionId());

            if(temp != null){
                setEQsettings(true,temp);
            }else {
                getEQsettings();
            }

        } catch (Exception e) {
            //例外が起きたら

            Log.d("NotOpenFile", "ファイルを開けなかったです");
            e.printStackTrace();
            return false;
        }


        return true;
    }

    //セットした内容を破棄
    public void unSetAudioTrack(){
        decoder = null;
//        audioTrack.setPlaybackPositionUpdateListener(null);
        if(audioTrack != null) {
            audioTrack.release();
        }
        audioTrack = null;
        position = 0;

        //イコライザオブジェクトが使われていたらtrue
        if(equalizer != null) {
            //イコライザをOFFにする
            equalizer.setEnabled(false);
            equalizer = null;
        }
    }

    /**
     * デコードするスレッドオブジェクトです。
     * こうすることで個別のスレッド管理ができるようになります
     */
    private class PlaybackThread extends Thread{
        private boolean abort;

        public PlaybackThread(){
            abort = false;
        }

        /**
         * ここのアルゴリズムに大きな変更はありません
         * 強いて言えば、中断処理が追加されたくらいです
         */
        //再生を実行する部分
        @Override
        public void run(){
            Log.d("thread","別スレッドで処理を実行します");
            try {
                //若干優先度高めで
                setPriority(6);
                //再生の処理を行う
                do {
                    //audioTrackの状態が停止中、一時停止中のとき
                    if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED || audioTrack.getPlayState() == AudioTrack.PLAYSTATE_STOPPED) {
                        //もし、スレッド終了命令が来ていたらtrue
                        if(abort){
                            break;
                        }else {
                            thread.sleep(500);
                            continue;
                        }
                    }

                    //Log.d("loop", String.valueOf(decoder.getSampleTime()));

                    EOF = decoder.pushDataToDecoder(1000L);
    //                Log.d("CoreAudioTest","EOF = " +(EOF));
                    byte[] decodedData;
                    decodedData = decoder.pullDataFromDecoder(1000L);

                    //入力バッファが満たされていないと入ってこないみたいなので、nullのときはもうちょいpushする
                    if (decodedData == null) {
                        continue;
                    }

                    if (decodedData.length > 0) {

                        //モノラル音声の場合はステレオ化する
                        if(decoder.getChannels() == 1){
                            decodedData = ChannelConverter.ConvMonoToStereo(decodedData);
                        }

                        /**
                         * 0:ステレオ再生
                         * 1:片耳左側モノラル
                         * 2:片耳右側モノラル
                         * 3:両耳モノラル
                         */
                        byte[] deco2 = null;
                        switch (earSetting){
                            case EAR_BOTH:
                                deco2 = decodedData;
                                break;
                            case EAR_LEFT:
                                deco2 = MonoralGenerator.StereoToMonoral(decodedData,EAR_LEFT);
                                break;
                            case EAR_RIGHT:
                                deco2 = MonoralGenerator.StereoToMonoral(decodedData,EAR_RIGHT);
                                break;

                            //何かあったらこっち
                            default:
                                deco2 = decodedData;
                        }

                        audioTrack.write(deco2, 0, deco2.length);
                    }

                    //現在の曲の再生が終了
                    if(EOF){
                        //再生開始位置、decoder、audioTrackをリセットする
                        decoder.dispose();
                        unSetAudioTrack();
                        position = 0;
                        //1曲ループ以外のとき次に再生する曲を取得
                        if(!(loopCheck == ONE_MUSIC)){
                            filePath = audioRelay.RelayGetPath();
    //                        Log.d("threadLog", "The Same File Set");
                        }

                        //filePathに次に再生する曲が入っている時
                        if(filePath != null) {
                            //次の曲の再生を開始する
                            if(!setAudioTrack(filePath)){
                                //ファイルが開けなかったときは開けるまで次の曲を順に試す
                                int tryCount = 1;
                                int countSize = audioRelay.getSize();
                                do{
                                    filePath = audioRelay.RelayGetPath();
                                    if(filePath == null){
                                        continue;
                                    }
                                    tryCount++;
                                }while(setAudioTrack(filePath) || tryCount > countSize);
                                if(tryCount > countSize){
                                    EOF = true;
                                }else{
                                    EOF = false;
                                    decoder.startDecode();
                                    audioTrack.play();
                                    showNotificationWithBroadcast(false);
                                    //                        setListener();
                                    Log.d("threadLog", "Next File Set");
                                }
                            }else {
                                EOF = false;
                                decoder.startDecode();
                                audioTrack.play();
                                showNotificationWithBroadcast(false);
                                //                        setListener();
                                Log.d("threadLog", "Next File Set");
                            }


                        }
                    }
                } while (!EOF && ! abort);
                //中止による終了でなければこの処理をする
                if(!abort) {
                    Log.d("positionEnd", "File End");
                    finish();
                }
            }catch (Exception ex){
                Log.e("CoreAudio","例外が発生しました:"+(ex.getMessage()));
                ex.printStackTrace();
            }
        }

        /**
         * スレッドに実行中断・終了命令を送ります
         */
        public void abort(){
            abort = true;
        }
    }


    //再生が指定位置()に達したときの処理【現在は使用しておりません。書いておかないといけないのと前の名残です。】
    @Override
    public void onMarkerReached(AudioTrack audioTrack) {
        Log.d("position","file end");
        //再生中の時
        if(audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            //再生を停止する
            audioTrack.stop();
            audioTrack.flush();
            thread = null;
        }
        //ループ再生が有効の時
        if(loopCheck == ONE_MUSIC){
            //
        }else if(loopCheck == ALL_MUSIC){

        }
    }

    //再生中に一定数(サンプリングレート)経過毎に実行する処理
    @Override
    public void onPeriodicNotification(AudioTrack audioTrack) {
        //現在再生時間の表示を変更する要求
//        audioRelay.RelayToMPA(ORDER_TEXT_NOWTIME);
//        audioRelay.RelayToMPA(ORDER_SEEKBAR_POSITION);
    }

    public void sendNextFilePath(String file){
        filePath = file;
    }

    //現在の再生状態を返す
    public int playState(){
        //audioTrackの再生状態を返す
        if(audioTrack == null){
            //汎用エラーを返しておく
            return AudioTrack.ERROR;
        }
        return audioTrack.getPlayState();
    }

    //現在の再生時間
    public long playTime(){
        //今の再生時間を返す
        return decoder.getSampleTime();
    }

    //サンプルレート数を返す
    public int getRate(){
        return decoder.getSampleRate();
    }

    //再生するデータの長さを返す(仮の処理)
    public int playLength(){
        Log.d("positionSend", String.valueOf(assetFileDescriptor.getLength()/decoder.getSampleRate()*1000000));
        return (int) assetFileDescriptor.getLength()/decoder.getSampleRate()*1000000;
    }

    public boolean audioTrackIsNull(){
        if(audioTrack == null){
            return true;
        }
        return false;
    }

    //受け取ったコンテキスト、ループを格納する
    public void setATCProperty(Activity activity, int loop) {
        //コンテキストが渡されているとき
        if(activity != null) {
            //コンテキストを保存
            this.activity = activity;
            //画面への要求を中継してくれるクラス
//            audioRelay = new AudioRelay(activity);
            //Controllerへの要求を中継してくれるクラス
            audioRelay = new AudioRelay(activity);
            assetManager = activity.getApplicationContext().getAssets();
        }
        //初期のループの状態を保存
        loopCheck = loop;
    }

    //ループ設定が変更された時
    public void setLoopProperty(int loop){
        loopCheck = loop;
    }

    /**
     * イコライザに適用する設定を更新します
     * @param enable 有効か無効か
     * @param settings 設定値
     * @return 更新成功したかどうか
     */
    public boolean setEQsettings(boolean enable, Equalizer.Settings settings){

        //一旦値をオブジェクトに入れる
        temp = settings;

        //もしnullなら、セットできないので放置
        if(equalizer == null){
            return false;
        }
//        Log.d("イコライザ設定変更受付",String.valueOf(enable));
//        Log.d("設定値0",String.valueOf(settings.bandLevels[0]));
//        Log.d("設定値1",String.valueOf(settings.bandLevels[1]));
//        Log.d("設定値2",String.valueOf(settings.bandLevels[2]));
//        Log.d("設定値3",String.valueOf(settings.bandLevels[3]));
//        Log.d("設定値4",String.valueOf(settings.bandLevels[4]));
        equalizer.setEnabled(enable);

        for (short i = 0; i < temp.bandLevels.length; i++) {
            equalizer.setBandLevel(i, temp.bandLevels[i]);
        }

        return true;
    }

    /**
     * 現在有効なイコライザの設定を行います
     */
    public void getEQsettings(){
        EQConfig eqConfig = new EQConfig(ApplicationContextHolder.getApplicationContext());
        ArrayList<EQsetting> eQsettings = eqConfig.getAllEQSettings();
        int size = eQsettings.size();
        boolean needEnable = false;
        for (int i = 0; i < size; i++) {
            if(eQsettings.get(i).getEnabled()){
                needEnable = true;
                break;
            }
        }

        setEQsettings(needEnable , eqConfig.getCombinedSettings());
    }

    public void setEarProperty(int oneEarMode) {
        earSetting = oneEarMode;
    }

    /**
     * 再生が終了したら
     */
    public void finish() {
        if(!audioTrackIsNull() && audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrackStopOperation();
        }
        if(!EOF) {
            EOF = true;
        }
        threadOff();
        stopSelf();
        showNotificationWithBroadcast(false);
    }

    /**
     * 使用中のAudioRelayオブジェクトを返します
     * @return
     */
    public AudioRelay getAudioRelay(){
        return audioRelay;
    }
}
