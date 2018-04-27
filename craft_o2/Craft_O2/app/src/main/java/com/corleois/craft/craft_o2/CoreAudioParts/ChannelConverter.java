package com.corleois.craft.craft_o2.CoreAudioParts;

/**
 * Created by StarLink on 2017/08/23.
 */

/**
 * ステレオに変換するクラスです
 */
public class ChannelConverter {

    /**
     * モノラルからステレオに単純に変換します。16bit整数専用です
     * @param data モノラルデータ
     * @return ステレオデータ
     */
    public static byte[] ConvMonoToStereo(byte[] data){
        //モノラルからステレオなら、バイトの長さは倍！
        byte[] ret = new byte[data.length * 2];

        int size = data.length;
        //ステレオ化するぜ！
        for (int i = 0; i < size; i += 2) {
            //片側にデータを詰めて
            ret[i + i] = data[i];
            ret[i + i + 1] = data[i + 1];

            //もう片側にもデータを詰める
            ret[i + i + 2] = data[i];
            ret[i + i + 3] = data[i + 1];
        }
        return ret;
    }
}
