package com.corleois.craft.craft_o2.Control;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;

import java.util.Collections;


/**
 * Created by Mato on 2017/08/04.
 */

public class AudioFilePreference{

    /*
        呼び出し元で以下を記述
        //共通部
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        //書き込み時
        AudioFilePreference.SetPreference(sharedPreferences,"書き込むファイルパス");
        //読み込み時
        String Path = AudioFilePreference.GetPreference(sharedPreferences);
 */
    /**
     *
     * @param context
     * @param str SetしたいString
     */
    public static void SetPreference (String str, Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor  = sharedPreferences.edit();
        editor.putString("LastPlayFilePath",str);
        editor.commit();
    }

    /**
     *
     * @param context
     * @return 書き込まれてるString
     */
    public static String GetPreference(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String FilePath = sharedPreferences.getString("LastPlayFilePath","");
        return FilePath;
    }
}
