package com.corleois.craft.craft_o2.MusicDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


/**
 * Created by 2150254 on 2017/06/02.
 */

public class MusicDBOpenHelper extends SQLiteOpenHelper {
    static  final String ThisClassName = "SQLite_MusicDB";
    static final String DB_NAME = "Music.db";
    static final int DB_VERSION = 8;
    static  final String TABLE_NAME = "MainTable";

    public MusicDBOpenHelper(Context context){
        super(context, DB_NAME, null,DB_VERSION);
        //OK
        Log.d(ThisClassName,"コンストラクタ呼ばれたよ！");
    }

    /**
     * データベースを新規に作成するためのメソッド。自動的に呼ばれるかもしれない。（呼ばれないかもしれない）
     * @param db
     */
    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(CreateID3v2QueryString());
        Log.d(ThisClassName,"データベース作ってみたよ！");
    }

    /**
     * データベースを再作成します。
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
        Log.d(ThisClassName,"データベース作り直してみたよ！");
    }


    /**
     * CREATE文を返します
     * @return
     */
    private String CreateID3v2QueryString(){
        //MusicDBColumnsクラスの定数を呼び出して定義。
        return "CREATE TABLE "+
                TABLE_NAME + " (" +
                //管理用列
                MusicDBColumns.FilePath + " TEXT PRIMARY KEY,"+   //ファイルパス
                MusicDBColumns.Hash + " TEXT,"+                  //ファイルが移動された場合などに追跡するために使うファイルのハッシュ関数。INTEGERでは大きすぎて格納できないのでテキストで。
                MusicDBColumns.NotExistFlag + " INTEGER,"+           //対になるファイルが見つからないときにフラグに1が立つ。他は0とかnull
                //メタデータ列
                MusicDBColumns.Title + " TEXT,"+
                MusicDBColumns.Artist+" TEXT,"+
                MusicDBColumns.AlbumArtist+" TEXT,"+
                MusicDBColumns.Album+" TEXT,"+
                MusicDBColumns.Genre+" TEXT,"+
                MusicDBColumns.YomiTitle+" TEXT,"+
                MusicDBColumns.YomiArtist+" TEXT,"+
                MusicDBColumns.YomiAlbum+" TEXT,"+
                MusicDBColumns.YomiGenre+" TEXT,"+
                MusicDBColumns.YomiAlbumArtist+" TEXT,"+
                MusicDBColumns.ArtWorkPath+" TEXT,"+
                MusicDBColumns.PlaybackCount+" INTEGER,"+
                MusicDBColumns.TrackNumber+" INTEGER,"+
                MusicDBColumns.AddDateTime+" TEXT,"+
                MusicDBColumns.LastPlayDateTime+" TEXT,"+
                MusicDBColumns.Year+" TEXT,"+
                MusicDBColumns.Season+" TEXT,"+
                MusicDBColumns.Rating+" REAL,"+
                MusicDBColumns.Comment+" TEXT,"+
                MusicDBColumns.TotalTracks+" INTEGER,"+
                MusicDBColumns.TotalDiscs+" INTEGER,"+
                MusicDBColumns.DiscNo+" INTEGER,"+
                MusicDBColumns.Lyrics+" TEXT,"+
                MusicDBColumns.ParentCreation+" TEXT"+
                ");";
    }
}
