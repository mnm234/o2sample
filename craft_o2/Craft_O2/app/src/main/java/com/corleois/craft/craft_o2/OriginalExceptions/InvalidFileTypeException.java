package com.corleois.craft.craft_o2.OriginalExceptions;

/**
 * 良くないファイルだったときに投げられる例外クラス
 */
public class InvalidFileTypeException extends Exception{
    public InvalidFileTypeException(){
        super("読み込めない形式のファイルです。");
    }
}
