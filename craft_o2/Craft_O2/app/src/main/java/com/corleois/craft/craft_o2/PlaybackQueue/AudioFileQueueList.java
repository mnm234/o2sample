/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.corleois.craft.craft_o2.PlaybackQueue;

import com.corleois.craft.craft_o2.CraftLibrary.SortType;

import java.util.ArrayList;

/**
 *再生する音声ファイルのリストを保持するクラス
 * @author StarLink
 */
public class AudioFileQueueList {
    //再生リスト
    public ArrayList<AudioFileQueueRecord> AudioList;
    
    /**
     * コンストラクタ
     */
    public AudioFileQueueList() {
        this.AudioList = new ArrayList<>();
    }
    /**
     *コンストラクタ（配列リストがあるとき） 
     * @param array 再生リスト
     */
    public AudioFileQueueList(ArrayList<AudioFileQueueRecord> array){
        this.AudioList = array;
    }
    
	//--------------------以下はキューの増減等取り扱い--------------------------
    
    
    /**
     * 再生待ちリストの各レコードが持つシャッフル再生用の乱数が振り直されます。ソートはされません。
     */
    public void Randomize(){
        
        /*0-掛けた数未満の正の整数を作る
        * とりうる値は、符号付き32ビット整数のうち、0～正の最大値の25%～75%にあたる数。
        * 正の整数の残りの50%分は、それぞれ乱数よりも先に再生したいもの、あとで再生したいものを手動で指定できるように予約。
        * 0未満の負の数は未使用。
        */
        int r;
        
        /*使用済み整数をこっちへ格納する。
        * 線形探索ではO(n)の計算時間が必要。この方式では同じ線形探索でもO(n/2)の計算量になるはず
        * オブジェクトのアクセス時間を考慮すると、こちらのほうが省電力と予想。
        */
        ArrayList<Integer> usedRandValue = new ArrayList<>();
        boolean DoubleBooking;
        
        //リストの1つごとの処理のループ
        for(int i = 0; i < this.AudioList.size(); i++){
            do {//ダブらない数が出るまでループ
                r = (int) ((Math.random()*Integer.MAX_VALUE / 2) + (Integer.MAX_VALUE / 4));
                DoubleBooking = false;
                
                //使用済みの値でないか検査
                for(int j = 0; j < usedRandValue.size();j++){
                    if(usedRandValue.get(i) == r){
                        DoubleBooking = true;
                    }
                }
            } while (DoubleBooking);
            
            //ここに来たときはrはダブってない
            //乱数の値をset☆
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, r));
        }
    }
    
    /**
     * 再生待ちのリストに一曲追加します。このメソッドが呼び出されると、レコードが持つシャッフル再生用乱数が振り直されます。
     * @param filepath 追加する曲のファイルパス。
     */
    public void Add(String filepath){
        AudioFileQueueRecord record = new AudioFileQueueRecord(filepath, this.AudioList.size());
        record.index = this.AudioList.size()+1;
        this.AudioList.add(record);
        Randomize();
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, this.AudioList.get(i).RandValue, i));
        }
    }
    
    /**
     * 再生待ちのリストを全クリアします。
     */
    public void Clear(){
        this.AudioList.clear();
        
    }
    
    /**
     * 指定されたファイルパスを含むレコードを再生待ちのリストから除去します
     * @param path 除去するファイルパス
     */
    public void RemoveByFilePath(String path){
        for(int i = 0; i< this.AudioList.size(); i++){
            //もし、除去するファイルパスと一致したらTrue
            if(this.AudioList.get(i).FilePath.equals(path)){
                this.AudioList.remove(i);
            }
        }
        //除去したので、先にindexの番号を詰める
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, this.AudioList.get(i).RandValue, i));
        }
        
        //まずIDの昇順に
        ListIDSort_NoIndexChange(SortType.ASC);
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath, i, this.AudioList.get(i).RandValue, this.AudioList.get(i).index));
        }
        indexSort(SortType.ASC);
        
    }
    
    /**
     * 指定されたソート後のインデックスを含むレコードを再生待ちのリストから除去し、インデックスを詰めます
     * @param index  除去するindex
     */
    public void RemoveByIndex(int index){
        for(int i = 0; i< this.AudioList.size(); i++){
            //もし、除去するインデックスと一致したらTrue
            if(this.AudioList.get(i).index == index){
                this.AudioList.remove(i);
            }
        }
        //除去したので、先にindexの番号を詰める
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, this.AudioList.get(i).RandValue, i));
        }
        //まずIDの昇順に
        ListIDSort_NoIndexChange(SortType.ASC);
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath, i, this.AudioList.get(i).RandValue, this.AudioList.get(i).index));
        }
        indexSort(SortType.ASC);
    }
    
		/**
	 * indexの番号に曲を挿入します。元のindexの値以上の曲は、元のindex番号より1大きくなります。
	 * 0以下の値が指定されると、リストの先頭に挿入されます。
	 * リストの曲数以上の値が指定されると、リストの後端に挿入されます。
	 * @param fullPath
	 * @param index 
	 */
	public void InsertByIndex(String fullPath,int index){
		this.Add(fullPath);
		ExchangeByIndex(index, this.AudioList.size() - 1);
	}
	
	/**
	 * リストの曲A,Bを交換します。存在しない曲数が指定された場合は先頭または後端に割り当てられます
	 * @param indexA
	 * @param indexB 
	 */
	public void ExchangeByIndex(int indexA, int indexB){
		
		//0以下の場合は先頭になるように
		indexA = indexA < 0 ? 0 : indexA;
		indexB = indexB < 0 ? 0 : indexB;
		
		//リストの曲数以上の場合は後端になるように
		indexA = indexA >= this.AudioList.size() ? this.AudioList.size() -1 : indexA;
		indexB = indexB >= this.AudioList.size() ? this.AudioList.size() -1 : indexB;
		
		//交換
		String temp;
		temp = this.AudioList.get(indexA).FilePath;
		this.AudioList.get(indexA).FilePath = this.AudioList.get(indexB).FilePath;
		this.AudioList.get(indexB).FilePath = temp;
	}
    /**
     * リスト中の任意のインデックスのレコードを返します。インデックスが不正の場合Nullを返します。
     * @param index
     * @return 
     */
    public AudioFileQueueRecord GetRecord(int index){
        return (index < 0 || index >= this.AudioList.size()) ? null : this.AudioList.get(index);
    }
    
    /**
     * リストの曲数を返します。
     * @return 
     */
    public int Count(){
        return this.AudioList.size();
    }
    
    /**
     * 追加順に付与されるListIDによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    public void ListIDSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).ListID > AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).ListID < AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, this.AudioList.get(i).RandValue, i));
        }
    }
    
    /**
     * 追加順に付与されるListIDによって曲をソートします。ただし、indexの数字は変化しません。内部クラス用です
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    private void ListIDSort_NoIndexChange(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).ListID > AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).ListID < AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
    }
    
    /**
     * 追加順に付与されるRandValueによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    public void RandValueSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).RandValue > AudioList.get(j + 1).RandValue) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).RandValue < AudioList.get(j + 1).RandValue) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
        for(int i = 0; i < this.AudioList.size(); i++){
            this.AudioList.set(i, new AudioFileQueueRecord(this.AudioList.get(i).FilePath,this.AudioList.get(i).ListID, this.AudioList.get(i).RandValue, i));
        }
        
    }
    
    /**
     * 追加順に付与されるindexによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    private void indexSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).index > AudioList.get(j + 1).index) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < AudioList.size() - 1; j++) {
                    if (AudioList.get(j).index < AudioList.get(j + 1).index) {
                        AudioFileQueueRecord temp;
                        temp = AudioList.get(j);
                        AudioList.set(j, AudioList.get(j + 1));
                        AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
    }
    
    /**
	 * 現在の再生待ちリストをソート後の並びで返します
	 * @return 文字列配列
	 */
    public String[] GetAllFilePathOfCurrentthis(){
        //現在のリストの数だけの配列を持つ
        String filelist[] = new String[this.AudioList.size()-1];
		
		//ファイルパスのリストを現在の再生順に転記する
		for (int i = 0; i < filelist.length; i++) {
			filelist[i] = this.AudioList.get(i).FilePath;
		}
        return filelist;
    }
   
}

