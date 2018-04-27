package com.corleois.craft.craft_o2;

import android.content.Context;
import android.util.Log;

import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Mato on 2017/08/08.
 */

public class VagueSearch {
    /**
     *
     * @param SearchWord 検索ワード
     * @param ColumnCode どの項目に対して検索をかけるか、タブの要素番号に連携予定
     * @param context 検索を使うのでコンテキストください
     * @return 検索後のArrayListを返すよ
     */
    public ArrayList<AudioFileInformation> VagueSearch(String SearchWord, int ColumnCode, Context context){

        //カラムコード0～2、アルバム、アーティスト、楽曲の場合
        if (ColumnCode >=0 && ColumnCode <= 2) {
            //検索カラムを割り当てる
            String MDB[] = {MusicDBColumns.Album, MusicDBColumns.Artist, MusicDBColumns.Title};
            SQLiteFractalCondition fractalCondition = FormatSearchWord(SearchWord, MDB[ColumnCode]);
            //検索検索ぅ
            ArrayList<AudioFileInformation> List = MusicDBFront.Select(fractalCondition, context, true);
            return List;
        }
        return new ArrayList<>();
    }

    /**
     * 検索ワードから空白区切りでOR条件で連結した曖昧検索条件を生成します
     * @param SearchWord 検索ワード
     * @return 検索条件
     */
    private SQLiteFractalCondition FormatSearchWord(String SearchWord ,String SearchColumns){

        //あとで使う検索条件設定用のやつ
        SQLiteConditionElement conditionElement;
        SQLiteFractalCondition fractalCondition;
        //入力した文字列を空白区切りのトークンに分ける
        StringTokenizer st = new StringTokenizer(SearchWord," ");
        int MaxCount = st.countTokens();
        //1つ目のトークンの処理。トークンがない場合(空白含む)の場合はスルーされる
        if(MaxCount != 0) {
            //1つ目のトークンから検索条件を設定、検索カラムは受け取ったやつなので変なの入ると怖い
            conditionElement =
                    new SQLiteConditionElement(SQLiteConditionElement.LIKE, SearchColumns, "%" + st.nextToken() + "%", "");
            fractalCondition = new SQLiteFractalCondition(conditionElement);

            //2つ目以降のトークンの処理、トークンが１つ以下の場合は通らないようになってます。
            while (st.hasMoreTokens()) {
                //↑と同様に検索条件を設定
                conditionElement =
                        new SQLiteConditionElement(SQLiteConditionElement.LIKE, SearchColumns, "%" + st.nextToken() + "%", "");
                //以前のfractalConditionとORでくっつける、継ぎ足し秘伝のタレ方式
                fractalCondition = new SQLiteFractalCondition(SQLiteFractalCondition.OR,fractalCondition,new SQLiteFractalCondition(conditionElement));
            }
            return fractalCondition;
        }else {
            //トークンに分けれない(空白のみとか)場合全件検索しとく(対策は他にもあるはずだけど…)
            conditionElement = new SQLiteConditionElement(SQLiteConditionElement.LIKE,MusicDBColumns.Album, "%%","");
            return new SQLiteFractalCondition(conditionElement);
        }

    }
}
