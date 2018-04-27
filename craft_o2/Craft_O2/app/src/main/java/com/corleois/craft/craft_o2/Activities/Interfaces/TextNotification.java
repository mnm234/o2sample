package com.corleois.craft.craft_o2.Activities.Interfaces;

/**
 * Created by StarLink on 2017/08/09.
 */

public interface TextNotification {
    /**
     * 対応したアクティビティで、テキスト通知を行います。
     * テキストの内容が空文字、あるいはnullの場合、通知領域は非表示になります。
     *
     * @param message　メッセージ内容
     */
    void showTextMessage(String message);
}
