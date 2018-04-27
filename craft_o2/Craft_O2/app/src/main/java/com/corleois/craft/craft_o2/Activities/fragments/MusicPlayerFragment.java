package com.corleois.craft.craft_o2.Activities.fragments;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.corleois.craft.craft_o2.Activities.EqualizerSettingsActivity;
import com.corleois.craft.craft_o2.Activities.Interfaces.ColorChangeListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.DatabaseUpdateNotifyListener;
import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Activities.TagSettingActivity;
import com.corleois.craft.craft_o2.AudioRelay;
import com.corleois.craft.craft_o2.AudioTrackCtrl;
import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.CraftLibrary.ImageCache;
import com.corleois.craft.craft_o2.MusicPlayerMotionController;
import com.corleois.craft.craft_o2.PlaybackQueue.PlayOrder;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.SelectMusicInfoFromDB;

import java.text.DecimalFormat;
import java.util.EnumSet;

import static android.R.drawable.ic_media_pause;
import static android.R.drawable.ic_media_play;
import static android.content.Context.MODE_PRIVATE;


/**
 * Created by corleois on 2017/06/12.
 */



public class MusicPlayerFragment extends Fragment implements Runnable, Animation.AnimationListener, ColorChangeListener, DatabaseUpdateNotifyListener {
    private Activity activity;
    private View rootView;

    private ImageButton buttonSP,buttonNext,buttonPrev,LoopCheck,ShuffleCheck; //各種MediaPlayerに対する操作を行うボタン
    private TextView nowTimeText,maxTimeText,TitleText,ArtistText;
    private SeekBar seekBar;
    private ImageView artWork;
    private Switch privateSwich;
    private RelativeLayout relativeLayout;
    private LinearLayout unit;

    private String Title,Artist = "";
    private int playTime = 0;
    private String imgPath = "";

    private LinearLayout oneEarMode_view;
    private int oneEarMode;
    private ImageView leftEar;
    private ImageView rightEar;
    private TextView equalizer;
    private TextView tagEdit;

    private String controlOrder;//コントローラにオーダーする種別
    private boolean playing = false;//停止中：false　再生中：true
    private boolean tempPlaying = false;//再生状態を退避させる場所
    public String NowFilePath = "";//再生中のファイルパス
    public String skipToFilePath = null;//リストから受け取ったスキップ先のファイルパス
    private int countPlayingTime = 0;//再生時間をカウントする
    private boolean running = false;//スレッドが動いているかどうか
    private Thread thread = new Thread(this);//画面の状態を逐一更新させるスレッド

    private boolean PrivateMode = false;//プライベートモードのオンオフを保持する変数

    private static final int DURATION = 800;//アニメーションの実行時間

    private int loop;//ループの状態を保持
    private static final int NOT_LOOP = 0;//ループなし
    private static final int ONE_MUSIC = 1;//現在の1曲のみループ
    private static final int ALL_MUSIC = 2;//再生リストの全曲ループ

    public String AlbumQueue = "";
    public String ArtistQueue = "";
    public boolean SongQueue = false;

    Controller ctr;  //コントローラのオブジェクト
    AudioTrackCtrl atc;//再生制御のオブジェクト
    private MusicPlayerMotionController motionController;   // 再生画面のスワイプ用のコントローラ

    private AudioRelay audioRelay;

    //画面の表示を変更するハンドラー用の変数
    private static final int CHANGE_PLAYINGTIME = 1;
    private static final int CHANGE_PLAYTIME = 2;
    private static final int CHANGE_MUSIC_INFO = 0;
    private static final int CHANGE_SEEK_POSITION = 3;
    private static final int CHANGE_SEEK_MAX = 4;
    private static final int CHANGE_BUTTON = 5;
    static private final int PRIVATE_ON = 6;
    static private final int PRIVATE_OFF = 7;
    public static final int NOTIFY_PLAYSTATE = 8;

    //AudioRelayを介してサービスからの画面表示変更要求を受け取り処理する
    public Handler handler = new Handler() {
        public void handleMessage(Message msg){
            switch (msg.what) {
                case CHANGE_MUSIC_INFO:

                    //Fragmentが正常にアクティビティにアタッチされていたら
                    //再生中の曲の情報を更新する
                    if(isAdded()) {
                        changeMusicInfo(NowFilePath);
                        changePlayingTime(0);
                    //もし、アクティビティに追加されていたらtrue
                        setButtonText(playing);
                    }
                    break;

                case CHANGE_PLAYINGTIME:
                    //再生中の曲の現在再生時間（テキストとシークバー）を更新
                    changePlayingTime(countPlayingTime);
                    break;

                case CHANGE_PLAYTIME:
                    //CHANGE_MUSIC_INFOを通れば更新されてるけどね。呼ばれてないけどねｗ
                    changePlayTime(countPlayingTime);
                    break;

                case CHANGE_BUTTON:
                    //再生状況に応じてボタンの表示を変更する
                    setButtonText(playing);
                    break;

                case PRIVATE_ON:
                    PrivateMode = true;
//                    privateChange(PrivateMode);
                    break;
                case PRIVATE_OFF:
                    PrivateMode = false;
//                    privateChange(PrivateMode);
                    break;

                case NOTIFY_PLAYSTATE:
                    //再生に関する通知が来た場合
                    //コントローラがnullでなかったら
                    if(ctr != null){
                        //再生状態が、再生中ならtrue、それ以外はfalse
                        setButtonText(ctr.getPlayState() == AudioTrack.PLAYSTATE_PLAYING);
                    }else {
                        //コントローラがnullだったら、再生できないとみなしてfalse
                        setButtonText(false);
                    }
                    break;

            }
        }
    };


    //【↓↓↓サービス化↓↓↓】
    //サービスへの接続状態を管理する変数
    private boolean connectionState = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            //サービス接続時に呼ばれる
            Log.d("Service","connect");
            //BinderからServiceのインスタンスを取得
            atc = ((AudioTrackCtrl.AudioTrackCtrlBinder)iBinder).getService();
            audioRelay = atc.getAudioRelay();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            //サービス切断（異常）時に呼ばれる
            Log.d("Service","disconnect");
            atc = null;
            audioRelay = null;
        }
    };

    //【↑↑↑サービス化↑↑↑】


    /**
     * 再生の制御に使用するコントローラオブジェクトをセットします。
     * @param ctr
     */
    public void setCtr(Controller ctr) {
        this.ctr = ctr;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        //コントローラを獲得
        ctr = Controller.getController();

        rootView = inflater.inflate(R.layout.fragment_music_player, container, false);

        //音量ボタンに音量調節を任せる
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        //初期のループセットをしておく
        PlaybackQueue.SetPlayOrder(EnumSet.of(PlayOrder.LOOP_OFF));
        //コンテキスト,ループの値を渡す
        ctr.setProperty(activity,loop);

        //【↓↓↓サービス化↓↓↓】
        //サービス起動
        activity.startService(new Intent(activity.getApplicationContext(),AudioTrackCtrl.class));
        //【↑↑↑サービス化↑↑↑】

        //[再生/一時停止]ボタン
        buttonSP = (ImageButton)rootView.findViewById(R.id.playButton);
        buttonSP.setOnClickListener(new View.OnClickListener(){
//        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    Toast.makeText(activity, "キューに再生できる楽曲がありません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //ループ設定なしでキューの最後の曲の再生が終了した時点で再生ボタンを押した時の処理
                if(!playing) {
                    String now = nowTimeText.getText().toString();
                    String max = maxTimeText.getText().toString();
                    if (NowFilePath.equals(ctr.GetNowFilePath()) && now.equals(max)) {
                        Log.d("MusicPlayerFragment","Play Same Song");
                        countPlayingTime = 0;
                        changePlayingTime(countPlayingTime);
                    }
                }

                //ボタンの回転・表示変更・ボタンの機能実行
                float toDegrees;
                if(playing) {
                    toDegrees = 90.0f;
                }else{
                    toDegrees = 130.0f;
                }
                startRotateAnim(toDegrees);

            }
        });

        //[次の曲]ボタン
        buttonNext = (ImageButton)rootView.findViewById(R.id.nextButton);
        buttonNext.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    Toast.makeText(activity, "キューに再生できる楽曲がありません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //次の曲の再生要求（引数　T:前の曲/F:次の曲）
                if(skipToFilePath!=null){
                    if(NowFilePath.equals(skipToFilePath)){
                        //同じパスだと何もしない処理をする
                        ctr.SkipFilePath(skipToFilePath);
                        skipToFilePath = null;
                        Toast.makeText(activity,"現在再生中です",Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if(ctr.playNextPrevTrack(false,skipToFilePath)) {
                    Log.d("同じ曲を再生","skipToFilePathはnull");

                    tempPlaying = playing;
                    playing = false;

                    skipToFilePath = null;
                    playing = tempPlaying;
                    countPlayingTime = 0;
//                    ctr.OrderFromPlayScreen("OrderStart");

                    if(ctr.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
                        playing = true;
                    }
                    if(!running) {
                        running = true;
                        thread.start();
                    }
                }else{
                    Toast.makeText(activity,"再生できません",Toast.LENGTH_SHORT).show();
                }
            }
        });

        //[前の曲]ボタン
        buttonPrev = (ImageButton)rootView.findViewById(R.id.prevButton);
        buttonPrev.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    Toast.makeText(activity, "キューに再生できる楽曲がありません。", Toast.LENGTH_SHORT).show();
                    return;
                }

                //現在の再生状況を退避
                tempPlaying = playing;
                playing = false;
                //現在の再生時間の判定
                if(countPlayingTime > 5000){
                    //5秒より大きい場合は先頭にシーク
                    if(ctr.OrderFromPlayScreen("OrderSeekTo",0)){
                        countPlayingTime = 0;
                    }
                }else {
                    //前の曲の再生要求（引数　T:前の曲/F:次の曲）
                    if (ctr.playNextPrevTrack(true, null)) {
                        //
                        skipToFilePath = null;
                    } else {
                        Toast.makeText(activity, "再生できません", Toast.LENGTH_SHORT).show();
                    }
                }
                playing = tempPlaying;
            }
        });

        seekBar = (SeekBar)rootView.findViewById(R.id.seekBar);
        seekBar.setProgress(0);//シークバーの初期値を0に設定
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }

                //初回再生前にシークバーが動かされた時
                countPlayingTime = i;
                changePlayingTime(countPlayingTime);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生何もしない
                    return;
                }

                //シークバーに触れたときの処理
                //別で動いてるスレッドの秒のカウントを止める
                tempPlaying = playing;
                playing = false;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //シークバーの現在の位置から再生を開始する
                //再生キューに楽曲がなかったら
                if(PlaybackQueue.Count() == 0){
                    //再生できるわけがないので何もしない
                    return;
                }
                if(seekBar.getMax() > 0) {
                    if (!ctr.OrderFromPlayScreen("OrderSeekTo", seekBar.getProgress())) {
                        Toast.makeText(activity, "シークに失敗しました。", Toast.LENGTH_SHORT).show();
                    }
                }
                playing = tempPlaying;
            }
        } );

        //ループ再生のボタン
        LoopCheck = (ImageButton)rootView.findViewById(R.id.loopButton);
        LoopCheck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                switch (loop){
                    case NOT_LOOP:
                        loop = ONE_MUSIC;
                        ((ImageButton) rootView.findViewById(R.id.loopButton)).setImageResource(R.drawable.replay_one);
                        Toast.makeText(activity.getApplication(),"現在の曲を繰り返しています",Toast.LENGTH_SHORT).show();

                        break;
                    case ONE_MUSIC:
                        loop = ALL_MUSIC;
                        ((ImageButton) rootView.findViewById(R.id.loopButton)).setImageResource(R.drawable.replay_all);
                        Toast.makeText(activity.getApplication(),"すべての曲を繰り返しています",Toast.LENGTH_SHORT).show();
                        PlaybackQueue.SetPlayOrder(EnumSet.of(PlayOrder.LOOP_ALL));
                        break;
                    case ALL_MUSIC:
                        loop = NOT_LOOP;
                        ((ImageButton) rootView.findViewById(R.id.loopButton)).setImageResource(R.drawable.replay_off);
                        Toast.makeText(activity.getApplication(),"繰り返しはOFFです",Toast.LENGTH_SHORT).show();
                        PlaybackQueue.SetPlayOrder(EnumSet.of(PlayOrder.LOOP_OFF));
                        break;
                }


                ctr.changeProperty(loop);

            }
        });

        //シャッフルボタン
        ShuffleCheck = (ImageButton)rootView.findViewById(R.id.shuffButton);
        ShuffleCheck.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //シャッフルボタンが押された時の処理
                //シャッフル再生用の配列を使用する処理

                //シャッフル再生のオーダーをトグルして、トーストでチンしちゃう
                //EnumSet<PlayOrder> order = PlaybackQueue.GetPlayOrder();
                EnumSet<PlayOrder> order = PlaybackQueue.GetPlayOrder();

                String text = "";    //ONになったかOFFになったかをトースト表示するための変数

                //取ってきた現在の再生モードに、シャッフル再生が含まれてたらTrue
                if (order.contains(PlayOrder.SHUFFLE)) {
                    //ONなのでOFFをセットする
                    order.remove(PlayOrder.SHUFFLE);
                    PlaybackQueue.SetPlayOrder(order);
                    text = "OFF";

                    //ボタンの画像を変えちゃう
                    ((ImageButton) rootView.findViewById(R.id.shuffButton)).setImageResource(R.drawable.shuffle1_off);

                } else {
                    //OFFだったので、ONにセットする
                    order.add(PlayOrder.SHUFFLE);
                    PlaybackQueue.SetPlayOrder(order);
                    text = "ON";

                    //ボタンの画像を変えちゃう
                    ((ImageButton) rootView.findViewById(R.id.shuffButton)).setImageResource(R.drawable.shuffle1);
                }
                //トースト表示
                Toast.makeText(activity, "シャッフル：" + text, Toast.LENGTH_SHORT).show();
                //キューリストの描画も更新
                ((MusicSelectionActivity) getActivity()).queueFragmentUpdate();
            }
        });

        //現在の再生時間を表示するテキストビュー
        nowTimeText = (TextView)rootView.findViewById(R.id.nowTimeTextView);
        maxTimeText = (TextView)rootView.findViewById(R.id.maxTimeTextView);
        TitleText = (TextView)rootView.findViewById(R.id.title);
        ArtistText = (TextView)rootView.findViewById(R.id.artist);
        artWork = (ImageView)rootView.findViewById(R.id.albumArt_current);

        if(ctr.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
            playing = true;
            setButtonText(playing);
            running = true;
            thread.start();
        }

        // [片耳モード]項目
        oneEarMode_view = (LinearLayout)rootView.findViewById(R.id.one_ear_mode);
        oneEarMode_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (oneEarMode) {
                    case 0:
                        //0(両耳モード)の時、1(右耳モード)に
                        leftEar.setVisibility(View.INVISIBLE);
                        break;
                    case 1:
                        //1(右耳モード)の時、2(左耳モード)に
                        leftEar.setVisibility(View.VISIBLE);
                        rightEar.setVisibility(View.INVISIBLE);
                        break;
                    case 2:
                        //2(左耳モード)の時、0(両耳モード)に
                        rightEar.setVisibility(View.VISIBLE);
                        break;
                }
                /*
                 *0:両耳モード
                 *1:左耳モード
                 *2:右耳モード
                 */
                oneEarMode = (oneEarMode + 1) % 3;
                ctr.updateEar(oneEarMode);

                //O2が終了しても設定を維持できるよう、プレファレンスで処理する
                SharedPreferences write =getActivity().getSharedPreferences("commonSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = write.edit();
                editor.putInt("oneEar", oneEarMode);
                editor.apply();
                //保存完了

            }
        });
        oneEarMode = 0;
        leftEar = (ImageView)rootView.findViewById(R.id.ear_left);
        rightEar = (ImageView)rootView.findViewById(R.id.ear_right);

        //プライベートモード
        privateSwich = (Switch)rootView.findViewById(R.id.private_mode);
        privateSwich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                //プライベートモードを切り替える
                privateChange(b);

                //O2が終了しても設定を維持できるよう、プレファレンスで処理する
                SharedPreferences write = getActivity().getSharedPreferences("commonSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = write.edit();
                editor.putBoolean("private", b);
                editor.apply();
                //保存完了

            }
        });

        // [イコライザー]項目
        equalizer = (TextView) rootView.findViewById(R.id.equalizer);
        equalizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //イコライザー設定画面の呼び出し
                Intent intent = new Intent(activity, EqualizerSettingsActivity.class);
                startActivity(intent);
            }
        });

        // [タグ編集]項目
        tagEdit = (TextView)rootView.findViewById(R.id.tagEdit);
        tagEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //タグ編集画面の呼び出し
                Intent intent = new Intent(activity,TagSettingActivity.class);
                intent.putExtra("filePath",NowFilePath);
                startActivity(intent);
            }
        });

        relativeLayout = (RelativeLayout)rootView.findViewById(R.id.gestureArea_albumArt);
        unit = (LinearLayout) rootView.findViewById(R.id.unit_seekBar);


        //サービス側の状態変更を通知してもらえるように、ハンドラを設定する
        //AudioRelayがあったなら、通知してもらうように設定
//        Toast.makeText(activity, "せってーした！", Toast.LENGTH_SHORT).show();
        AudioRelay.resistPlayStateChangeReceiveHandler(handler);

        return rootView;
    }



    /**
     * Activityが表示された時にActivityから呼ばれる
     *
     * @param hasFocus
     */
    public void onWindowFocusChanged(boolean hasFocus) {

        // 再生画面にスワイプ操作用のコントローラをセット
        motionController = new MusicPlayerMotionController(activity, rootView, MusicPlayerMotionController.ScreenMode.MINI);
        LinearLayout playerContainer = (LinearLayout)activity.findViewById(R.id.playerContainer);
        playerContainer.setOnTouchListener(motionController);
        playerContainer.setOnDragListener(motionController);
        //色変化のリスナーもつけておく
        motionController.setChangeListener(this);
    }




    //ボタンが押された時の処理（戻る[🔙]ボタンが押された時の処理が実行されないようにしてある）
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode != KeyEvent.KEYCODE_BACK){
//            super.onKeyDown(keyCode, event);
//            return true;
//        }else{
//            //ここに遷移先アクティビティを呼び出す、あるいは現在のアクティビティを終了させる記述を書く？【予想】
//            return false;
//        }
//    }


//バインドさせなくても操作できちゃうｗコントローラ使ってるからやろうけど
    //【サービスのバインド処理】
    private void onConnect(){
        //サービスをバインドし操作可能にする
        if(!connectionState) {
            activity.bindService(new Intent(activity, AudioTrackCtrl.class), connection, Context.BIND_AUTO_CREATE);
            //サービスとの接続状況（true：接続）
            connectionState = true;
        }
    }

    //【サービスのアンバインド処理】
    private void unConnect(){
        //サービスに接続中していたら切断
        if(connectionState) {
            activity.unbindService(connection);
            //サービスとの接続状況（false：切断）
            connectionState = false;
        }
    }

    //画面が破棄されるときの処理
    @Override
    public void onDestroy(){
        if(connectionState) {
            activity.unbindService(connection);
            connectionState = false;
        }
        if(!playing) {
           activity.stopService(new Intent(activity,AudioTrackCtrl.class));
        }


        super.onDestroy();
    }

    private void startRotateAnim(float toDegrees){

        //[再生/一時停止]ボタンの回転アニメーション
        RotateAnimation rotate = new RotateAnimation(0.0f, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(DURATION);       // アニメーションにかける時間(ミリ秒)
        rotate.setFillAfter(false);          // アニメーション表示後の状態を保持
        buttonSP.startAnimation(rotate);    // アニメーション開始
        rotate.setAnimationListener(this);
    }


    //ボタンの表示を変更し、操作要求を決定する
    private void setButtonText(boolean playing) {
        this.playing = playing;
        if(playing){
            //音楽再生中にボタンを押したときの処理
            //停止された後に表示を「||」に変える
            if(!buttonSP.getDrawable().equals(ic_media_pause)) {
                buttonSP.setImageDrawable(getResources().getDrawable(ic_media_pause, getActivity().getTheme()));
                Log.d("再生ボタン","||に変えた");
            }
        }else{
            //音楽が停止された後に表示を変えるので「▶」を表示する
            if(!buttonSP.getDrawable().equals(ic_media_play)) {
                buttonSP.setImageDrawable(getResources().getDrawable(ic_media_play, getActivity().getTheme()));
                Log.d("再生ボタン","▶に変えた");
            }
        }
    }

    private void changeMusicInfo(String filePath){
        //タイトル,アーティスト,再生時間を取得し反映させる
        SelectMusicInfoFromDB selectMusicInfo = new SelectMusicInfoFromDB(filePath,activity);
        Title = selectMusicInfo.getTitleInfo();
        Artist = selectMusicInfo.getArtistInfo();
        playTime = selectMusicInfo.getPlayTimeInfo();
        imgPath = selectMusicInfo.getImgPath();

        //プライベートモードがonの場合納言さん表示
        if(PrivateMode){
            artWork.setImageDrawable(getResources().getDrawable(R.drawable.disc_512,activity.getTheme()));
            changePaletteEnable(null);
        }else {
            if (imgPath.equals("")) {
                artWork.setImageDrawable(getResources().getDrawable(R.drawable.disc_512, activity.getTheme()));
                changePaletteEnable(null);
            } else {

//                Bitmap bitmap = ImageCache.getImage(imgPath);
//                if(getView() != null){
//                    int viewWidth = getView().getWidth();
//                    if(bitmap.getWidth() >= viewWidth) {
//                        int viewHeight = getView().getHeight();
//                        double ratio = viewWidth / viewHeight;
//                        bitmap = Bitmap.createScaledBitmap(bitmap, viewWidth, (int)(viewWidth * ratio), true);
//                    }
//                }

//                artWork.setImageBitmap(bitmap);
                
                Bitmap bitmap = ImageCache.getImage(imgPath);

                artWork.setImageBitmap(bitmap);

                //ミニプレイヤーモードでないなら色を変える
                if(motionController != null) {
                    MusicPlayerMotionController.ScreenMode screenMode = motionController.getScreenMode();
                    //ミニプレイヤー状態ではないなら色変更
                    if(screenMode != MusicPlayerMotionController.ScreenMode.MINI){
                        changePaletteEnable(((BitmapDrawable) artWork.getDrawable()).getBitmap());
                    }else {
                        //色を戻しておく
                        changePaletteEnable(null);
                    }
                }

            }
        }

        TitleText.setText(Title);
        ArtistText.setText(Artist);
        changePlayTime(playTime);

        //そして、他のFragmentの画面の再描画をお願いする
        ((MusicSelectionActivity) getActivity()).refreshAllPageFragments();

    }

    public void privateChange(boolean b) {
        PrivateMode = b;

        //プライベートモードがonの場合納言さん表示
        if(PrivateMode){
            artWork.setImageDrawable(getResources().getDrawable(R.drawable.disc_512,activity.getTheme()));
            changePaletteEnable(null);
        }else {
            if (imgPath.equals("")) {
                artWork.setImageDrawable(getResources().getDrawable(R.drawable.disc_512, activity.getTheme()));
                changePaletteEnable(null);
            } else {
                //ミニプレイヤーモードのときは変更せずにバイバイ
                if(motionController != null && motionController.getScreenMode() == MusicPlayerMotionController.ScreenMode.MINI){
                    return;
                }

//                Bitmap bitmap = ImageCache.getImage(imgPath);
//                if(getView() != null){
//                    int viewWidth = getView().getWidth();
//                    if(bitmap.getWidth() >= viewWidth) {
//                        int viewHeight = getView().getHeight();
//                        double ratio = viewWidth / viewHeight;
//                                bitmap = Bitmap.createScaledBitmap(bitmap, viewWidth, (int)(viewWidth * ratio), true);
//                    }
//                }

                Bitmap bitmap = ImageCache.getImage(imgPath);

                artWork.setImageBitmap(bitmap);
                changePaletteEnable(((BitmapDrawable)artWork.getDrawable()).getBitmap());
            }
        }

    }

    /**
     * 引数のbitmapを解析して、大画面表示時のレイアウトデザインの配色を調整します。
     * nullで標準の配色レイアウトに戻ります
     * @param bitmap
     */
    private void changePaletteEnable(Bitmap bitmap){
        //nullなら元に戻す
        if(bitmap == null){
            TitleText.setTextColor(Color.DKGRAY);
            ArtistText.setTextColor(Color.DKGRAY);
            relativeLayout.setBackgroundColor(Color.WHITE);
            unit.setBackgroundColor(Color.WHITE);

            TitleText.setBackgroundColor(Color.TRANSPARENT);
            ArtistText.setBackgroundColor(Color.TRANSPARENT);
            return;
        }

        //パレットから非同期で色を設定してみるテスト
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                int mainColor = palette.getDominantColor(Color.TRANSPARENT);
                int textColor;

                //メインカラーが
                //落ち着いた暗い色の場合
                if(mainColor == palette.getDarkMutedColor(Color.TRANSPARENT)){
                    textColor = palette.getLightVibrantColor(Color.LTGRAY);
//                    Toast.makeText(activity, "落ち着いた暗い色", Toast.LENGTH_SHORT).show();

                    //落ち着いた色
                }else if(mainColor == palette.getMutedColor(Color.TRANSPARENT)){
                    textColor = palette.getDarkVibrantColor(Color.DKGRAY);
//                    Toast.makeText(activity, "落ち着いた色", Toast.LENGTH_SHORT).show();
                    //落ち着いた明るい色
                }else if(mainColor == palette.getLightMutedColor(Color.TRANSPARENT)){
                    textColor = palette.getDarkVibrantColor(Color.DKGRAY);
//                    Toast.makeText(activity, "落ち着いた明るい色", Toast.LENGTH_SHORT).show();
                    //鮮やかな暗い色
                }else if(mainColor == palette.getDarkVibrantColor(Color.TRANSPARENT)){
                    textColor = palette.getLightVibrantColor(Color.LTGRAY);
//                    Toast.makeText(activity, "鮮やかな暗い色", Toast.LENGTH_SHORT).show();
                    //鮮やかな色
                }else if(mainColor == palette.getVibrantColor(Color.TRANSPARENT)){
                    //背景の色で目が痛くなるのでチェンジ
                    mainColor = palette.getDarkVibrantColor(palette.getDarkMutedColor(Color.TRANSPARENT));
                    textColor = palette.getLightMutedColor(Color.LTGRAY);
//                    Toast.makeText(activity, "チェンジで", Toast.LENGTH_SHORT).show();

                    //鮮やかな明るい色
                }else if(mainColor == palette.getLightVibrantColor(Color.TRANSPARENT)){
                    textColor = palette.getDarkMutedColor(Color.DKGRAY);
//                    Toast.makeText(activity, "鮮やかな明るい色", Toast.LENGTH_SHORT).show();
                    //その他
                }else{
                    //配色不明なので、Swachに頼る
                    if(palette.getDominantSwatch() != null){
                        textColor = palette.getDominantSwatch().getTitleTextColor();
                    }else{
                        textColor = Color.DKGRAY;
                    }
//                    Toast.makeText(activity, "その他の色", Toast.LENGTH_SHORT).show();
                }

                //textColor = (palette.getDominantSwatch() == null ? Color.DKGRAY : palette.getDominantSwatch().getTitleTextColor());
                relativeLayout.setBackgroundColor(mainColor);
                TitleText.setTextColor(textColor);
                ArtistText.setTextColor(Color.argb(0xC0, Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
                TitleText.setBackgroundColor(Color.argb(0xC0, Color.red(mainColor), Color.green(mainColor),Color.blue(mainColor)));
                ArtistText.setBackgroundColor(Color.argb(0xC0, Color.red(mainColor), Color.green(mainColor),Color.blue(mainColor)));
                unit.setBackgroundColor(palette.getLightMutedColor(Color.parseColor("#A0EEEEEE")));
            }
        });
    }

    //ファイルの再生時間の表示とシークバーの最大値を変更
    private void changePlayTime(long time){
        //久留島と結合次第変更
        maxTimeText.setText(getMin(time) + ":" + getSec(time));
        seekBar.setMax((int) time);
    }

    //現在の再生時間の表示を変更
    private void changePlayingTime(int time){
        nowTimeText.setText(getMin(time) + ":" + getSec(time));
        seekBar.setProgress(time);
    }

    //引数(μ秒)から分を求めて返す。
    private String getMin(long time){
        long min;
        if(time < 60000){
            //1分未満の場合
            return "0";
        }else{
            //1分以上の場合
            min = time/60000;
        }
        //分を返す
        return String.valueOf(min);
    }

    //引数(μ秒)から秒を求めて2桁表示で返す。1桁の場合、先頭を0で埋める
    private String getSec(long time){
        long sec = 0;
        if(time < 60000 && time >= 0){
            //0秒以上1分未満の場合
            sec = time/1000;
        }else if(time >= 60000){
            //1分以上の場合
            long mintime = (time/60000)*60000;
            sec = (time - mintime)/1000;
        }

        //得られた秒数を2桁で返す（1桁の場合、先頭を0で埋める）
        DecimalFormat df = new DecimalFormat("00");
        return String.valueOf(df.format(sec));
    }


    public MusicPlayerMotionController getMotionController() {
        return motionController;
    }

    //再生中に動く画面表示関連の処理
    @Override
    public void run() {
        while(running){
            //再生している曲を問い合わせる
            if(ctr.GetNowFilePath() != null) {
                String NowPlayingFilePath = ctr.GetNowFilePath();
                //自分が認識している情報と相違があれば
                if (!NowFilePath.equals(NowPlayingFilePath)) {
                    //情報の更新
                    NowFilePath = NowPlayingFilePath;
                    //秒数のカウントリセット
                    countPlayingTime = 0;
                    Message msg = new Message();
                    msg.what = CHANGE_MUSIC_INFO;
                    handler.sendMessage(msg);

                    try {
                        thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
            }else {
                //もし、再生している曲がnullだったら
            }
            //再生中の場合
            if(playing) {
                //秒数をﾒｯｾｰｼﾞに入れてハンドラに送る
                Message msg = new Message();
                msg.what = CHANGE_PLAYINGTIME;
                handler.sendMessage(msg);
                //秒数を１秒ずつカウント
                countPlayingTime += 1000;
                if(countPlayingTime > playTime){
                    if(loop == ONE_MUSIC){
                        countPlayingTime = 0;
                    }else {
                        countPlayingTime = playTime;
                        if(loop == NOT_LOOP){
                            if(ctr.getPlayState() != AudioTrack.PLAYSTATE_PLAYING){
                                //再生中の曲の最大時間に達した・繰り返し無・再生状態が再生中ではない
                                // (キューの終端の曲の再生が終了したことを想定した処理)
                                //ボタンの表示を変更する
                                Message msg2 = new Message();
                                msg2.what = NOTIFY_PLAYSTATE;
                                handler.sendMessage(msg2);
                            }
                        }
                    }
                }
                //1秒休み
                try {
                    thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                //一時停止中の場合
                try {
                    thread.sleep(100);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
        //アニメーションスタート時に実行する処理

//        onConnect();

//        if(playing){
//            controlOrder = "OrderStop";
//        }else{
//            controlOrder = "OrderStart";
//        }
//
//        //playingの状態を変更する
//        //playing = !playing;
//
//        //コントローラに対してaudioTrackへの処理を要求する
//        boolean flg = ctr.OrderFromPlayScreen(controlOrder);
//
//        if(!running) {
//            running = true;
//            thread.start();
//        }
//        if(!flg){
//            Toast.makeText(activity,"おや？audioTrackのようすが…",Toast.LENGTH_SHORT).show();
//        }

//        unConnect();
    }

    @Override
    public void onAnimationEnd(Animation animation) {
        //アニメーションが終了したときに実行する処理

        //ボタンの表示を切り替える
        //setButtonText(playing);

        if(playing){
            controlOrder = "OrderStop";
        }else{
            controlOrder = "OrderStart";
        }

        //playingの状態を変更する
        //playing = !playing;

        //コントローラに対してaudioTrackへの処理を要求する
        if(!ctr.OrderFromPlayScreen(controlOrder)){
            Toast.makeText(activity,"再生ができないファイルがありました。",Toast.LENGTH_SHORT).show();
        }

        if(!running) {
            running = true;
            thread.start();
        }

    }

    @Override
    public void onAnimationRepeat(Animation animation) {
        //アニメーションが繰り返すときに実行する処理
    }

    public void setMotionController(MusicPlayerMotionController motionController) {
        this.motionController = motionController;
    }

    public void startFromActivity() {
        if(!playing) {
            playing = ctr.OrderFromPlayScreen("OrderStart");
            if(!running){
                running = true;
                thread.start();
            }
            setButtonText(playing);
        }
    }

    public void ClearQueue() {
        //再生キューの削除
        PlaybackQueue.Clear();
        //コントローラの保持してるパスをリセット
        ctr.SetNowFileNull();

        //audioTrackをリセット
//        ctr.UnSetAudioTrack();
        //再生状態もリセット
//        playing = false;
    }

    public void playFromList(String skipTo) {
        buttonSP.callOnClick();
        if(!NowFilePath.equals(skipTo)){
            skipToFilePath = skipTo;
            buttonNext.callOnClick();
        }
    }

    public void playFromList(){
//        if(ctr.getPlayState() == AudioTrack.PLAYSTATE_PLAYING){
//            ctr.OrderFromPlayScreen("OrderStop");
//        }
//        boolean b =ctr.OrderFromPlayScreen("OrderStart");
//        if(!playing){
//            playing = b;
//            setButtonText(playing);
//        }
//        if(!running){
//            running = b;
//            thread.start();
//        }
        ctr.SetNowFileNull();
    }

    /**
     * 色のデザインが必要になったら呼ばれます
     */
    @Override
    public void onColorDesignNeed() {

        //プライベートモードのときと、ミニプレイヤー時は適用しない
        if(motionController != null && motionController.getScreenMode() == MusicPlayerMotionController.ScreenMode.MINI){
            changePaletteEnable(null);
            return;
        }

        if(PrivateMode){
            changePaletteEnable(null);
            return;
        }
        if(artWork.getDrawable() == null || imgPath.equals("")){
            changePaletteEnable(null);
            return;
        }
        changePaletteEnable(((BitmapDrawable)artWork.getDrawable()).getBitmap());
    }

    /**
     * Fragmentが復帰する直前に呼ばれます
     */
    @Override
    public void onResume(){
        super.onResume();
        //設定をプレファレンスから読み出す
        SharedPreferences read = getActivity().getSharedPreferences("commonSettings", MODE_PRIVATE);
        //プライベートモード
        PrivateMode = read.getBoolean("private", false);
        privateChange(PrivateMode); //プライベートモードを設定
        privateSwich.setChecked(PrivateMode);

        //片耳イヤホンモード
        oneEarMode = read.getInt("oneEar", 0);
        //読み出し用に改変してます
        switch (oneEarMode) {
            case 0:
                //0(両耳モード)
                leftEar.setVisibility(View.VISIBLE);
                rightEar.setVisibility(View.VISIBLE);
                break;
            case 1:
                //1(右耳モード)
                leftEar.setVisibility(View.INVISIBLE);
                rightEar.setVisibility(View.VISIBLE);
                break;
            case 2:
                //2(左耳モード)
                rightEar.setVisibility(View.INVISIBLE);
                leftEar.setVisibility(View.VISIBLE);
                break;
        }
                /*
                 *0:両耳モード
                 *1:左耳モード
                 *2:右耳モード
                 */
        ctr.updateEar(oneEarMode);

        //色設定を反映させる
        onColorDesignNeed();
    }


    /**
     * ビューがﾃﾞｽﾄﾛｰｲされる直前で呼ばれます。その後はやっぱり　デ　ス　ト　ロ　イ　♪
     */
    @Override
    public void onDestroyView() {
        //audioRelayになにかデータが入っていたら、ハンドラ設定をnullに!
        AudioRelay.resistPlayStateChangeReceiveHandler(null);
        //スレッドも停止
        running = false;
        super.onDestroyView();
    }

    /**
     * データベースのデータが更新されたら呼ばれます
     */
    @Override
    public void onDatabaseUpdated() {
        //現在表示中のアルバムアート・曲名やアーティストを取得する
        if(NowFilePath != null && NowFilePath.length() > 0){
            changeMusicInfo(NowFilePath);
        }
    }

}

