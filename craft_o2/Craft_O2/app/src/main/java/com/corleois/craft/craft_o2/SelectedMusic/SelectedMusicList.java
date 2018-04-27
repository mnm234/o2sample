package com.corleois.craft.craft_o2.SelectedMusic;

/**
 * Created by StarLink on 2017/08/05.
 */

import android.support.annotation.NonNull;

import com.corleois.craft.craft_o2.PlaybackQueue.AudioFileQueueList;
import java.util.ArrayList;

/**
 * 選択された楽曲のリストを、リスト名ごとに持つクラスです
 */
public class SelectedMusicList{

    private static ArrayList<AudioFileQueueList> selectedList = new ArrayList<>();
    private static ArrayList<String> listName = new ArrayList<>();

    /**
     * 選択リストに、楽曲を追加します。リスト名が存在しない場合、追加処理は行われません。
     * @param list 追加したい選択リスト名
     * @param filePath 追加したいファイルパス
     * @return 追加処理を行った：true 行わなかった:false
     */
    public static boolean addSelectedfFile(String list, String filePath){

        //リスト名があるかどうか探す。なければfalse
        int i = getlistNamePosition(list);
        if(i < 0){
            return false;
        }

        //ここに来たときは、リストに追加する
        selectedList.get(i).Add(filePath);
        return true;
    }

    /**
     * 選択リストから、楽曲を取り除きます。楽曲が存在しない場合、削除処理は行われません。
     * @param list 処理を行いたいリスト名
     * @param filePath 取り除きたいファイルパス
     * @return 処理を行った：true 行わなかった:false
     */
    public static boolean removeSelectedFile(String list, String filePath){
        //リスト名があるかどうか探す。なければfalse
        int i = getlistNamePosition(list);
        if(i < 0){
            return false;
        }
        //ここに来たときは、リストから取り除く
        selectedList.get(i).RemoveByFilePath(filePath);
        return true;
    }

    /**
     * 新しい選択済みリストを作成します。既に存在しているときは新規作成されません
     * @param list 新規作成したいリスト名
     * @return 新規作成された:true されなかった:false
     */
    public static boolean createNewSelectedList(String list){
        //もし、そのリスト名がすでに存在していたら、何もせず終わる
        int listSize = listName.size();
        for (int i = 0; i < listSize; i++) {
            if(listName.get(i).equals(list)){
                return false;
            }
        }

        //ここに来たときは新規作成が必要なパターン
        selectedList.add(new AudioFileQueueList());
        listName.add(list);
        return true;
    }

    /**
     * 指定された選択済みリストを削除します。存在しない場合は何も行われません。
     * このメソッドが呼び出されて以後、同名のリスト名で作成しない限り、同名のリストに対する操作は行えません。
     * @param list 削除したいリスト名
     */
    public static void removeSelectedList(String list){
        int i = getlistNamePosition(list);
        if(i < 0){
            return;
        }
        //ここに来たら削除処理が必要
        listName.remove(i);
        selectedList.remove(i);
    }

    /**
     * 指定された選択済みリストをリセットし、選択中として登録されているファイルパスがすべて取り除かれます。存在しない場合は何も行われません。
     * @param list リセットしたいリスト名
     */
    public static void resetList(String list){
        int i = getlistNamePosition(list);
        if(i < 0){
            return;
        }
        //ここに来たときは処理が必要
        selectedList.set(i, new AudioFileQueueList());
    }

    /**
     * すべてのリスト、すべての選択中として登録されているファイルを削除します。
     */
    public static void deleteAllSelectedList(){
        selectedList = new ArrayList<>();
        listName = new ArrayList<>();
    }


    /**
     * 引数のリスト名の選択リストが存在するかを返します。
     * @param list 調べたいリスト名
     * @return 存在する:true 存在しない:false
     */
    public static boolean isListNameExists(String list){
        return getlistNamePosition(list) >= 0;
    }

    /**
     * 引数の選択リストを返します。
     * @param list
     * @return
     */
    public static AudioFileQueueList getSelectedList(String list){
        int i = getlistNamePosition(list);
        if(i < 0){
            return null;
        }

        return selectedList.get(i);
    }

    /**
     * 「保持されているリストの名前」のリストを返します。
     * @return
     */
    public static ArrayList<String> getListName() {
        return listName;
    }

    /**
     * 指定されたリストの中に、指定された楽曲があるか探します。
     * 存在すればtrue、そうでないとき/リストが存在しないときはfalseです。
     * @param list
     * @param filePath
     * @return
     */
    public static boolean contains(@NonNull String list,@NonNull String filePath){
        int pos = getlistNamePosition(list);
        //リストがなかったらF
        if(pos < 0){
            return false;
        }


        boolean res = false;
        AudioFileQueueList queueList = getSelectedList(list);
        {
            //キューリストがnullだったらグッバイ
            if(queueList == null){
                return false;
            }
            //リストを探して同じのがあったらtrueになる
            for (int i = 0; i < queueList.Count(); i++) {
                if(queueList.GetRecord(i).FilePath.equals(filePath)){
                    res = true;
                }
            }
        }
        return res;
    }

    //----------------------------------------------
    /**
     * リスト名のArrayListから、引数のリスト名に対応する要素番号を調べる内部メソッドです。
     * 存在しない場合は負数が返ります。
     * @param list 検索したいリスト名
     * @return 要素番号
     */
    private static int getlistNamePosition(String list){
        //リスト名がすでにあるかどうかを探す
        int listSize = listName.size();

        //中身空っぽなら、要素番号は負数
        if(listSize == 0){
            return -1;
        }
        //リストの中で、一致しない間ループ文を回す
        int i;
        for (i = 0; (i < listSize && !listName.get(i).equals(list)); i++);
        //もし、リストの範囲を超えていたら、その時点で追加不可なので終了
        if(i >= listSize){
            return -1;
        }

        return i;
    }

}
