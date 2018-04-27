package com.corleois.craft.craft_o2.CoreAudioParts;

/**
 * Created by StarLink on 2017/06/18.
 */

/**
 * 片耳イヤホンモードの実装
 */
public class MonoralGenerator {
    public static final int OUT_LEFT_ONLY = 1;
    public static final int OUT_RIGHT_ONLY = 2;
    public static final int BOTH = 3;
    /**
     * ステレオ音声をモノラルに変換します。
     * @param Type  左右片方ずつ、または両方を選択できます。
     * @return
     */
    public static byte[] StereoToMonoral(byte[] input,int Type){
        //返す値の配列を作成
        byte[] output = new byte[input.length];
        int start = 0;
        switch (Type){
            case MonoralGenerator.OUT_RIGHT_ONLY:
                start = 2;
                break;
            case MonoralGenerator.OUT_LEFT_ONLY:
                start = 0;
                break;
            case MonoralGenerator.BOTH:
                start = -1;
            default:
                return input;
        }

        //16bit音声専用
        //まずモノラル化。16bitPCMはリトルエンディアン
        for(int i = 0; i < input.length; i+=4){
            int sample;
            int sample2;
            int sum;

            //前者は小さな音、後者は大きな音

            // そのまま加算すると、16bitの表現範囲を超えてしまうので、
            // やむなく15bitまで解像度を落とす
            sample = ((input[i] >>> 1) & 0xFF);
            sample = (sample | (((input[i+1] >>> 1) & 0xFF) << 8));
            sample2 = ((input[i+2] >>> 1) & 0xFF);
            sample2 = (sample2 | (((input[i+3] >>> 1) & 0xFF) << 8));

            //加算することでモノラル16bitの解像度になる
            sum = sample + sample2;

            sample = sum;
            sample2 = sum;

            output[i] = (byte)(sample & 0xFF);
            output[i+1] = (byte)((sample >> 8) & 0xFF);
            output[i+2] = (byte)(sample2 & 0xFF);
            output[i+3] = (byte)((sample2 >> 8) & 0xFF);
        }

        //もし、モノラル両方出力でなかったらT
        if(start >= 0){
            //片耳だけ出力を0に
            for(int i = start; i < output.length; i+=4){
                output[i] = 0;
                output[i+1] = 0;
            }
        }

        return output;
    }
}
