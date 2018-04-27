package com.corleois.craft.craft_o2.MusicDB;

/**
 * 条件を生成してくれるクラス
 */
public class SQLiteFractalCondition {
    public static final int AND = 0;
    public static final int OR = 1;
    public static final int NOT = 2;

    //葉の場合に格納される
    private SQLiteConditionElement Element;

    //枝の場合に格納される
    private SQLiteFractalCondition FractalA;
    private SQLiteFractalCondition FractalB;
    private int And_Or_Not;

    /**
     * 末端用コンストラクタ。末端のオブジェクトは、全てSQLiteConditionElementオブジェクトを持つ
     * @param element  検索条件を格納したオブジェクト
     */
    public  SQLiteFractalCondition(SQLiteConditionElement element){
        init();
        //異常値チェック
        if(element == null){
            throw new IllegalArgumentException();
        }
        this.Element = element;
    }

    /**
     * 中間用コンストラクタ。配下に置くオブジェクトを2つ必要とします。
     * @param ConnectType   ANDまたはORまたはNOT
     * @param fractalA このオブジェクトの配下に置くオブジェクト1
     * @param fractalB このオブジェクトの配下に置くオブジェクト2
     */
    public SQLiteFractalCondition(int ConnectType, SQLiteFractalCondition fractalA, SQLiteFractalCondition fractalB){
        init();
        //異常値チェック
        if(ConnectType < 0 || ConnectType > 2){
            throw new IllegalArgumentException();
        }
        if(fractalA == null || fractalB == null){
            throw new IllegalArgumentException();
        }

        this.FractalA = fractalA;
        this.FractalB = fractalB;
        this.And_Or_Not = ConnectType;
    }

    /**
     * コンストラクタ内の共通処理
     */
    private void init(){
        Element = null;
        FractalA = null;
        FractalB = null;
        And_Or_Not = 0;
    }

    /**
     * SQL文の条件を結合したテキストを生成する
     * @return .query()に代入可能な検索条件文字列
     */
    public String getConditionString(){
        String result = "";

        //もし、このオブジェクトが葉なら
        if(Element != null){
            //条件文字列をとってくる
            result = Element.getCondition();
            return result;
        }

        //そうでないときは、ここが枝

        //前の方の条件単体またはグループを文字列化して連結する
        result += "(" + FractalA.getConditionString();

        //2つのグループを論理演算子で連結する
        switch (And_Or_Not){
        case AND:
            result += " AND ";
            break;
        case OR:
            result += " OR ";
            break;
        case NOT:
            result += " NOT ";
            break;
        }

        //もう片方の条件単体またはグループを文字列化して連結する
        result += FractalB.getConditionString() + ")";

        return result;
    }

    public String[] getParameters(){
        String[] result;

        //もし、このオブジェクトが葉なら
        if(Element != null){
            //パラメータをとってくる
            result = Element.getParameters();
            return result;
        }

        //そうでないときは、ここは枝
        //先に登場するのはFractalAなので、そっちから先に代入したリストを作る
        //少しばかりの戯れ
        String[] FractalBoy = FractalA.getParameters();
        String[] FractalGirl = FractalB.getParameters();

        String[] FairyTale = new String[FractalBoy.length + FractalGirl.length];

        //FractalAから代入
        for (int i = 0; i < FractalBoy.length; i++){
            FairyTale[i] = FractalBoy[i];
        }
        //FractalBを続いて代入
        for(int i = 0; i < FractalGirl.length; i++){
            FairyTale[FractalBoy.length + i] = FractalGirl[i];
        }

        //結果はおとぎ話。
        result = FairyTale;

        return result;
    }

    /**
     * この検索条件特有の文字列を出力します。
     * @return
     */
    public String getOriginalString(){
        String result = "";
        String[] strings = getParameters();
        result = getConditionString();

        for (int i = 0; i < strings.length; i++) {
            result += strings[i];
        }

        return result;
    }
}


