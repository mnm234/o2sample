package com.corleois.craft.craft_o2.CraftLibrary.FileManipulator;

/**
 * Created by Mato on 2017/07/04.
 */

public class FileNameEscape {
    public static String Escape(String fileName) {
        String result = fileName;
        // \ / : * ? " < > |
        result = result.replaceAll("\\\\","＼");
        result = result.replaceAll("\\/","／");
        result = result.replaceAll("\\:","：");
        result = result.replaceAll("\\;","；");
        result = result.replaceAll("\\*","＊");
        result = result.replaceAll("\\?","？");
        result = result.replaceAll("\\\"","”");
        result = result.replaceAll("\\<","＜");
        result = result.replaceAll("\\>","＞");
        result = result.replaceAll("\\|","_");
        return result;
    }


    /**
     *置き換える文字を向こう側で指定してもらうやつ（がばいばあちゃん
     * @param Str もとの文字列
     * @param TargetChars 置きかえ対象の文字の配列
     * @param ReplaceChars 置き換え後の文字の配列
     * @return 置き換えたあとの文字列
     */
    public static String Escape2 (String Str, String[] TargetChars, String[] ReplaceChars){
        String resurt = Str;
        //配列の要素がちっさい方を格納、まぁ長さ違ったらスルーでもいいんだけどね
        int n = (TargetChars.length <= ReplaceChars.length ) ? TargetChars.length : ReplaceChars.length;

        //要素分だけ置き換えを実行
        for (int i = 0; i < n; i++) {
            resurt = resurt.replaceAll(TargetChars[i],ReplaceChars[i]);
        }
        return Str;
    }
}
