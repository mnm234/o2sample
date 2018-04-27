package com.corleois.craft.craft_o2.Activities.Interfaces;

/**
 * Created by StarLink on 2017/08/12.
 */

import android.media.MediaFormat;

/***
 * デコーダーからのイベントを受け取るインターフェイス
 */
public interface CoreDecorderNotyfication {
    void onOutPutFormatChanged(MediaFormat format);
}
