package com.corleois.craft.craft_o2;

import android.app.Activity;
import android.media.MediaMetadataRetriever;
import android.util.Log;

import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;

import java.util.ArrayList;

/**
 * Created by corleois on 2017/07/09.
 * 再生画面からの要求に基づき再生中の曲の各種情報を返すクラスです。
 */

public class SelectMusicInfoFromDB {
    private String searchPath;

     static private Activity activity;
    static private SQLiteFractalCondition fractalCondition;
    static private String Title = "";
    static private String Artist = "";
    static private int playTime = 0;
    static private String imgPath = "";

    /**
     *
     * @param filePath 検索する曲のファイルパス
     * @param activity 再生画面が持ってるactivityを使用します
     */
    public SelectMusicInfoFromDB(String filePath, Activity activity) {
        //検索するファイルﾊﾟｽをもらう
        searchPath = filePath;
        this.activity = activity;
        setInfo();
    }

    /**
     * filePathで検索をかけてタイトル、アーティスト名(またはアルバムアーティスト名)、再生時間、アートワークのパスを保持する。
     * 必要であれば保持するデータ追加してね。
     */
    public void setInfo(){
        //検索項目の設定　ファイルパスの完全一致
        SQLiteConditionElement conditionElement =
                new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.FilePath,searchPath,"");
        //え？なんで彼もstaticなのかって？それは後で分かるサ（UpDateするタイミングの問題です）
        fractalCondition = new SQLiteFractalCondition(conditionElement);
        //検索結果をAudioFileInfoに格納、ファイルパスだから択一のはず
        ArrayList<AudioFileInformation> List = MusicDBFront.Select(fractalCondition,activity, true);

        if(List.size() == 0){
            //何も見つからなかったらばいばーい
            Log.d("検索結果","0件");
            return;
        }

        //再生してる曲の情報が見つかればセットしていくよー
        AudioFileInformation afi = List.get(0);
        //タイトル
        if(afi.getTitle() == null || afi.getTitle().equals("")){
            Title = "タイトル不明";
        }else{
            Title = StringArrayListConverter.EncodeToString(afi.getTitle()," / ");
        }
        Log.d("GET ITEM",Title);

        //アーティスト
        if(afi.getArtist() == null || afi.getArtist().equals("")){
            if(afi.getAlbumArtist() == null || afi.getAlbumArtist().equals("")){
                Artist = "アーティスト不明";
            }else{
                Artist = StringArrayListConverter.EncodeToString(afi.getAlbumArtist()," / ");
            }
        }else{
            Artist = StringArrayListConverter.EncodeToString(afi.getArtist()," / ");
        }
        Log.d("GET ITEM",Artist);

        //再生時間
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(searchPath);
        if(mmr.extractMetadata(mmr.METADATA_KEY_DURATION) != null) {
          playTime = Integer.parseInt(mmr.extractMetadata(mmr.METADATA_KEY_DURATION));
        }
        Log.d("GET ITEM", String.valueOf(playTime));

        //アーティスト画像のパス
        if(afi.getArtWorkPath() == null || afi.getArtWorkPath().equals("") || afi.getArtWorkPath().size() == 0){
            imgPath = "";
        }else{
            imgPath = afi.getArtWorkPath().get(0);
        }
        Log.d("GET ITEM",imgPath);
    }

    public String getArtistInfo(){
        return Artist;
    }

    public String getTitleInfo(){
        return Title;
    }

    public int getPlayTimeInfo(){
        return playTime;
    }

    public String getImgPath(){
        return imgPath;
    }
}
