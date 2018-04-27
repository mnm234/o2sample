package com.corleois.craft.craft_o2.OriginalExceptions;


/**
 * Created by 2150254 on 2017/06/20.
 */

public class GabagabaArgumentException extends IllegalArgumentException {

    /**
     * コンストラクタ。
     * 引数がガバガバだったときに使います。
     */
    public GabagabaArgumentException(){
        super("引数がガバガバです");
    }

    /**
     * メッセージを代入できるコンストラクタ
     * 引数がガバガバだったときに使います。
     * @param message
     */
    public GabagabaArgumentException(String message){
        super("引数がガバガバです："+(message));
    }
}
