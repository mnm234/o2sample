package com.corleois.craft.craft_o2.OriginalExceptions;

/**
 * Created by StarLink on 2017/07/29.
 */

public class CompareFailedException extends Exception {

    public CompareFailedException(){
        super("比較に失敗しました");
    }

    public CompareFailedException(String message){
        super("比較に失敗しました:" + (message));
    }

}
