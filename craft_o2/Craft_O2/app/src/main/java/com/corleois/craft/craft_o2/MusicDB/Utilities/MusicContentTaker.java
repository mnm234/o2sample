package com.corleois.craft.craft_o2.MusicDB.Utilities;

/**
 * Created by StarLink on 2017/08/13.
 */

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.FileNameEscape;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * AndroidのコンテントプロバイダーからMusicを取得する
 */
public class MusicContentTaker {

    Context context;
    ContentResolver contentResolver;
    Cursor cursor;

    public MusicContentTaker(Context context){
        contentResolver = context.getContentResolver();
        this.context = context;
    }

    /**
     * 端末内部のデータを取得してきます
     * @return
     */
    public ArrayList<AudioFileInformation> getMusicDataFromInternalFlash(){
        return getMusicDataFromContentResolver(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
    }

    public ArrayList<AudioFileInformation> getMusicDataFromExternalStorage(){
        return getMusicDataFromContentResolver(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
    }


    /**
     * 指名されたURIのコンテントリゾルバから、AudioFileInformationを生成します。
     * @param uri
     * @return
     */
    public ArrayList<AudioFileInformation> getMusicDataFromContentResolver(Uri uri){

        // 引っ張ってくる拡張子を指定
        String[] extensions = {
                "mp3","MP3",
                "wav","WAV",
                "mp4","MP4","m4a","M4A",
                "ogg","OGG",
                "mid","MID","midi","MIDI",
                "flac","FLAC"};

        int size = extensions.length;

        //早速クエリ実行
        cursor = contentResolver.query(uri, null, null, null, null);

        ArrayList<AudioFileInformation> res = null;
        //結果が帰ってきたらtrue
        if(cursor != null){
            res = new ArrayList<>();

            //最後の行までぶん回せー
            while (cursor.moveToNext()){

                //先にファイルパスを獲得するよー
//                Log.d("コンテントリゾルバ",cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                //DBにデータがあったら、作って挿入する処理がもったいないので省く
                if(DBCache.SelectByFilePath(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)), context) != null){
                    continue;
                }

                {
                    //次は拡張子が対応しているか検査するよ！
                    boolean support = false;
                    for (int i = 0; i < size; i++) {
                        //拡張子がマッチしたら、サポートしていることを示す
                        if (cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)).matches("^.*\\." + extensions[i] + "$")) {
                            support = true;
                            break;
                        }else{
                            Log.d("MusicContentTaker107", "こいつは違う：" +cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                        }
                    }
                    //サポートしてなかったら飛ばす
                    if (!support){
                        continue;
                    }
                }
                Log.d("MusicContentTaker107", cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)));
                //短縮のため
                StringArrayListConverter SAC = new StringArrayListConverter();
                String separator = ";";

                AudioFileInformation record = new AudioFileInformation(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA)), "");
                {//トラック番号・ディスク番号

                    //ドキュメントによれば、千の桁以上がディスク番号、それ以下はトラック番号で格納されているとのこと
                    int discAndTracks = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TRACK));
                    int trackNumber = discAndTracks % 1000;
                    int discNo = discAndTracks / 1000;
                    record.setTrackNumber(trackNumber);
                    record.setDiscNo(discNo);
                }

                record.setNotExistFlag(0);
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.TITLE));
                record.setTitle(SAC.Decode(title, separator));

                record.setArtist(SAC.Decode(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ARTIST)), separator));
                record.setAlbumArtist(SAC.Decode("", separator));

                String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.ALBUM));
                record.setAlbum(SAC.Decode(album, separator));

                record.setGenre(SAC.Decode("", separator));
                record.setYomiTitle(SAC.Decode("", separator));
                record.setYomiArtist(SAC.Decode("", separator));
                record.setYomiAlbum(SAC.Decode("", separator));
                record.setYomiGenre(SAC.Decode("", separator));
                record.setYomiAlbumArtist(SAC.Decode("", separator));


                record.setAddDateTime(cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATE_ADDED)));
                record.setPlaybackCount(0);
                record.setYear(String.valueOf(cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.AudioColumns.YEAR))));
                //機能追加があればここに追記

                res.add(record);
            }
        }

        return res;
    }

}
