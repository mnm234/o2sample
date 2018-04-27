package com.corleois.craft.craft_o2.Activities.Interfaces;

/**
 * Created by StarLink on 2017/08/11.
 */

import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;

import java.util.ArrayList;

/**
 * 検索を受け取るインターフェイス
 */
public interface SearchQueryListener {

    /**
     * 検索結果を受け取ると呼ばれるメソッドです
     */
    void onReceiveSearchResult(ArrayList<AudioFileInformation> audioFileInformations);
}
