package com.corleois.craft.craft_o2.CraftLibrary;

/**
 * Created by StarLink on 2017/06/11.
 */

import java.lang.reflect.Array;
import java.util.ArrayList;

/**
 * DBでのデータ送受信に使うString型のArrayListにしたり、メタデータを取り扱うときに使う区切り文字で結合した文字列をゲットしたりするときに使う
 */
public class StringArrayListConverter {
    /**
     * 文字列のリストを指定された区切り文字を付加したString形式に変換します
     * @param stringArrayList 入力する文字列リスト
     * @param separator　項目の末尾に付加する区切り情報の文字列。最後には付加されません
     * @return 約束された例のブツ（結合済のString型のデータ）
     */
    public static String EncodeToString(ArrayList<String> stringArrayList, String separator){

        //String→StringBuilderで少しでも高速化するんやで！
        //……って思ったけど、実際はStringでもVMが実行する際、最適化でStringBuilderに型変換してスピードアップさせてたわ……

        //追記：char型のほうがもっと速かった！

        //char型にお必要な配列の長さを求めるよ
        int totalLength = 0;
        int sepLength = separator.length();
        int size = stringArrayList.size();
        for (int i = 0; i < size; i++) {
            totalLength += stringArrayList.get(i).length() + sepLength;
        }
        if(size > 0) {
            //1回分だけセパレータが多いので長さを差し引いておく
            totalLength -= sepLength;
        }

        char[] chars = new char[totalLength];

        char[] sepChars = separator.toCharArray();  //セパレータ文字列ね

        //foreachみたいなものは、順番が保証されないので、順番が大事なこのリストでは通常のfor文を使う
        int pos = 0;
        for (int i = 0; i < size; i++){
            //文字列を追加結合する
            char[] tempChars = stringArrayList.get(i).toCharArray();
            int recordLength = tempChars.length;

            //配列の内容の文字列を代入
            for (int j = 0; j < recordLength; j++) {
                chars[pos] = tempChars[j];
                pos++;
            }

            //リストの終端でないならば、連結文字列を付加する
            if(i != size -1){
                for (int j = 0; j < sepLength; j++) {
                    chars[pos] = sepChars[j];
                    pos++;
                }
            }
        }

        return new String(chars);
    }
    /**
     * 文字列のリストを指定された区切り文字を付加したString形式に変換します
     * @param stringArrayList 入力する文字列リスト
     * @param separator　項目の末尾に付加する区切り情報の文字列。最後には付加されません
     * @return 約束された例のブツ（結合済のString型のデータ）
     */
    public static  String Encode(ArrayList<String> stringArrayList, String separator){
        return EncodeToString(stringArrayList, separator);
    }

    /**
     * 文字列を指定した区切りで分割してArrayList形式にして返します
     * @param dynamite 入力する文字列
     * @param separator セパレータ
     * @return  約束された例のブツ（ArrayList String形式）
     */
    public static ArrayList<String> DecodeToStringArrayList(String dynamite, String separator){
        ArrayList<String> result = new ArrayList<>();

        //花火を打ち上げようじゃないか
        String[] explosion = dynamite.split(separator,0);

        //foreachはすべての要素についての処理は保証しても、順番までは保証してくれない。
        for(int i = 0; i < explosion.length; i++){
            result.add(explosion[i]);
        }
        return result;
    }

    /**
     * 文字列を指定した区切りで分割してArrayList形式にして返します
     * @param dynamite 入力する文字列
     * @param separator セパレータ
     * @return  約束された例のブツ（ArrayList String形式）
     */
    public static  ArrayList<String> Decode(String dynamite, String separator){
        return DecodeToStringArrayList(dynamite,separator);
    }
}
