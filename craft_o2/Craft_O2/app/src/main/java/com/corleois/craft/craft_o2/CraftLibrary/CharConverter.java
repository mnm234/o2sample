package com.corleois.craft.craft_o2.CraftLibrary;

/**
 * Created by StarLink on 2017/08/20.
 */

/**
 * 文字を変換するクラスです
 */
public class CharConverter {

    /**
     * 半角カタカナを全角カタカナに、全角カタカナはひらがなに
     * 全角英数字は半角に、小文字は大文字に変換します
     * @param input
     * @return
     */
    public static String ConvertToHiragana_Hankaku_Lowercase(String input){

        //プリミティブ型となけなしのCPUキャッシュの本気のデュエット……いくぞ？
        char[] buffer = input.toCharArray();

        int length_a = buffer.length;
        for (int i = 0; i < length_a; i++) {

            //半角カタカナを全角にするよ！
            if(buffer[i] >= 'ｧ' && buffer[i] <= 'ﾝ'){
                int dakuten_handakuten = 0;
                //次の文字が濁点または半濁点なら
                //Unicodeでは文字コードは「はばぱ」の順なので
                switch (buffer[i + 1]){
                    case 'ﾟ':   //2加算
                        dakuten_handakuten++;
                    case 'ﾞ':   //1加算
                        dakuten_handakuten++;
                        break;
                    default:
                }
                //ひらがなへ変換
                buffer[i] = (char) (buffer[i] - ('ｧ' - 'ぁ') + dakuten_handakuten);


                //半角カナの濁点半濁点を掃除
                //その後ろの文字列を詰める
                for (int j = (i + 1); j < length_a - 1; j++) {
                    buffer[j] = buffer[j + 1];
                }
                //最後の配列は0にしておく
                buffer[length_a - 1] = 0x0000;
            }

            //ひらがなよりカタカナの方が文字コード大きいぞ！
            //U+30A1からU+30F3
            if(buffer[i] >= 'ァ' && buffer[i] <= 'ン'){
                buffer[i] = (char) (buffer[i] - ('ァ' - 'ぁ'));

                //英数字って全角半角あったよな! これも手動で直していくぞ！
            }else if(buffer[i] >= '０' && buffer[i] <= '～'){
                buffer[i] = (char) (buffer[i] - ('０' - '0'));
            }

            //小文字に変換
            if(buffer[i] >= 'a' && buffer[i] <= 'z'){
                buffer[i] = (char) (buffer[i] - ('a' - 'A'));
            }
        }

        return new String(buffer);
    }

}
