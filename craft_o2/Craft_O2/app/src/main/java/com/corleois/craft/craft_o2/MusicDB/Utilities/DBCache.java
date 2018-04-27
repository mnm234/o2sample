package com.corleois.craft.craft_o2.MusicDB.Utilities;

/**
 * Created by StarLink on 2017/08/02.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.LruCache;
import android.widget.Toast;

import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 過去に発行されたSelect文の結果をキャッシュするクラスです。
 * AudioFileInformationクラスのインスタンス化やデータの代入に時間的コストがかかるので……
 */
public class DBCache {
    private static int maxEntry = 128;
    private static LruCache<String ,ArrayList<AudioFileInformation>> dbCache = new LruCache<>(maxEntry);
    private static HashMap<Class,ArrayList<AudioFileInformation>> resultCache = new HashMap<>();
    private static LruCache<String, AudioFileInformation> recordCache;

    /**
     * キャッシュからデータを探します。
     * なければデータをDBより取得して返します。
     * @param param
     * @return
     */
    public static synchronized ArrayList<AudioFileInformation> Select(SQLiteFractalCondition param, Context context){

        ArrayList<AudioFileInformation> audioFileInformations;

        //もし、検索条件がなかったらTrue
        if(param == null){
            //検索条件を適当にでっち上げる
            SQLiteConditionElement element = new SQLiteConditionElement();
            SQLiteFractalCondition condition = new SQLiteFractalCondition(element);

            audioFileInformations = dbCache.get(condition.getOriginalString());

            if(audioFileInformations == null){
                dbCache.put(condition.getOriginalString(), MusicDBFront.Select(null, context, false));
                audioFileInformations = dbCache.get(condition.getOriginalString());
                //Toast.makeText(context, "キャッシュミス！ " + dbCache.missCount() , Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(context, "キャッシュヒット！" + dbCache.hitCount() , Toast.LENGTH_SHORT).show();
            }

        }else{
            //検索条件があったらこっち
            audioFileInformations = dbCache.get(param.getOriginalString());

            if(audioFileInformations == null){
                dbCache.put(param.getOriginalString(), MusicDBFront.Select(param, context, false));
                audioFileInformations = dbCache.get(param.getOriginalString());
                //Toast.makeText(context, "キャッシュミス！ " + dbCache.missCount() , Toast.LENGTH_SHORT).show();
            }else{
                //Toast.makeText(context, "キャッシュヒット！" + dbCache.hitCount() , Toast.LENGTH_SHORT).show();
            }

        }


        return audioFileInformations;
    }

    /**
     * ファイルパスと対応するAudioFileInformationクラスを吐き出します。
     * @param filePath
     * @param context
     * @return
     */
    public static synchronized AudioFileInformation SelectByFilePath(@NonNull String filePath, Context context){

        //キャッシュが作られてなければ、ここで作る
        if(recordCache == null){
            ArrayList<AudioFileInformation> informations = MusicDBFront.Select(null, context, true);

            if(informations != null) {
                int size = informations.size();
                if (size > 0) {
                    recordCache = new LruCache<>(size);
                    for (int i = 0; i < size; i++) {
                        recordCache.put(informations.get(i).getFilePath(), informations.get(i));
                    }
                } else {
                    return null;
                }
            }
        }

        AudioFileInformation information;

        //ここでnullなら、DBもnullのはず
        information = recordCache.get(filePath);

        return information;
    }


    /**
     * クラスで保持しているAudioFileInformationリストをメモリ上に保存します。
     * フラッシュするときはnullを入れてください
     * @param yourClass
     */
    public static synchronized void setResultCache(Class yourClass, ArrayList<AudioFileInformation> fileInformations){
        resultCache.put(yourClass, fileInformations);
    }

    /**
     * クラスごとに保存してあるキャッシュを獲得します。ない場合はnullです
     * @param yourClass
     * @return
     */
    public static synchronized ArrayList<AudioFileInformation> getResultCache(Class yourClass){
        return resultCache.get(yourClass);
    }

    /**
     * クラスで保持しているキャッシュをすべて白紙に戻します
     */
    public static synchronized void flashResultCache(){
        resultCache = new HashMap<>();
    }

    /**
     * キャッシュをフラッシュし、白紙に戻します。
     */
    public static synchronized void flash(){
        dbCache = null;
        dbCache = new LruCache<>(maxEntry);
        resultCache = null;
        resultCache = new HashMap<>();

        recordCache = null;
    }

}
