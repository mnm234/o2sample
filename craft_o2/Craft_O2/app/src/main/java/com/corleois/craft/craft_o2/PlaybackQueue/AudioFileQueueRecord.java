/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.corleois.craft.craft_o2.PlaybackQueue;

/**
 *再生する音声ファイルの1レコードを定義するクラス
 * @author StarLink
 */
public class AudioFileQueueRecord {
    public String FilePath; //ファイルパス名
    public int ListID;      //追加順の番号
    public int RandValue;   //整数の乱数
    public int index;       //ソート後の順番
    
    /**
     * コンストラクタ
     * @param FilePath 再生する音声ファイルのパス
     * @param ListID 　リスト内の通し番号
     */
    public AudioFileQueueRecord(String FilePath, int ListID){
        this.FilePath = FilePath;
        this.ListID = ListID;
        this.RandValue = 0;
        this.index = 0;
    }
    
    /**
     * コンストラクタ
     * @param FilePath  再生する音声ファイルのパス
     * @param ListID    リスト内の通し番号
     * @param RandValue ランダム再生用の乱数 
     */
    public AudioFileQueueRecord(String FilePath, int ListID,int RandValue){
        this.FilePath = FilePath;
        this.ListID = ListID;
        this.RandValue = RandValue;
        this.index = (this.index == 0) ? 0 : this.index;
    }
    
    /**
     * コンストラクタ
     * @param FilePath  再生する音声ファイルのパス
     * @param ListID    リスト内の通し番号
     * @param RandValue ランダム再生用の乱数 
     * @param index    ソートされた再生順番号
     */
    public AudioFileQueueRecord(String FilePath, int ListID,int RandValue,int index){
        this.FilePath = FilePath;
        this.ListID = ListID;
        this.RandValue = RandValue;
        this.index = index;
    }
    
    /**
     * コンストラクタ
	 * 
	 * @param record
     */
    public AudioFileQueueRecord(AudioFileQueueRecord record){
        this.FilePath = record.FilePath;
        this.ListID = record.ListID;
        this.RandValue = record.RandValue;
        this.index = record.index;
    }
    
}
