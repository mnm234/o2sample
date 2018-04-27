package com.corleois.craft.craft_o2.MusicDB;

/**
 * Created by StarLink on 2017/06/10.
 */

import android.app.Application;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * DBやキャッシュに関するものを取り扱うクラス
 */
public class MusicDBFront extends Application{//AndroidManifest.xmlに、android:name=".MusicDBFront"を追加する

    /**
     * 引数で渡されたレコードリストを、すべてデータベースに挿入します。返り値は、処理できなかった・処理しなかったリストです。
     * @param audioFileInformations 処理したいデータのリスト
     * @param IfAlreadyExistsThenUpdate   すでに同じ主キーが登録されている場合、アップデートを行うか
     * @return  処理できなかった・処理しなかったリスト
     */
    public static ArrayList<AudioFileInformation> Insert(ArrayList<AudioFileInformation> audioFileInformations, boolean IfAlreadyExistsThenUpdate, Context yourObject){
        Log.d("MusicDBFront","インサート処理を始めます");
        //下2つの変数は、必要とするメソッドでオープン・クローズ処理を行う
        MusicDBOpenHelper helper;
        SQLiteDatabase db;
        //準備
        helper = new MusicDBOpenHelper(yourObject);
        db = helper.getWritableDatabase();
        //レコードを入れるやつ
        ContentValues values;

        //失敗リスト
        ArrayList<AudioFileInformation> failed = new ArrayList<>();

        //Transactionを明記してスピードアップを図るよ
        //排他モードのトランザクション
        db.beginTransaction();
        
        StringArrayListConverter conv = new StringArrayListConverter();
        
        for (AudioFileInformation record:audioFileInformations ) {
            //データの挿入または更新を行う
            try{
                //INSERTするデータ一件をこれから用意していく
                values = new ContentValues();
                values.put(MusicDBColumns.FilePath, record.getFilePath());
                values.put(MusicDBColumns.Hash, record.getHash());
                values.put(MusicDBColumns.NotExistFlag, record.getNotExistFlag());

                //ArrayList形式のものに関しては区切り文字";"で区切ったStringに変換する
                String separator = ";";
                values.put(MusicDBColumns.Title,              conv.Encode(record.getTitle(),separator));
                values.put(MusicDBColumns.Artist,             conv.Encode(record.getArtist(),separator));
                values.put(MusicDBColumns.AlbumArtist,        conv.Encode(record.getAlbumArtist(),separator));
                values.put(MusicDBColumns.Album,              conv.Encode(record.getAlbum(),separator));
                values.put(MusicDBColumns.Genre,              conv.Encode(record.getGenre(),separator));
                values.put(MusicDBColumns.YomiTitle,          conv.Encode(record.getYomiTitle(),separator));
                values.put(MusicDBColumns.YomiArtist,         conv.Encode(record.getYomiArtist(),separator));
                values.put(MusicDBColumns.YomiAlbum,          conv.Encode(record.getYomiAlbum(),separator));
                values.put(MusicDBColumns.YomiGenre,          conv.Encode(record.getYomiGenre(),separator));
                values.put(MusicDBColumns.YomiAlbumArtist,    conv.Encode(record.getYomiAlbumArtist(),separator));
                values.put(MusicDBColumns.ArtWorkPath,        conv.Encode(record.getArtWorkPath(),separator));
                values.put(MusicDBColumns.Season,             conv.Encode(record.getSeason(),separator));
                values.put(MusicDBColumns.Comment,            conv.Encode(record.getComment(),separator));
                values.put(MusicDBColumns.Lyrics,             conv.Encode(record.getLyrics(),separator));
                values.put(MusicDBColumns.ParentCreation,     conv.Encode(record.getParentCreation(),separator));
                values.put(MusicDBColumns.PlaybackCount,      record.getPlaybackCount());
                values.put(MusicDBColumns.TrackNumber,        record.getTrackNumber());
                values.put(MusicDBColumns.AddDateTime,        record.getAddDateTimeByString());
                values.put(MusicDBColumns.LastPlayDateTime,   record.getLastPlayDateTimeByString());
                values.put(MusicDBColumns.Year,               record.getYearByString());
                values.put(MusicDBColumns.Rating,             record.getRating());
                values.put(MusicDBColumns.TotalTracks,        record.getTotalTracks());
                values.put(MusicDBColumns.TotalDiscs,         record.getTotalDiscs());
                values.put(MusicDBColumns.DiscNo,             record.getDiscNo());

                /*
                * 挿入を行う。
                * UNIQUE制約が衝突したときにどう処理するかの判断までしてくれるありがたいメソッドがあったので、使わせていただく
                * insertできなかったときや、しなかったときは-1Lが返り値
                * https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#CONFLICT_IGNORE
                * */
                long result = db.insertWithOnConflict("MainTable",null,values,((IfAlreadyExistsThenUpdate) ? SQLiteDatabase.CONFLICT_REPLACE : SQLiteDatabase.CONFLICT_IGNORE));

                //もし、挿入・更新処理に失敗あるいは処理を行わなかったら
                if(result == -1L){
                    //例外を投げる
                    throw new Exception();
                }
            }catch (Exception e){
                failed.add(record);
                Log.d("MusicDBFront_Insert()",e.toString());
                e.printStackTrace();
            }
        }
        //トランザクションが成功したことを宣言する（コミットされる）
        db.setTransactionSuccessful();
        //トランザクションを終了させる
        db.endTransaction();

        //遊んだらお片付け
        db.close();
        helper.close();
        //キャッシュは使い物になってない可能性があるのでフラッシュ
        DBCache.flash();

        //失敗したリストを返す
        return failed;
    }
    
    /**
     * 引数で渡されたレコードを、データベースに挿入します。返り値は、処理できたかどうかです。できた場合はTrueが返ります。
     * @param audioFileInformation 処理したいレコード
     * @param IfAlreadyExistsThenUpdate   すでに同じ主キーが登録されている場合、アップデートを行うか
     * @return  成功可否
     */
    public static Boolean Insert(AudioFileInformation audioFileInformation, boolean IfAlreadyExistsThenUpdate, Context yourObject){
        Log.d("MusicDBFront","インサート単体処理を始めます");
        //下2つの変数は、必要とするメソッドでオープン・クローズ処理を行う
        MusicDBOpenHelper helper;
        SQLiteDatabase db;

        //準備
        helper = new MusicDBOpenHelper(yourObject);
        db = helper.getWritableDatabase();
        //レコードを入れるやつ
        ContentValues values;

        boolean success = true;
        StringArrayListConverter conv = new StringArrayListConverter();
        //データの挿入または更新を行う
        try{
            //INSERTするデータ一件をこれから用意していく
            values = new ContentValues();
            values.put(MusicDBColumns.FilePath,           audioFileInformation.getFilePath());
            values.put(MusicDBColumns.Hash,               audioFileInformation.getHash());
            values.put(MusicDBColumns.NotExistFlag,       audioFileInformation.getNotExistFlag());

            //ArrayList形式のものに関しては区切り文字";"で区切ったStringに変換する
            String separator = ";";
            values.put(MusicDBColumns.Title,              conv.Encode(audioFileInformation.getTitle(),separator));
            values.put(MusicDBColumns.Artist,             conv.Encode(audioFileInformation.getArtist(),separator));
            values.put(MusicDBColumns.AlbumArtist,        conv.Encode(audioFileInformation.getAlbumArtist(),separator));
            values.put(MusicDBColumns.Album,              conv.Encode(audioFileInformation.getAlbum(),separator));
            values.put(MusicDBColumns.Genre,              conv.Encode(audioFileInformation.getGenre(),separator));
            values.put(MusicDBColumns.YomiTitle,          conv.Encode(audioFileInformation.getYomiTitle(),separator));
            values.put(MusicDBColumns.YomiArtist,         conv.Encode(audioFileInformation.getYomiArtist(),separator));
            values.put(MusicDBColumns.YomiAlbum,          conv.Encode(audioFileInformation.getYomiAlbum(),separator));
            values.put(MusicDBColumns.YomiGenre,          conv.Encode(audioFileInformation.getYomiGenre(),separator));
            values.put(MusicDBColumns.YomiAlbumArtist,    conv.Encode(audioFileInformation.getYomiAlbumArtist(),separator));
            values.put(MusicDBColumns.ArtWorkPath,        conv.Encode(audioFileInformation.getArtWorkPath(),separator));
            values.put(MusicDBColumns.Season,             conv.Encode(audioFileInformation.getSeason(),separator));
            values.put(MusicDBColumns.Comment,            conv.Encode(audioFileInformation.getComment(),separator));
            values.put(MusicDBColumns.Lyrics,             conv.Encode(audioFileInformation.getLyrics(),separator));
            values.put(MusicDBColumns.ParentCreation,     conv.Encode(audioFileInformation.getParentCreation(),separator));
            values.put(MusicDBColumns.PlaybackCount,      audioFileInformation.getPlaybackCount());
            values.put(MusicDBColumns.TrackNumber,        audioFileInformation.getTrackNumber());
            values.put(MusicDBColumns.AddDateTime,        audioFileInformation.getAddDateTimeByString());
            values.put(MusicDBColumns.LastPlayDateTime,   audioFileInformation.getLastPlayDateTimeByString());
            values.put(MusicDBColumns.Year,               audioFileInformation.getYearByString());
            values.put(MusicDBColumns.Rating,             audioFileInformation.getRating());
            values.put(MusicDBColumns.TotalTracks,         audioFileInformation.getTotalTracks());
            values.put(MusicDBColumns.TotalDiscs,         audioFileInformation.getTotalDiscs());
            values.put(MusicDBColumns.DiscNo,             audioFileInformation.getDiscNo());

            /*
            * 挿入を行う。
            * UNIQUE制約が衝突したときにどう処理するかの判断までしてくれるありがたいメソッドがあったので、使わせていただく
            * insertできなかったときや、しなかったときは-1Lが返り値なので、一致しないかどうかでBooleanに変換する
            * https://developer.android.com/reference/android/database/sqlite/SQLiteDatabase.html#CONFLICT_IGNORE
            * */
            success = (-1L != db.insertWithOnConflict("MainTable",null,values,((IfAlreadyExistsThenUpdate) ? SQLiteDatabase.CONFLICT_REPLACE : SQLiteDatabase.CONFLICT_IGNORE)));

        }catch (Exception e){
            //どこかで例外が起きたら、失敗扱いにする
            success = false;
            Log.d("MusicDBFront_Insert()",e.getMessage());
        }finally {
            //遊んだらお片付け
            db.close();
            helper.close();
            //キャッシュは使い物になってない可能性があるのでフラッシュ
            DBCache.flash();

            return success;
        }
    }

    /**
     * データベースから、引数で渡された条件に該当するレコードを検索し、その結果を返します。
     * @param sqLiteFractalCondition
     * @param yourObject
     * @param allowDataFromCache
     * @return  検索結果のリスト
     */
    public static ArrayList<AudioFileInformation> Select(SQLiteFractalCondition sqLiteFractalCondition, Context yourObject, boolean allowDataFromCache){

        //キャッシュから取ってくることを許されていれば、キャッシュを先に探してそれを答えとしちゃう
        if(allowDataFromCache){
            return DBCache.Select(sqLiteFractalCondition, yourObject);
        }


        //下2つの変数は、必要とするメソッドでオープン・クローズ処理を行う
        Log.d("DBFront_Select()","初期化開始");
        MusicDBOpenHelper helper;
        SQLiteDatabase db;

        //SQLのWHERE句の部分をどうにかして生成する
        String WHERE = null;
        String[] PARAM = null;
        //もし、引数がnullじゃないなら、検索条件文字列を取得する
        if(sqLiteFractalCondition != null) {
            WHERE = sqLiteFractalCondition.getConditionString();
            PARAM = sqLiteFractalCondition.getParameters();
//        }
        Log.d("DBFront_Select()","WHERE条件設定完了"+WHERE);
//        {
            String test ="";
            for (int i = 0; i < PARAM.length; i++){
                test += PARAM[i] + "/";
            }
            Log.d("DBFront_Select()","PARAM設定完了"+test);
        }

        //返す値を準備
        ArrayList<AudioFileInformation> result = new ArrayList<>();
        //DB初期化
        helper = new MusicDBOpenHelper(yourObject);
        db = helper.getReadableDatabase();
        try {
            Cursor cur = null;

            //ここから読み出し処理
            //SELECT文発行
            Log.d("DBFront_Select()","SELECT文実行");
            cur = db.query("MainTable", //テーブル名
                    null,
                    WHERE,
                    PARAM,
                    null,
                    null,
                    null);

            //もし、カーソルがnullじゃないなら結果があるはず
            if(cur != null){

                String separator = ";";
                StringArrayListConverter conv = new StringArrayListConverter();
                Log.d("DBFront_Select()","結果取得開始");
                //最初から最後まで取り出す
                while (cur.moveToNext()){
                    //レコードオブジェクトを作る
                    AudioFileInformation gotRecord = new AudioFileInformation(
                            cur.getString(cur.getColumnIndex(MusicDBColumns.FilePath)),
                            cur.getString(cur.getColumnIndex(MusicDBColumns.Hash))
                    );

                    //メタデータを格納していく
                    gotRecord.setNotExistFlag(cur.getInt(cur.getColumnIndex(MusicDBColumns.NotExistFlag)));

                    //高速詠唱
                    gotRecord.setTitle(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Title)),separator));
                    gotRecord.setArtist(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Artist)),separator));
                    gotRecord.setAlbumArtist(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.AlbumArtist)),separator));
                    gotRecord.setAlbum(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Album)),separator));
                    gotRecord.setGenre(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Genre)),separator));
                    gotRecord.setYomiTitle(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.YomiTitle)),separator));
                    gotRecord.setYomiArtist(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.YomiArtist)),separator));
                    gotRecord.setYomiAlbum(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.YomiAlbum)),separator));
                    gotRecord.setYomiGenre(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.YomiGenre)),separator));
                    gotRecord.setYomiAlbumArtist(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.YomiAlbumArtist)),separator));
                    gotRecord.setArtWorkPath(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.ArtWorkPath)),separator));
                    gotRecord.setPlaybackCount(cur.getInt(cur.getColumnIndex(MusicDBColumns.PlaybackCount)));
                    gotRecord.setTrackNumber(cur.getInt(cur.getColumnIndex(MusicDBColumns.TrackNumber)));
//                    gotRecord.setAddDateTime(getDateTimeFromString(cur.getString(cur.getColumnIndex(MusicDBColumns.AddDateTime)),"yyyy/MM/dd HH:mm:ss"));
//                    gotRecord.setLastPlayDateTime(getDateTimeFromString(cur.getString(cur.getColumnIndex(MusicDBColumns.LastPlayDateTime)), "yyyy/MM/dd HH:mm:ss"));
//                    gotRecord.setYear(getDateTimeFromString(cur.getString(cur.getColumnIndex(MusicDBColumns.Year)), "yyyy"));
                    gotRecord.setAddDateTime(cur.getString(cur.getColumnIndex(MusicDBColumns.AddDateTime)));
                    gotRecord.setLastPlayDateTime(cur.getString(cur.getColumnIndex(MusicDBColumns.LastPlayDateTime)));
                    gotRecord.setYear(cur.getString(cur.getColumnIndex(MusicDBColumns.Year)));
//                    gotRecord.setYear(cur.getString(cur.getColumnIndex(MusicDBColumns.Year)));
                    gotRecord.setSeason(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Season)),separator));
                    gotRecord.setRating(cur.getDouble(cur.getColumnIndex(MusicDBColumns.Title)));
                    gotRecord.setComment(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Comment)),separator));
                    gotRecord.setTotalTracks(cur.getInt(cur.getColumnIndex(MusicDBColumns.TotalTracks)));
                    gotRecord.setTotalDiscs(cur.getInt(cur.getColumnIndex(MusicDBColumns.TotalDiscs)));
                    gotRecord.setDiscNo(cur.getInt(cur.getColumnIndex(MusicDBColumns.DiscNo)));
                    gotRecord.setLyrics(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.Lyrics)),separator));
                    gotRecord.setParentCreation(conv.Decode(cur.getString(cur.getColumnIndex(MusicDBColumns.ParentCreation)),separator));

                    //データ1件分ができたら、これをリストに追加する
                    result.add(gotRecord);
                }
                cur.close();
            }
        }catch (Exception e){
            Log.d("MusicDBFront_Insert()",e.getMessage());
        }
        Log.d("DBFront_Select()","結果取得完了");
        //終了処理
        db.close();
        helper.close();

        //リストを返却して終了
        return result;
    }


    /**
     * 指定された項目をアップデートします
     * @param values
     * @param condition
     * @param IfAlreadyExistsThenUpdate
     * @param yourObject
     * @return  処理した行数
     */
    public static int Update(ContentValues values, SQLiteFractalCondition condition, boolean IfAlreadyExistsThenUpdate, Context yourObject){

        //Update文を実行する準備をする
        MusicDBOpenHelper musicDBOpenHelper = new MusicDBOpenHelper(yourObject);
        SQLiteDatabase db = musicDBOpenHelper.getWritableDatabase();

        int result = db.updateWithOnConflict("MainTable",
                values,
                condition.getConditionString(),
                condition.getParameters(),
                ((IfAlreadyExistsThenUpdate)?SQLiteDatabase.CONFLICT_REPLACE : SQLiteDatabase.CONFLICT_IGNORE));

        //後片付け
        db.close();
        musicDBOpenHelper.close();

        //キャッシュは使い物になってない可能性があるのでフラッシュ
        DBCache.flash();


        return result;
    }

    /**
     * 条件に合うレコードを削除します。
     * @param condition
     * @param yourObject
     * @return 処理された行数
     */
    public static int Delete(SQLiteFractalCondition condition, Context yourObject){

        //Update文を実行する準備をする
        MusicDBOpenHelper musicDBOpenHelper = new MusicDBOpenHelper(yourObject);
        SQLiteDatabase db = musicDBOpenHelper.getWritableDatabase();

        int result = db.delete("MainTable",condition.getConditionString(),condition.getParameters());

        //後片付け
        db.close();
        musicDBOpenHelper.close();
        //キャッシュは使い物になってない可能性があるのでフラッシュ
        DBCache.flash();

        return result;
    }


    /**
     * DBからとってきた文字列形式の時間データを、返すオブジェクトに適合する形に変換する内部メソッド。
     * @param DateTimeString
     * @return
     */
    private static Date getDateTimeFromString(String DateTimeString, String format){
        Date result = null;

        //もしデータがなかったら、それは空欄なのでnullを返す
        if(DateTimeString == "" || DateTimeString == null){
            return null;
        }
        try {
            //何かしらのデータが入っていたら、変換する
            SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
            result = sdf.parse(DateTimeString);
        }catch (Exception e){
            Log.d("MusicDBFront_DateConv", e.getMessage());
        }

        return result;
    }

}
