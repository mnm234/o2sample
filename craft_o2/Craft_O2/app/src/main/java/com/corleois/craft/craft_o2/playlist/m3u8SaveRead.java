package com.corleois.craft.craft_o2.playlist;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 2150087 on 2017/06/20.
 */
//このクラスはリストの保存、読込に使用する
public class m3u8SaveRead extends Activity {
    //コンテキストを受け取る。MainActivity以外で読み書きするのに必要。
    private Context c;

    public m3u8SaveRead(Context c){
        this.c = c;

    }

    /**
     *
     * @param filePathList リスト本体
     * @param listname リストの名前
     * @return
     * 送られてきたリストを保存する
     */
    public int saveList(ArrayList<String> filePathList, String listname){

        //バリデーションチェック
        //使えない文字が入っていないかチェック。あったらTrue
        if(listname.matches("^.*[\\\\|/|:|\\*|?|\"|<|>|\\|].*$")){
            return INVALID_NAME;
        }



        FileOutputStream fos ;

        try{
            fos = c.openFileOutput(listname + ".m3u8", Context.MODE_PRIVATE);
            //もし、リストがnullでないならば実行
            if(filePathList != null) {
                //ArrayListのそれぞれのインデックスにおいてマップに格納されたキー値を保存
                for (int i = 0; i < filePathList.size(); i++) {
                    //アルバムアートのファイル名
//                String fileName = filePathList.get(i).get("title").toString() + filePathList.get(i).get("artist").toString();
//                File file = new File(c.getExternalCacheDir (), fileName);
                    //画像を保存するストリーム
//                FileOutputStream fileOutputStream = new FileOutputStream(file);
                    //アルバムアートの画像を保存
//                Bitmap image = (Bitmap) filePathList.get(i).get("albumArt");
//
//                image.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
//                fileOutputStream.close();
//
//                //タイトルとアーティスト名を保存
//                String str;
//                str = filePathList.get(i).get("title").toString() + "\n";
//                fos.write(str.getBytes());
//                str = filePathList.get(i).get("artist").toString() + "\n";
//                fos.write(str.getBytes());

                    //一件データに改行を付して書き込み
                    String record = filePathList.get(i) + "\n";
                    fos.write(record.getBytes());
                }
            }
            fos.close();
            return 0;
        }catch(IOException e){

            return -4;
        }
    }


    public int saveList(ArrayList<String> filePathList, String listname, boolean allowOverWrite){
        //バリデーションチェック
        //使えない文字が入っていないかチェック。あったらTrue
        if(listname.matches("^.*[\\\\|/|:|\\*|?|\"|<|>|\\|].*$")){
            return INVALID_NAME;
        }

        {
            //新しい名前がすでに存在していないかチェック
            File file = new File(this.c.getFilesDir().getPath() + "/" + listname + ".m3u8");
            if (file.exists() && (!allowOverWrite)) {
                //存在してて、かつ上書きが許されていない場合バイバイ
                return ALREADY_EXISTS;
            }
        }
        FileOutputStream fos ;

        try{
            fos = c.openFileOutput(listname + ".m3u8", Context.MODE_PRIVATE);
            //もし、リストがnullでないならば実行
            if(filePathList != null) {
                //ArrayListのそれぞれのインデックスにおいてマップに格納されたキー値を保存
                for (int i = 0; i < filePathList.size(); i++) {

                    //一件データに改行を付して書き込み
                    String record = filePathList.get(i) + "\n";
                    fos.write(record.getBytes());
                }
            }
            fos.close();
            return 0;
        }catch(IOException e){

            return -4;
        }

    }

    /**
     *
     * @param listname  開くリストの名前
     * @return
     * 特定の名前のリストを開く
     */
    public ArrayList<String> openList(String listname){
        InputStream inputStream;

        String lineBuffer;
        //読み込んだリスト
        ArrayList<String> getList = new ArrayList<>();

   try {


       inputStream = c.openFileInput(listname + ".m3u8");

            BufferedReader reader= new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));
            while( (lineBuffer = reader.readLine()) != null ){
//                Map<String, Object> data = new HashMap();
//                data.put("title", lineBuffer.toString());
//                lineBuffer = reader.readLine();
//                data.put("artist", lineBuffer.toString());
//
//                InputStream imageinput = openFileInput(data.get("title").toString() + data.get("artist").toString() + ".png");
//                Bitmap bitmap = BitmapFactory.decodeStream(imageinput);
//                data.put("albumArt", bitmap);
//                getList.add(data);

                //もしも、行頭が#、つまりコメント行ではない場合のみ
                if(lineBuffer.charAt(0) != '#') {
                    //一行読み取ったバッファを返却用のArrayListに追加
                    getList.add(new String(lineBuffer));
                }
            }
            inputStream.close();
            //リストを返す
            return getList;

        }catch(FileNotFoundException e){

            e.printStackTrace();

            return getList;
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
            //例外発生時
            
            //return (ArrayList<Map<String, Object>>) getList.clone();
            return getList;
        }
    }

    /**
     * 引数で指定されたプレイリストを削除します
     * @param listName 削除するプレイリスト
     * @return 削除成功可否
     */
    public boolean deleteList(String listName){
        //Log.d("DELETELIST",(listName == null ? "ぬるぬる" : "あったー" + listName));
        return c.deleteFile(listName + ".m3u8");
    }



    public static final int INVALID_NAME = -1;      //無効な名前
    public static final int ALREADY_EXISTS = -2;    //既に存在する
    public static final int SAME_NAME = -3;         //引数の名前が2つとも同じ

    /**
     * プレイリストの名前を変更します。
     * @param oldName   変更するプレイリストの名前
     * @param newName   変更後のプレイリストの名前
     * @param allowOverWrite 既にファイルがあった場合、上書きを許可するかどうか True:許可
     * @return  変更成功可否 成功：0
     */
    public int renamePlayList(String oldName, String newName, boolean allowOverWrite){

        //プレイリストをコピー、のち削除
        //リネーム前のプレイリストのファイルパスを全件取ってくる

        int ret = copyList(oldName,newName,allowOverWrite);

        if(ret == 0){
            deleteList(oldName);
            return 0;
        }else {
            return ret;
        }
    }

    /**
     * プレイリストを複製します
     * @param baseName 元のプレイリスト名
     * @param newName  新規に作成するプレイリスト名
     * @param allowOverWrite 既にファイルがあった場合、上書きを許可するかどうか
     * @return 変更成功可否 成功:0
     */
    public int copyList(String baseName, String newName, boolean allowOverWrite){
        //バリデーションチェック
        //使えない文字が入っていないかチェック。あったらTrue
        if(newName.matches("^.*[\\\\|/|:|\\*|?|\"|<|>|\\|].*$")){
            return INVALID_NAME;
        }

        //元の名前も新しい名前も同一なら
        if(baseName.equals(newName)){
            //処理する必要なし、というかコピーできないので退却
            return SAME_NAME;
        }

        {
            //新しい名前がすでに存在していないかチェック
            File file = new File(this.c.getFilesDir().getPath() + "/" + newName + ".m3u8");
            if (file.exists() && (!allowOverWrite)) {
                //存在してて、かつ上書きが許されていない場合バイバイ
                return ALREADY_EXISTS;
            }
        }

        //プレイリストをコピー
        ArrayList<String> records = openList(baseName);
        saveList(records, newName);

        return 0;
    }
}
