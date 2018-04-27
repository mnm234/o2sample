package com.corleois.craft.craft_o2.playlist;

import android.app.Activity;
import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by 2150087 on 2017/06/13.
 */


public class m3u8 extends Activity{
    //元のプレイリスト
    ArrayList<String> fileList = new ArrayList<String>();
    //コメント削除後のリストの一時的な保存に使うプレイリスト
    ArrayList<String> outputList = new ArrayList<String>();


    /**
     *
     * @param playList 読み込ませたいプレイリスト
     *                 プレイリストを初期化コンストラクタ
     */

    //コンストラクタ
    public m3u8(ArrayList<String> playList){

        this.fileList = playList;
    }

    public m3u8(){

    }
    //プレイリストを上書きする
    public void changPlayList(ArrayList<String> list){
        //上書き前にクリア
        fileList.clear();

        fileList = (ArrayList<String>) list.clone();

    }




    /**
     *
     * @return
     * オリジナルのリストを返す
     */
    public ArrayList<String> originalList(){
        return (ArrayList<String>) fileList.clone();
    }




    /**
     *
     * @return
     * プレイリストからコメント文を取り除く
     */


    public ArrayList<String>  extractMediaFile(){

        int length = fileList.size();

        outputList.clear();

        for(int i = 0; i < length; i++){
            //コメント文が含まれていたら(先頭が#)、飛ばす
            if(fileList.get(i).substring(0,1).contains("#") ){

            }else{
                //コメント文でなかったら、アウトプットに格納
                outputList.add(fileList.get(i));
            }
        }
        //コメント文なしのリストを返す
        return (ArrayList<String>) outputList.clone();
    }


    /**
     *
     * @param string
     * プレイリストに追加するファイル名(1曲)
     */
    public void addList(String string){
        fileList.add(string);

    }


    /**
     *
     * @param string
     * リストから曲を削除する
     */
    public void removeList(String string){

        for(int i = 0; i < fileList.size(); i++){
            if(fileList.get(i).equals(string)){
                fileList.remove(i);
            }
        }
    }


}