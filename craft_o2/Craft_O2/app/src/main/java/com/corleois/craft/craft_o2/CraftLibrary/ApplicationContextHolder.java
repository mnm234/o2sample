package com.corleois.craft.craft_o2.CraftLibrary;

/**
 * Created by StarLink on 2017/08/16.
 */

import android.content.Context;

/**
 * アプリケーションのContextをどこからでも参照できるようにするクラス
 */
public class ApplicationContextHolder {

    private static Context context = null;

    /**
     * アプリケーションのContextを保持します。
     * 大元のContext以外のもの（ActivityやFragment等）の代入はメモリリークの原因になります
     * @param applicationContext
     */
    public static void setApplicationContextToHolder(Context applicationContext){
        context = applicationContext;
    }

    /**
     * 大元のContextが返される予定です。予定は未定。
     * @return
     */
    public static Context getApplicationContext(){
        return context;
    }

}
