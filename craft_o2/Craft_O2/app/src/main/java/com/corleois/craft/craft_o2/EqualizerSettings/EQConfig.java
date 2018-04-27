package com.corleois.craft.craft_o2.EqualizerSettings;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.audiofx.Equalizer;
import android.util.Log;

import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;

import java.util.ArrayList;

/**
 * Created by StarLink on 2017/06/19.
 */

/**
 * イコライザ設定画面側のクラス
 */
public class EQConfig {
    private Equalizer.Settings combinedSettings;                //有効設定の総和の値を保持する設定
    private ArrayList<EQsetting> costomEQsettings;              //イコライザーの各個のカスタム設定を保持するクラス
    private ArrayList<EQsetting> allEQSettings;                 //すべてのイコライザ設定を格納した配列

    private int lastShowedIndex;                                //設定画面にて最後に表示された設定リストのインデックス

    private short minDB;            //イコライザで設定可能な最小音量
    private short maxDB;            //イコライザで設定可能な最大音量
    private int[] intBandLevels;    //イコライザのバンドの数だけ、表現範囲を拡張した変数を用意する
    private int[] intBandFreqs;     //イコライザの各バンドの中心周波数を格納する

    private static final String fName = "EQ.dat";               //イコライザーの設定ファイル名を保持する変数

    /**
     * イコライザを担当するコンストラクタです。
     */
    public EQConfig(Context context){

        //クラス変数の初期設定
        combinedSettings = new Equalizer.Settings();
        costomEQsettings = new ArrayList<>();

        allEQSettings = new ArrayList<>();

        //デフォルトのイコライザー設定を取得してリストで保存する
        allEQSettings.addAll(getDefaultEQSettings());

        //プレファレンスから読み出しを行う
        String readStr = "";
        String readDefaultEnable = "";

        SharedPreferences readP = context.getSharedPreferences(fName,Context.MODE_PRIVATE);
        readDefaultEnable = readP.getString("defaultEnable","");
        //標準の有効無効設定から読み出す
        ArrayList<String> defEnable = StringArrayListConverter.DecodeToStringArrayList(readDefaultEnable,";");
        for (int i = 0; i < defEnable.size(); i++) {
            //trueと等しいとtrueが入る
            allEQSettings.get(i).setEnabled(defEnable.get(i).equals(String.valueOf(true)));
            Log.d("defaltEnable",(String.valueOf(defEnable.get(i).equals(String.valueOf(true)))));
        }

        readStr = readP.getString("EQSettings","");
        int readIndex = readP.getInt("LastShowedIndex",-1);

        Log.d("EQConfig","読み出した設定:"+(readStr));

        //読み出した設定を、設定ごとに分割していく
        ArrayList<String> readEQSettingList;
        ArrayList<String> readEQSettingRecord;

        readEQSettingList = StringArrayListConverter.Decode(readStr,"@");

        //空文字列でないときのみ処理する
        if(!readStr.equals("")){

            //設定ごとに、カスタム設定のオブジェクトを生成してリストに追加する
            for (int i = 0; i < readEQSettingList.size(); i++) {
                readEQSettingRecord = StringArrayListConverter.Decode(readEQSettingList.get(i), ";");

                String presetName = "";
                Boolean enabled = false;
                Boolean canOverWrite = false;
                Equalizer.Settings settings = new Equalizer.Settings();

                //bandLevelsは、最初nullなので、イコライザのバンドの要素数だけ配置してあげる
                settings.bandLevels = new short[intBandLevels.length];

                //プリセット名、有効無効フラグ、設定保護フラグを読み出す
                presetName = readEQSettingRecord.get(0);
                enabled = Boolean.valueOf(readEQSettingRecord.get(1));
                canOverWrite = Boolean.valueOf(readEQSettingRecord.get(2));

                //イコライザーの数値設定を読み出して格納する。設定レコードのバンドの数か、格納できるバンドの数の限界まで到達したら、そこでループ終了
                for (int j = 3; (j < readEQSettingRecord.size() && (j - 3) < settings.bandLevels.length); j++) {
    //                Log.d("EQConfig",String.valueOf(settings.bandLevels.length));
                    settings.bandLevels[j - 3] = Short.valueOf(readEQSettingRecord.get(j));
                }

                EQsetting eQsetting = new EQsetting(settings, enabled);
                eQsetting.setCanOverWrite(canOverWrite);
                eQsetting.setPresetName(presetName);

                //イコライザの設定リストに追加する
                allEQSettings.add(eQsetting);

                //ここはデバッグ用
                Log.d("EQConfig", eQsetting.getPresetName() + "\n" + eQsetting.getCanOverWrite() + "\n" + eQsetting.getEnabled());
                for (int j = 0; j < settings.bandLevels.length; j++) {
                    Log.d("EQConfig", String.valueOf(eQsetting.getSettings().bandLevels[j]));
                }

                //重ねがけ設定の総和を取得して設定する
                getCombinedSettings();
            }
        }

    }

    /**
     * 設定ファイルにカスタムされた設定を保存します。
     * @param context   画面のContext
     * @param lastShowedIndex 最後に表示していた設定リストのindex
     */
    public void saveCustomSettings(Context context,int lastShowedIndex){
        //ArrayList<-->Stringのコンバーターを応用してイコライザー設定を保存してみるテスト
        ArrayList<String> record = null;
        ArrayList<String> strEQSettingList = new ArrayList<>();

        //デフォルト設定の有効無効だけを記録する
        ArrayList<String> defaultEnable = new ArrayList<>();

        //設定を書き出す準備をする
        for (int i = 0; i < allEQSettings.size(); i++) {

            //もしも、上書き可能＝カスタム設定ならば記録する
            if(allEQSettings.get(i).getCanOverWrite()){
                //一件用の配列リスト
                record = new ArrayList<>();

                record.add(allEQSettings.get(i).getPresetName());
                record.add(String.valueOf(allEQSettings.get(i).getEnabled()));
                record.add(String.valueOf(allEQSettings.get(i).getCanOverWrite()));
                Equalizer.Settings settings = allEQSettings.get(i).getSettings();

                for (int j = 0; j < settings.bandLevels.length; j++) {
                    record.add(String.valueOf(settings.bandLevels[j]));
                }

                //1件分の変換済みの設定データを作成する
                strEQSettingList.add(StringArrayListConverter.EncodeToString(record,";"));
            }else{
                //そうでないならば、有効無効の設定だけ別キーで書き込む
                defaultEnable.add(String.valueOf(allEQSettings.get(i).getEnabled()));
            }
        }
        Log.d("EQConfig", StringArrayListConverter.EncodeToString(strEQSettingList,"@"));

        //ここから保存処理
        SharedPreferences preferences = context.getSharedPreferences(fName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("EQSettings", StringArrayListConverter.EncodeToString(strEQSettingList,"@"));
        editor.putString("defaultEnable", StringArrayListConverter.EncodeToString(defaultEnable,";"));
        editor.putInt("LastShowedIndex",lastShowedIndex);
        editor.apply();
        Log.d("EQConfig","保存完了"+ StringArrayListConverter.EncodeToString(strEQSettingList,"@"));
        Log.d("EQConfig","保存完了"+ StringArrayListConverter.EncodeToString(defaultEnable,";"));
    }


    /**
     * イコライザー設定の値を複数反映させ更新、その結果を取得するメソッド。
     */
    public Equalizer.Settings getCombinedSettings(){
        /*
        計算結果を格納するオブジェクトを作る。
        ユーザーの意思を最大限反映させるため、一時的に格納先オブジェクトの表現範囲を超える数値を取り扱う場合がある。
        そのため、変数は各個用意する。
         */

        //イコライザーが持つバンドの数を格納した変数
        short bands = (short)intBandLevels.length;

        //持ってる数値設定を初期化する
        for (int i = 0; i < intBandLevels.length; i++) {
            intBandLevels[i] = 0;
        }

        //適用できる設定を加算する
        for (int i = 0; i < allEQSettings.size(); i++) {

            Log.d("getCombinedSettings",(allEQSettings.get(i).getEnabled()) ? "有効" :"無効");

            //リスト中のイコライザー設定が有効だったらTrue
            if(allEQSettings.get(i).getEnabled()){
                //イコライザーの各バンドの設定値を加算していく
                for(short j = 0; j < bands; j++){
                    intBandLevels[j] += allEQSettings.get(i).getSettings().bandLevels[j];
                }
            }
        }

        //すべての加算が終了したら、表現範囲超過の設定をバッサリ切り落とす
        //要望があれば別の処理方法を考えてもいいかもしれない

        for (int i = 0; i < intBandLevels.length; i++) {

            //もし、最大値超過の場合は最大値でカンスト
            if(intBandLevels[i] > maxDB){
                intBandLevels[i] = maxDB;

                //最小値に不足の場合は最小値でカンスト
            }else if (intBandLevels[i] < minDB){
                intBandLevels[i] = minDB;
            }
        }

        //増幅率を格納する配列を初期化する
        combinedSettings.bandLevels = new short[intBandLevels.length];
        combinedSettings.numBands = (short) intBandLevels.length;

        Log.d("eqc","heartbeat2");
        //イコライザー設定の総和を保持する変数に変数を格納する
        for (short i = 0; i < combinedSettings.numBands; i++){
            combinedSettings.bandLevels[i] = (short)intBandLevels[i];
            Log.d("getCombinedSettings",String.valueOf(combinedSettings.bandLevels[i]));
        }

        //イコライザーに設定を放り込む
        return combinedSettings;
    }



    /**
     * デフォルトのイコライザー設定のグループを取得します。
     * @return
     */
    private ArrayList<EQsetting> getDefaultEQSettings(){
        ArrayList<EQsetting> result = new ArrayList<>();

        //デフォルトのイコライザー設定を取得するのはちょっと特殊
        //先にAudiotrackなどで再生準備を終わらせた状態でセッションIDのみ取得して、何も再生せず終了する

        AudioTrack audioTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                44100,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                AudioTrack.getMinBufferSize(44100,AudioFormat.CHANNEL_IN_STEREO,AudioFormat.ENCODING_PCM_16BIT),
                AudioTrack.MODE_STREAM
        );

        //このセッションIDが欲しかった
        int sessionID =  audioTrack.getAudioSessionId();
        Equalizer tempEQ = new Equalizer(0,sessionID);

        //デフォルトのプリセットの数だけ回す
        for (short i = 0; i < tempEQ.getNumberOfPresets(); i++) {
            //デフォルトにあるプリセットを使うよーって宣言する
            tempEQ.usePreset(i);
            //使うよーって宣言したプリセットの名前を持ってくる
            String pName = tempEQ.getPresetName(i);
            //使うよーって宣言したプリセットの設定値を持ってくる
            Equalizer.Settings settings = tempEQ.getProperties();

            //オリジナルのEQ設定オブジェクトを新規作成する
            EQsetting eQsetting = new EQsetting(settings,false);
            eQsetting.setPresetName(pName);
            eQsetting.setCanOverWrite(false);

            //リザルトに追加する
            result.add(eQsetting);
        }

        //ついでに、必要になる音量の表現範囲などを取得更新する
        minDB = tempEQ.getBandLevelRange()[0];
        maxDB = tempEQ.getBandLevelRange()[1];
        intBandLevels = new int[tempEQ.getNumberOfBands()];
        intBandFreqs = new int[intBandLevels.length];

        //各バンドの調整可能な中心周波数を格納する
        for (short i = 0; i < intBandFreqs.length; i++) {
            intBandFreqs[i] = tempEQ.getCenterFreq(i) / 1000;
        }

        //オーディオトラックのリソースを解放する
        audioTrack.release();

        return result;
    }

    /**
     * すべてのイコライザー設定のグループを取得する
     * @return
     */
    public ArrayList<EQsetting> getAllEQSettings(){
        //リストのコピーを渡す
        ArrayList<EQsetting> result = new ArrayList<>();
        for (int i = 0; i < allEQSettings.size(); i++) {
            EQsetting source = new EQsetting(allEQSettings.get(i).getSettings(),allEQSettings.get(i).getEnabled());
            source.setCanOverWrite(allEQSettings.get(i).getCanOverWrite());
            source.setPresetName(allEQSettings.get(i).getPresetName());
            result.add(source);
        }
        return result;
    }

    /**
     * オブジェクトが持つリスト設定を更新します。
     * @param presetList
     */
    public void setAllEQsettings(ArrayList<EQsetting> presetList){
        //持っているリストをクリア
        allEQSettings = new ArrayList<>();
        //渡されたリストの数だけ処理する
        for (int i = 0; i < presetList.size(); i++) {
            allEQSettings.add(presetList.get(i));
        }
    }

    /**
     * イコライザで調節できるバンド数を返します
     * @return  バンド数
     */
    public int getBandCount(){
        return intBandLevels.length;
    }

    /**
     * イコライザで調節できる各バンドの中心周波数を返します
     * @return
     */
    public int[] getBandCenterFreqs(){
        return intBandFreqs;
    }

    /**
     * イコライザで調節できる最小増幅率（減衰率）を返します
     * @return
     */
    public int getMindB(){
        return minDB;
    }

    /**
     * イコライザで調節できる最大増幅率を返します
     * @return
     */
    public int getMaxdB(){
        return maxDB;
    }

    /**
     * 設定ファイルから読み出した、前回保存時に記録された、最後に表示したインデックス番号を返します
     * @return
     */
    public int getLastShowedIndex(){return lastShowedIndex;}
}
