package com.corleois.craft.craft_o2.playlist;

import android.content.Context;

import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;

import java.util.ArrayList;

/**
 * プレイリストのファイルパスを引数にして、プレイリストのデータを読み込んで、
 * そのプレイリストの曲について、
 * DBから取得して、ArrayList<AudioFileInformation>を返してくれるようにするクラス
 */

public class FetchPlaylist {

    /**
     * DBCacheから取ってきたAudioFileInformationをArrayListに格納
     * @param listName　プレイリスト名
     * @param c コンテキスト
     * @return
     */
    public static ArrayList<AudioFileInformation> getList(String listName, Context c){
        //返すプレイリストの曲のリスト
        ArrayList <AudioFileInformation> list = new ArrayList<>();
        ArrayList<String> paths;

        m3u8SaveRead saveRead = new m3u8SaveRead(c);
        paths = saveRead.openList(listName);

        int size = paths.size();

        /**
         * リストの数だけ探してくる
         */
        for (int i = 0; i < size; i++) {
            list.add(DBCache.SelectByFilePath(paths.get(i), c));
        }

        return list;
    }
   
}
