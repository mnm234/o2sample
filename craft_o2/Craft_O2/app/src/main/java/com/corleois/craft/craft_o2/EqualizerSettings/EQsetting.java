package com.corleois.craft.craft_o2.EqualizerSettings;

import android.media.audiofx.Equalizer;

import com.corleois.craft.craft_o2.OriginalExceptions.GabagabaArgumentException;

/**
 * イコライザー設定と、その有効無効の状態を1件だけ保持するクラス。
  */
public class EQsetting {

    private Equalizer.Settings settings;    //一件が持つ設定
    private boolean enabled;                //適用するかしないかの設定
    private boolean canOverWrite;           //この設定を上書き保存できるかの設定。デフォルトで入っている設定を壊さないようにするためのフラグ
    private String presetName;              //イコライザー設定の名前。

    /**
     * イコライザーとその有効無効の設定を設定したレコードを一件作成します
     * @param settings
     * @param enabled
     */
    public EQsetting(Equalizer.Settings settings, boolean enabled){

        //バリデーションチェック
        if(settings == null){
            throw new GabagabaArgumentException();
        }

        //設定系を代入
        this.settings = new Equalizer.Settings();
        this.settings.bandLevels = new short[settings.bandLevels.length];
        for (int i = 0; i < this.settings.bandLevels.length; i++) {
            this.settings.bandLevels[i] = settings.bandLevels[i];
        }
        this.settings.curPreset = settings.curPreset;
        this.settings.numBands = settings.numBands;

        this.enabled = enabled;
        canOverWrite = true;
        presetName = "";
    }

    /**
     * イコライザーの設定値を返します。
     * @return  イコライザー設定オブジェクト
     */
    public Equalizer.Settings getSettings(){
        return settings;
    }

    /**
     * イコライザーの設定値を格納します。
     */
    public void setSettings(Equalizer.Settings settings){
        if(settings == null){
            throw new GabagabaArgumentException("Equalizer.SettingsがNullです");
        }

        //private変数にしまっちゃおーねー
        this.settings = settings;
    }

    /**
     * イコライザーが適用されているかどうかを返します。
     * @return  true:有効 false:無効
     */
    public boolean getEnabled(){
        return enabled;
    }

    /**
     * イコライザーの適用状態を設定します。
     */
    public void setEnabled(boolean enabled){
        this.enabled = enabled;
    }

    /**
     * このプリセットの上書きを許可するかどうかを設定します。
     * @param canOverWrite
     */
    public void setCanOverWrite(boolean canOverWrite){
        this.canOverWrite = canOverWrite;
    }

    /**
     * このプリセットの上書きが許可されているかどうかを設定します。
     * @return
     */
    public boolean getCanOverWrite(){
        return canOverWrite;
    }

    /**
     * このプリセットの名前があれば返します。ない場合は空文字列です。
     * @return
     */
    public String getPresetName() {
        return presetName;
    }

    /**
     * このプリセットの名前を設定します。
     * 文字「;」と[@]は、内部処理で使われるため、自動的に取り除かれます。
     * イコライザ登録時にバリデーションチェックを行い除外することが最も望ましいです。
     * @param presetName
     */
    public void setPresetName(String presetName) {

        //nullチェック
        if(presetName == null){
            throw new GabagabaArgumentException("EQConfig:presetNameがnullです");
        }

        presetName = presetName.replace(";","");
        presetName = presetName.replace("@","");
        this.presetName = presetName;
    }
}
