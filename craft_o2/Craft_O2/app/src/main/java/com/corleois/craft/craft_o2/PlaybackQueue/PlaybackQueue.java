/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.corleois.craft.craft_o2.PlaybackQueue;
import android.app.Application;
import android.util.Log;

import com.corleois.craft.craft_o2.CraftLibrary.CodeCompare;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 *再生待ちのファイルリストオブジェクト。
 * @author StarLink
 */
public class PlaybackQueue extends Application {//AndroidManifest.xmlに android:name=".PlaybackQueue.PlaybackQueue"　を追加
    /**
     * 再生待ちのファイルリスト。ファイルパス、一意なリストID、一意な乱数を持つ。
     */
    private static AudioFileQueueList PlayBackList = new AudioFileQueueList();
    private static AudioFileQueueRecord NowPlaying = null;						//現在再生中のソート後のインデックスを保持します
    private static EnumSet<PlayOrder> Order = EnumSet.noneOf(PlayOrder.class);  //シャッフルやループ再生などの再生要求状態を保持します
    
	
	/**
	 * 再生方法の現在の指定値を取得します。
	 * @return 
	 */
	public static EnumSet<PlayOrder> GetPlayOrder(){return Order;}
	


	public static boolean SetPlayOrder(EnumSet<PlayOrder> order){
		
		//もし、相容れない全曲ループと、一曲ループが同時に指定されたら
		if(order.contains(PlayOrder.LOOP_ALL) && order.contains(PlayOrder.LOOP_SINGLE)){
			return false; //アディオス!!	
		}
		
		//ここに来たときは、大丈夫なパティーン
		//なので、注文を受け付ける
		Order = order;
		
		//シャッフルソート、あるいはそうでないソートを実行する
		if(Order.contains(PlayOrder.SHUFFLE)){
			RandValueSort(SortType.DESC);
		}else{
			ListIDSort(SortType.ASC);
		}
		
		return true;
	}



    /**
     * 現在再生中の曲の順番を返します。シャッフル再生時はソートされた順に並びます。
     * ソートされたリストの、先頭から何曲目を再生しているかを確認できます。先頭の数値は0です。
     * 負数は現在再生できるものがないことを示します。
     * @return 
     */
    public static AudioFileQueueRecord GetNowPlaying(){
        return NowPlaying;
    }

    public String getQueueList(int i) {
        String queueSong = "";
        queueSong = PlayBackList.AudioList.get(i).FilePath;
//        for(int i=0;i<PlayBackList.AudioList.size();i++){
//            String Path = PlayBackList.AudioList.get(i).FilePath;
//            if(Path != null) {
                Log.d("addQueueList",queueSong);
//                queueList.add(Path);
//            }
//        }
        if(queueSong == null){
            return "";
        }
        return PlayBackList.AudioList.get(i).FilePath;
    }

    public void SetNowPlaying(AudioFileQueueRecord record){
		NowPlaying = record;
	}
	
    /**
     * 次に再生する曲のファイルパスを現在の再生要求に基づいて返し、再生キュー側ではその曲が再生されているものと認識します。
     * 現在の曲がリストの後端にある場合、全曲ループモード以外ではnullを返します。
     * @return ファイルパス
     */
    public String GetNextFilePath(){
        //もし、現在再生できるものがない場合
        if(NowPlaying == null && PlayBackList.AudioList.get(0) == null){
            return null;
			
			//再生できるものが追加されていた場合
        }else if(NowPlaying == null && PlayBackList.AudioList.get(0) != null){
			NowPlaying = new AudioFileQueueRecord(PlayBackList.AudioList.get(0));
			return PlayBackList.AudioList.get(0).FilePath;
		}
		
		
        //そうでない場合は次の曲を送る
        //もし、次の曲が存在しない場合
        if(NowPlaying.index + 1 >= PlayBackList.AudioList.size()){
            //さらに条件分岐。
            //もし、ループ再生の場合、（一曲ループ再生は順再生扱いのためFALSE）
            if(Order.contains(PlayOrder.LOOP_ALL)){
                //さらに、ちゃんと曲が入っていれば処理続行
                if(PlayBackList.AudioList.size() > 0) {
                    NowPlaying = new AudioFileQueueRecord(PlayBackList.AudioList.get(0));
                    return PlayBackList.AudioList.get(0).FilePath;
                }else {
                    //曲が空っぽだったら
                    return null;
                }
            }else if(Order.contains(PlayOrder.LOOP_OFF)){
                //順再生の場合はないのでnull
				//NowPlaying = null;
                return null;
            }
        }
        //次の曲が存在する場合
		NowPlaying = PlayBackList.AudioList.get(NowPlaying.index + 1);
        return PlayBackList.AudioList.get(NowPlaying.index).FilePath;
    }
    
    /**
     * 1つ前の曲のファイルパスを現在の再生要求に基づいて返し、再生キュー側ではその曲が再生されているものと認識します。
     * 現在の曲がリストの先頭にある場合、全曲ループモード以外ではnullを返します。
     * @return ファイルパス
     */
    public String GetPreviewFilePath(){
        //もし、現在再生できるものがない場合
        if(NowPlaying == null && PlayBackList.AudioList.get(0) == null){
            return null;
			
			//再生できるものが追加されていた場合
        }else if(NowPlaying == null && PlayBackList.AudioList.get(0) != null){
			NowPlaying = new AudioFileQueueRecord(PlayBackList.AudioList.get(PlayBackList.AudioList.size() - 1));
			return PlayBackList.AudioList.get(PlayBackList.AudioList.size() - 1).FilePath;
		}
		
        //そうでない場合は前の曲を送る
        //もし、前の曲が存在しない場合
        if(NowPlaying.index - 1 < 0){
            //さらに条件分岐。
            //もし、ループ再生の場合、（一曲ループ再生は順再生扱いのためFALSE）
            if(Order.contains(PlayOrder.LOOP_ALL)){
                //さらに、ちゃんと曲が入っていれば処理続行
                if(PlayBackList.AudioList.size() > 0) {
                    NowPlaying = PlayBackList.AudioList.get(PlayBackList.AudioList.size() - 1);
                    return PlayBackList.AudioList.get(PlayBackList.AudioList.size() - 1).FilePath;
                }else {
                    //一曲も入ってなければ当然null
                    return null;
                }
            }else if(Order.contains(PlayOrder.LOOP_OFF)){
                //順再生の場合はないのでnull
				//NowPlaying = null;
                return null;
            }
        }
        //前の曲が存在する場合
		NowPlaying = PlayBackList.AudioList.get(NowPlaying.index - 1);
        return PlayBackList.AudioList.get(NowPlaying.index).FilePath;
    }
    
    /**
     * 次に再生する曲のファイルパスを現在の再生要求に基づいて返します。
     * 現在の曲がリストの後端にある場合、全曲ループモード以外ではnullを返します。
     * @return ファイルパス
     */
    public String FetchNextFilePath(){
        //もし、現在再生できるものがない場合
        if(NowPlaying == null){
            return null;
        }
        //そうでない場合は次の曲を送る
        //もし、次の曲が存在しない場合
        if(NowPlaying.index + 1 >= PlayBackList.AudioList.size()){
            //さらに条件分岐。
            //もし、ループ再生の場合、（一曲ループ再生は順再生扱いのためFALSE）
            if(Order.contains(PlayOrder.LOOP_ALL)){
                return PlayBackList.AudioList.get(0).FilePath;
            }else{
                //順再生の場合はないのでnull
                return null;
            }
        }
        //次の曲が存在する場合
        return PlayBackList.AudioList.get(NowPlaying.index + 1).FilePath;
    }
    
    /**
     * 1つ前の曲のファイルパスを現在の再生要求に基づいて返します。
     * 現在の曲がリストの先頭にある場合、全曲ループモード以外ではnullを返します。
     * @return ファイルパス
     */
    public String FetchPreviewFilePath(){
        //もし、現在再生できるものがない場合
        if(NowPlaying == null){
            return null;
        }
        //そうでない場合は前の曲を送る
        //もし、前の曲が存在しない場合
        if(NowPlaying.index - 1 < 0){
            //さらに条件分岐。
            //もし、ループ再生の場合、（一曲ループ再生は順再生扱いのためFALSE）
            if(Order.contains(PlayOrder.LOOP_ALL)){
                return PlayBackList.AudioList.get(PlayBackList.AudioList.size() -1 ).FilePath;
            }else{
                //順再生の場合はないのでnull
                return null;
            }
        }
        //前の曲が存在する場合
        return PlayBackList.AudioList.get(NowPlaying.index - 1).FilePath;
    }
    
    
//--------------------以下はキューの増減等取り扱い--------------------------
    
    
    /**
     * 再生待ちリストの各レコードが持つシャッフル再生用の乱数が振り直されます。ソートはされません。
     */
    public static void Randomize(){
        
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
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
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
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, r));
        }
    }
    
    /**
     * 再生待ちのリストに一曲追加します。このメソッドが呼び出されると、レコードが持つシャッフル再生用乱数が振り直されます。
     * @param filepath 追加する曲のファイルパス。
     */
    public static void Add(String filepath){
        AudioFileQueueRecord record = new AudioFileQueueRecord(filepath, PlayBackList.AudioList.size());
        record.index = PlayBackList.AudioList.size()+1;
        PlayBackList.AudioList.add(record);
        Randomize();
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
        //もし、再生できるものがない状態のままだったら、更新
/*        if(NowPlaying == null){
            NowPlaying = new AudioFileQueueRecord(PlayBackList.AudioList.get(0));
        }
*/
        Log.d("追加完了",filepath);
    }

    /**
     * 再生待ちのリストに曲リストを追加します。このメソッドが呼び出されると、レコードが持つシャッフル再生用乱数が振り直されます。
     * また、多量の楽曲を入れるとき向けにアルゴリズムが最適化されています。
     * @param fileList
     */
    public static void AddList(ArrayList<String> fileList){
        int size = fileList.size();
        for (int i = 0; i < size; i++) {
            AudioFileQueueRecord record = new AudioFileQueueRecord(fileList.get(i), PlayBackList.AudioList.size());
            PlayBackList.AudioList.add(record);
        }
        //ラムダムに変数を振るのは1回のみ
        Randomize();

        int size2 = PlayBackList.AudioList.size();
        for(int i = 0; i < size2; i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
    }

    
    /**
     * 再生待ちのリストを全クリアします。
     */
    public static void Clear(){
        PlayBackList.AudioList.clear();

        //再生できるものがなくなるので更新
        if (NowPlaying != null) {
            NowPlaying.index = -1;
        }

        //なんかクリアしたときに上でアプリ落ちちゃうのでコメントアウトしたらいけた。
    }
    
    /**
     * 指定されたファイルパスを含むレコードを再生待ちのリストから除去します
     * @param path 除去するファイルパス
     */
    public static void RemoveByFilePath(String path){
        for(int i = 0; i< PlayBackList.AudioList.size(); i++){
            //もし、除去するファイルパスと一致したらTrue
            if(PlayBackList.AudioList.get(i).FilePath.equals(path)){
                PlayBackList.AudioList.remove(i);
                //もし、取り除かれた楽曲が、再生中の楽曲か、それよりもID番号が小さいものだったら
                //再生中のindexを1減らして同調する
                if(NowPlaying.index >= i){
                    NowPlaying.index--;
                }
            }
        }
        //除去したので、先にindexの番号を詰める
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
        
        //まずIDの昇順に
        ListIDSort_NoIndexChange(SortType.ASC);
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath, i, PlayBackList.AudioList.get(i).RandValue, PlayBackList.AudioList.get(i).index));
        }
        indexSort(SortType.ASC);
        
    }
    
    /**
     * 指定されたソート後のインデックスを含むレコードを再生待ちのリストから除去し、インデックスを詰めます
     * @param index  除去するindex
     */
    public static void RemoveByIndex(int index){
        for(int i = 0; i< PlayBackList.AudioList.size(); i++){
            //もし、除去するインデックスと一致したらTrue
            if(PlayBackList.AudioList.get(i).index == index){
                PlayBackList.AudioList.remove(i);

                //もし、取り除かれた楽曲が、再生中の楽曲か、それよりもID番号が小さいものだったら
                //再生中のindexを1減らして同調する
                if(NowPlaying.index >= index){
                    NowPlaying.index--;
                }
            }
        }
        //除去したので、先にindexの番号を詰める
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
        //まずIDの昇順に
        ListIDSort_NoIndexChange(SortType.ASC);
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath, i, PlayBackList.AudioList.get(i).RandValue, PlayBackList.AudioList.get(i).index));
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
	public static void InsertByIndex(String fullPath,int index){
		PlayBackList.Add(fullPath);
		ExchangeByIndex(index, PlayBackList.AudioList.size() - 1);
	}
	
	/**
	 * リストの曲A,Bを交換します。存在しない曲数が指定された場合は先頭または後端に割り当てられます
	 * @param indexA
	 * @param indexB 
	 */
	public static void ExchangeByIndex(int indexA, int indexB){
		
		//0以下の場合は先頭になるように
		indexA = (indexA < 0) ? 0 : indexA;
		indexB = (indexB < 0) ? 0 : indexB;
		
		//リストの曲数以上の場合は後端になるように
		indexA = (indexA >= PlayBackList.AudioList.size()) ? PlayBackList.AudioList.size() -1 : indexA;
		indexB = (indexB >= PlayBackList.AudioList.size()) ? PlayBackList.AudioList.size() -1 : indexB;
		
		//交換
		String temp;
		temp = PlayBackList.AudioList.get(indexA).FilePath;
		PlayBackList.AudioList.get(indexA).FilePath = PlayBackList.AudioList.get(indexB).FilePath;
		PlayBackList.AudioList.get(indexB).FilePath = temp;

        //もし、交換したのが再生中の楽曲だったら、indexも交換
        if(NowPlaying.index == indexA){
            NowPlaying.index = indexB;

        }else if(NowPlaying.index == indexB){
            NowPlaying.index = indexA;
        }
	}

    /**
     * リスト中の任意のインデックスのレコードを返します。インデックスが不正の場合Nullを返します。
     * @param index
     * @return
     */
    public static AudioFileQueueRecord GetRecord(int index){
        return (index < 0 || index >= PlayBackList.AudioList.size()) ? null : PlayBackList.AudioList.get(index);
    }

    /**
     * リスト中の任意のインデックスのレコードを上書きします。インデックスが不正の場合Nullを返します。
     * @param index
     * @return
     */
    public static void SetRecord(int index, AudioFileQueueRecord record){
        PlayBackList.AudioList.set(index,record);
    }

    /**
     * 再生待ちリストの曲数を返します。
     * @return 
     */
    public static int Count(){
        return PlayBackList.AudioList.size();
    }
    
    /**
     * 追加順に付与されるListIDによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    public static void ListIDSort(SortType sort){

        //とりま至急なのでバブルソートで実装する　2017年5月23日
        //昇順ソート
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).ListID > PlayBackList.AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).ListID < PlayBackList.AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }

        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
    }
    
    /**
     * 追加順に付与されるListIDによって曲をソートします。ただし、indexの数字は変化しません。内部クラス用です
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    private static void ListIDSort_NoIndexChange(SortType sort){
        //とりま至急なのでバブルソートで実装する　2017年5月23日
        //昇順ソート
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).ListID > PlayBackList.AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).ListID < PlayBackList.AudioList.get(j + 1).ListID) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
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
    public static void RandValueSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).RandValue > PlayBackList.AudioList.get(j + 1).RandValue) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                if (PlayBackList.AudioList.get(j).RandValue < PlayBackList.AudioList.get(j + 1).RandValue) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
        for(int i = 0; i < PlayBackList.AudioList.size(); i++){
            PlayBackList.AudioList.set(i, new AudioFileQueueRecord(PlayBackList.AudioList.get(i).FilePath,PlayBackList.AudioList.get(i).ListID, PlayBackList.AudioList.get(i).RandValue, i));
        }
        
    }
    
    /**
     * 追加順に付与されるindexによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
    private static void indexSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).index > PlayBackList.AudioList.get(j + 1).index) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (PlayBackList.AudioList.get(j).index < PlayBackList.AudioList.get(j + 1).index) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
    }

    /**
     * ファイルパスの文字コードによって曲をソートします。
     * @param sort ソート順をSortType列挙型で指定します。昇順と降順が指定できます。
     */
	public static void FilePathSort(SortType sort){
        boolean changed = true;

        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (CodeCompare.StringA_minus_B(PlayBackList.AudioList.get(j).FilePath,PlayBackList.AudioList.get(j + 1).FilePath) > 0) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < PlayBackList.Count() - 1; j++) {
                    if (CodeCompare.StringA_minus_B(PlayBackList.AudioList.get(j).FilePath,PlayBackList.AudioList.get(j + 1).FilePath) < 0) {
                        AudioFileQueueRecord temp;
                        temp = PlayBackList.AudioList.get(j);
                        PlayBackList.AudioList.set(j, PlayBackList.AudioList.get(j + 1));
                        PlayBackList.AudioList.set(j + 1, temp);
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
    public static String[] GetAllFilePathOfCurrentPlayBackList(){
        //現在のリストの数だけの配列を持つ
        String filelist[] = new String[PlayBackList.AudioList.size()-1];
		
		//ファイルパスのリストを現在の再生順に転記する
		for (int i = 0; i < filelist.length; i++) {
			filelist[i] = PlayBackList.AudioList.get(i).FilePath;
		}
        return filelist;
    }
    
}
