package com.corleois.craft.craft_o2.MusicDB;

/**
 * 検索条件の格納形式を定義する内部オブジェクト
 */
public class SQLiteConditionElement{

    public static final String EQUAL                 = " = ? ";
    public static final String NOT_EQUAL             = " != ? ";
    public static final String MORE_THAN             = " < ? ";
    public static final String LESS_THAN             = " > ? ";
    public static final String MORE_THAN_OR_EQUAL    = " <= ? ";
    public static final String LESS_THAN_OR_EQUAL    = " >= ? ";
    public static final String BETWEEN               = " BETWEEN ? AND ? ";
    public static final String LIKE                  = " LIKE ? ";
    public static final String IS_NULL               = " IS NULL";
    public static final String IS_NOT_NULL           = " IS NOT NULL";

    private String Operator;
    private String ColumnName;
    private String Value1;
    private String Value2;

    /**
     * コンストラクター。ダミーデータ用です。
     */
    public SQLiteConditionElement(){
        this.Operator = "";
        this.ColumnName = "";
    };

    /**
     * コンストラクター
     * @param operator      演算子
     * @param columnName    対象カラム名
     * @param value1        値1（NULL演算子では、何をしても何も起こらない）
     * @param value2        値2（BETWEEN演算子向け。それ以外では指定しても何も起こらない）
     */
    public SQLiteConditionElement(String operator, String columnName, String value1, String value2){
        //列名とオペレータは危険度が高いので、ここでSQLインジェクション対策をしたい……
        //列名チェック
        if(columnName.equals("") ||columnName == null){
            throw new IllegalArgumentException("引数のカラム名がガバガバです：" + ((columnName == null) ? "(null)" : "(空文字列)"));
        }

        //条件を通過した場合のみTrue
        boolean chk = false;

        //文字列は列名のいずれかに一致しているかチェック
        for (String col : MusicDBColumns.ColumnList()) {

            //列名と一致ならOK
            if(columnName.equals(col)){
                //trueにして、for文を抜ける
                chk = true;
                break;
            }
        }
        //chk == falseの場合true
        if(!chk){
            throw new IllegalArgumentException("引数のカラム名に変な名前がついています：" + columnName);
        }

        //次はオペレータチェック
        chk = false;
        switch (operator){
            case EQUAL:
            case NOT_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case MORE_THAN:
            case MORE_THAN_OR_EQUAL:
            case BETWEEN:
            case LIKE:
            case IS_NULL:
            case IS_NOT_NULL:
                chk = true;
                break;
            default:
                chk = false;
        }
        //異常値ならここでバイバイ
        if(!chk){
            throw new IllegalArgumentException("引数の演算子に変なのが入ってます：" + ((operator == null) ? "(null)" : operator));
        }

        this.Operator = operator;
        this.ColumnName = columnName;
        this.Value1 = value1;
        this.Value2 = value2;

    }


    /**
     * 検索条件の文字列を生成します
     * @return
     */
    public String getCondition(){
        return  "("+ this.ColumnName + this.Operator + ")";
    }

    public String[] getParameters(){
        //変数の数を格納する変数
        int valuecount;

        switch (this.Operator){
            //BETWEEN演算子は3つ
            case BETWEEN:
                valuecount = 2;
                break;
            //Null系は1つ
            case IS_NULL:
            case IS_NOT_NULL:
                valuecount = 0;
                break;
            //それ以外は2つ
            case EQUAL:
            case NOT_EQUAL:
            case MORE_THAN:
            case MORE_THAN_OR_EQUAL:
            case LESS_THAN:
            case LESS_THAN_OR_EQUAL:
            case LIKE:
                valuecount = 1;
            break;
            //未定義のテキストが入ってきたときは、空配列
            default:
                valuecount = 0;
        }

        //返す値を作る
        String[] result;
        result = new String[valuecount];

        //順番通りの代入処理
        //foreachは、処理の順番までは保証しないので×
        for(int i = 0;i < valuecount; i++){
            switch (i){ //引数がそれぞれ1，2個のとき
                case 0:
                    result[i] = Value1;
                    break;
                case 1:
                    result[i] = Value2;
            }
        }

        return result;
    }

}
