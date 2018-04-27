package com.corleois.craft.craft_o2.MetaData;

/**
 * Created by StarLink on 2017/08/07.
 */

import java.util.ArrayList;

/**
 * AudioFileInformationのリストのグループを管理するオブジェクトです
 */
public class AudioGroupInformation {

    private ArrayList<AudioFileInformation> songList;
    private AudioGroupInformation parentGroupInformation;
    private ArrayList<AudioGroupInformation> childGroupInformation;
    private String groupName;

    /**
     * 子グループの楽曲リストをすべて取得します。
     * @param includeThisObject このメソッドを呼び出した最初のオブジェクトを含めるかどうか
     * @return 子グループが持つ全ての楽曲リスト
     */
    public ArrayList<AudioFileInformation> getChildGroupAFI(boolean includeThisObject){
        ArrayList<AudioFileInformation> arrayList = new ArrayList<>();

        //もし、子要素があれば
        if(childGroupInformation != null && childGroupInformation.size() > 0) {
            //すべての子要素を探す
            for (int i = 0; i < childGroupInformation.size(); i++) {
                ArrayList<AudioFileInformation> temp = childGroupInformation.get(i).getChildGroupAFI(true);

                //もし、nullではなく、かつちゃんと中身が入っていればtrue
                if (temp != null && temp.size() > 0) {
                    int size = temp.size();
                    for (int j = 0; j < size; j++) {
                        arrayList.add(temp.get(j));
                    }
                }
            }
        }

        //このオブジェクトも結果に含めるときで、かつデータがあれば
        if(includeThisObject && songList != null && songList.size() > 0){
            int size = songList.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(songList.get(i));
            }
        }

        return arrayList;
    }

    /**
     * このグループの名前を取得します
     * @return
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * このグループの名前を設定します
     * @param groupName
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * このグループの親グループを取得します
     * @return
     */
    public AudioGroupInformation getParentGroupInformation() {
        return parentGroupInformation;
    }

    /**
     * このグループの親グループを設定します
     * @param parentGroupInformation
     */
    public void setParentGroupInformation(AudioGroupInformation parentGroupInformation) {
        this.parentGroupInformation = parentGroupInformation;
    }

    /**
     * このグループの子グループを設定します
     * @param childGroupInformation
     */
    public void setChildGroupInformation(ArrayList<AudioGroupInformation> childGroupInformation) {
        this.childGroupInformation = childGroupInformation;
    }

    /**
     * このグループの子グループを取得します
     * @return
     */
    public ArrayList<AudioGroupInformation> getChildGroupInformation() {
        return childGroupInformation;
    }

    /**
     * このグループに所属している楽曲リストを設定します
     * @param songList
     */
    public void setSongList(ArrayList<AudioFileInformation> songList) {
        this.songList = songList;
    }

    /**
     * このグループに所属している楽曲リストを取得します
     * @return
     */
    public ArrayList<AudioFileInformation> getSongList() {
        return songList;
    }
}
