/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.corleois.craft.craft_o2.CraftLibrary;

/**
 *
 * @author 2150087
 */
public class CodeCompare {
    /*public static void main(String args[]){
        
        String s1 = "ミュージック";
        String s2 = "ミュージック0";
        CodeCompare cc = new CodeCompare();
        
        System.out.println(cc.StringCodeCompare(s1, s2));
    }*/
    
    /**
	 * 文字コードが大きい方の文字列を返す
	 */
    public static int StringCodeCompare(String a, String b){
        char[] bytes1 = a.toCharArray();
        char[] bytes2 = b.toCharArray();

        int bytes1L = bytes1.length;
        int bytes2L = bytes2.length;
        int length = bytes1L < bytes2L ? bytes1L : bytes2L;

        //空文字を含むなら文字の長さで比較ができる
        if(length == 0){
            if(bytes1L < bytes2L){
                return -1;
            }else if(bytes1L > bytes2L){
                return 1;
            }else {
                return 0;
            }
        }


        for(int i = 0; i < length; ){
            if(bytes1[i] == bytes2[i]){
                i++;
                if(i == length){
                    if(bytes1L < bytes2L){
                        return -1;
                    }else if(bytes1L > bytes2L){
                        return 1;
                    }
                }
            }else if(bytes1[i] > bytes2[i]){
                return 1;
            }else{
                return -1;
            }
        }
        
        return 0;
    }

    /**
     * 文字列A,Bを計算式（A - B）を用いて比較します。
     * String.equals()メソッドなんかよりずっと速いです。
     *
     * A > Bの場合、A - B = 正の数
     * A < Bの場合、A - B = 負の数
     * A = Bの場合、A - B = 0
     * @param a　文字列A
     * @param b 文字列B
     * @return 比較結果
     */
    public static int StringA_minus_B(String a, String b){
        //比較する
    return StringCodeCompare(a, b);
    }
    /**
     * 文字列A,Bを計算式（A - B）を用いて比較します。
     * ただし、通常とは異なりアルファベットの大文字小文字の区別と、ひらがな・カタカナの区別を行いません
     * A > Bの場合、A - B = 正の数
     * A < Bの場合、A - B = 負の数
     * A = Bの場合、A - B = 0
     * @param a　文字列A
     * @param b 文字列B
     * @return 比較結果
     */
    public static int StringA_minus_B_Special(String a, String b){

        String tempA, tempB;
        tempA = CharConverter.ConvertToHiragana_Hankaku_Lowercase(a);
        tempB = CharConverter.ConvertToHiragana_Hankaku_Lowercase(b);

        //比較する
        return StringCodeCompare(tempA, tempB);
    }
}
