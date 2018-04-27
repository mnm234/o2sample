package com.corleois.craft.craft_o2.MetaData;

/**
 * Created by StarLink on 2017/07/29.
 */

import android.support.annotation.NonNull;
import android.util.Log;

import com.corleois.craft.craft_o2.CraftLibrary.CodeCompare;
import com.corleois.craft.craft_o2.CraftLibrary.SortArrayList;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.OriginalExceptions.GabagabaArgumentException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AudioFileInformationクラスの配列を集計したりソートしたりするクラスです。
 */
public class AudioFileInformationAnalyzer {

    private ArrayList<AudioFileInformation> analyzeData;


    /**
     * コンストラクタ。
     */
    public AudioFileInformationAnalyzer(){
        analyzeData = null;
    }

    /**
     * 解析するデータをセットします。Nullが引数に割り当てられると、NullPointerExceptionが返ります
     * @param analyzeData
     */
    public void setAnalyzeData(@NonNull ArrayList<AudioFileInformation> analyzeData) {
        if(analyzeData == null){
            throw new NullPointerException(AudioFileInformationAnalyzer.class.toString()+"クラスのsetAnalyzeData()はNullの入力を受け付けません");
        }
        this.analyzeData = analyzeData;
    }

    /**
     * 現在解析用にセットされているデータを返します。
     * @return
     */
    public ArrayList<AudioFileInformation> getAnalyzeData() {
        return analyzeData;
    }


    /**
     * ソートを提供します。
     * 指定されていないカラムについては、何も行われません
     *
     * @param priorityColumn　ソートしたいカラム名
     * @param type　昇順降順
     * @return ソート後のリスト
     */
    public ArrayList<AudioFileInformation> Sort(@NonNull String priorityColumn, @NonNull SortType type) {

        //返却用のリストにコピー
        ArrayList<AudioFileInformation> sorted = new ArrayList<>(analyzeData);

        sorted = quickSort(sorted, priorityColumn,0, sorted.size() - 1);

        //もし、降順だったら逆転させる
        if(type == SortType.DESC){
            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(sorted);
            sorted = analyzer.reverse();
        }

        return sorted;
    }

    /**
     * クイックソートを行う内部メソッドです。
     *
     * @param data
     * @param start
     * @param end
     * @return
     */
    private ArrayList<AudioFileInformation> quickSort(@NonNull ArrayList<AudioFileInformation> data,@NonNull String columnName, int start, int end){

        if(start < end) {
            int i = start;
            int j = end;
            AudioFileInformation pivot = data.get( start + (end - start) / 2);

            //クロスしたらその場でループを止める
            //昇順ソート
            while (true) {
                //iがpivotより大きいならば、インクリメント
                while (isEquals(data.get(i), pivot , columnName) < 0) i++;
                //jがpivot未満なら、デクリメント
                while (isEquals(data.get(j), pivot, columnName) > 0) j--;
                //もし、iとjの大小関係がクロスしていたら
                if (i >= j) {
                    //交換終了でループを脱出
                    //Log.d("quickS",start + "-"+ i + " / "+ j + "-" + end);
                    break;
                }

                //ここに来たとき、交換対象があったということで交換
                AudioFileInformation temp = data.get(i);
                data.set(i, data.get(j));
                data.set(j, temp);

                //各自それぞれ次の項目へ進んで同じことを繰り返す
                i++;
                j--;
            }

            //もし、左右いずれかが指定された範囲すべて回ってきたときは、

            //ここに来たとき、データの一巡は終わっているので、左右分かれてソート
            data = quickSort(data, columnName, start, i - 1);
            data = quickSort(data, columnName, j + 1, end);
        }
        return data;
    }



    /**
     * 優先度に基づいたソートを提供します。
     * Indexの小さいものから優先的にソートされ、次に小さい列名で、直近のソートで同位に並んだデータをソートします。
     * 昇順降順の指定数と、ソートで指定する列名の数は、必ず一致している必要があります。
     *
     * @param priorityColumns　ソートしたいデータ名
     * @param sortTypes　昇順降順の設定
     * @return ソート後の配列
     */
    public ArrayList<AudioFileInformation> Sort(@NonNull ArrayList<String> priorityColumns,@NonNull ArrayList<SortType> sortTypes){
        //まず最初に入力チェック
        if(priorityColumns == null || sortTypes == null){
            throw new GabagabaArgumentException("引数にNullが含まれています");
        }
        //長さが一致していなかったら
        if(priorityColumns.size() != sortTypes.size()){
            throw new GabagabaArgumentException("priorityColumns.size()とsortTypes.size()が一致しません");
        }
        //列名がちゃんと有効なもので収まっているかチェック
        {
            ArrayList<String> list = MusicDBColumns.ColumnList();
            boolean[] validList = new boolean[priorityColumns.size()];

            for (int i = 0; i < priorityColumns.size(); i++) {
                validList[i] = false;
                int size = list.size();
                for (int j = 0; j < size; j++) {
                    //列名と一致するものがあればtrue
                    if (CodeCompare.StringCodeCompare(priorityColumns.get(i), list.get(j)) == 0){
                        validList[i] = true;
                    }
                }
            }

            //1つでも存在しないものがあれば、例外が出る
            for (int i = 0; i < validList.length; i++) {
                if( ! validList[i]){
                    throw new GabagabaArgumentException("引数priorityColumnsに、不明な値が含まれています");
                }
            }
        }


        //では、ソートを始めましょう
        int from;   //同一のデータが存在する塊の始点
        int to;     //同一のデータが存在する塊の終点
        ArrayList<AudioFileInformation> tmp = new ArrayList<>(analyzeData);
        ArrayList<AudioFileInformation> tempCut;    //一時的に切り出した配列を格納する変数

        //指定された列数だけ回る
        for (int i = 0; i < priorityColumns.size(); i++) {
            //始点と終点を決定する
            //fromが配列の最後に到達するまで回り続ける
            for (from = 0; from < tmp.size(); from = to){

                //toをfromと一致しない最初のanalyzeDataにもっていく
                for (to = from; isEquals(analyzeData.get(from), analyzeData.get(to), priorityColumns.get(i)) == 0; to++);

                //連続した同じプロパティの塊を切り出す
                tempCut = new ArrayList<>();

                //もし初回ならtrue
                if(i == 0){
                    for (int j = 0; j < tmp.size(); j++) {
                        tempCut.add(tmp.get(j));
                    }
                }else {
                    for (int j = from; j < to; j++) {
                        tempCut.add(tmp.get(j));
                    }
                }

                //ソートを行う
                AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
                analyzer.setAnalyzeData(tempCut);
                ArrayList<AudioFileInformation> tmpRes = analyzer.Sort(priorityColumns.get(i), sortTypes.get(i));

                //ソート結果を置換
                for (int j = from; j < to; j++) {
                    tmp.set(j, tmpRes.get(j - from));
                }
            }
        }
        return tmp;
    }

    /**
     * 2つのメタデータの指定されたプロパティが同一のものであるか返します
     * data1よりdata2が大きければ負数、逆は正の数、等しければ0が返ります
     * 数の大きさは未定義です
     *
     * @param data1
     * @param data2
     * @param columnName
     * @return
     */
    public int isEquals(AudioFileInformation data1, AudioFileInformation data2, @NonNull String columnName){
        int ret;
        switch (columnName) {
            case MusicDBColumns.FilePath:
                ret = CodeCompare.StringA_minus_B_Special(data1.getFilePath(), data2.getFilePath());
                break;

            case MusicDBColumns.Hash:
                ret = CodeCompare.StringA_minus_B_Special(data1.getHash(), data2.getHash());
                break;

            case MusicDBColumns.NotExistFlag:
                ret = data1.getNotExistFlag() - data2.getNotExistFlag();
                break;

            case MusicDBColumns.Title:
                //もし、タイトルがない場合は、ファイルパスで比較する
            {
                String data01;
                String data02;
                data01 = StringArrayListConverter.EncodeToString(data1.getTitle(), "");
                data02 = StringArrayListConverter.EncodeToString(data2.getTitle(), "");

                if(data01.length() == 0){
                    data01 = data1.getFilePath();
                }
                if(data02.length() == 0){
                    data02 = data2.getFilePath();
                }

                ret = CodeCompare.StringA_minus_B_Special(data01, data02);

            }
                break;

            case MusicDBColumns.Artist:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getArtist(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getArtist(), ""));
                break;

            case MusicDBColumns.AlbumArtist:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getAlbumArtist(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getAlbumArtist(), ""));
                break;

            case MusicDBColumns.Album:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getAlbum(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getAlbum(), ""));
                break;

            case MusicDBColumns.Genre:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getGenre(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getGenre(), ""));
                break;

            case MusicDBColumns.YomiTitle:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getYomiTitle(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getYomiTitle(), ""));
                break;

            case MusicDBColumns.YomiArtist:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getYomiArtist(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getYomiArtist(), ""));
                break;

            case MusicDBColumns.YomiAlbum:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getYomiAlbum(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getYomiAlbum(), ""));
                break;

            case MusicDBColumns.YomiGenre:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getYomiGenre(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getYomiGenre(), ""));
                break;

            case MusicDBColumns.YomiAlbumArtist:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getYomiAlbumArtist(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getYomiAlbumArtist(), ""));
                break;

            case MusicDBColumns.ArtWorkPath:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getArtWorkPath(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getArtWorkPath(), ""));
                break;

            case MusicDBColumns.PlaybackCount:
                ret = data1.getPlaybackCount() - data2.getPlaybackCount();
                break;

            case MusicDBColumns.TrackNumber:
                ret = data1.getTrackNumber() - data2.getTrackNumber();
                break;

            case MusicDBColumns.AddDateTime:
                ret = data1.getAddDateTime().compareTo(data2.getAddDateTime());
                break;

            case MusicDBColumns.LastPlayDateTime:
                ret = data1.getLastPlayDateTime().compareTo(data2.getLastPlayDateTime());
                break;

            case MusicDBColumns.Year:
                ret = data1.getYear().compareTo(data2.getYear());
                break;

            case MusicDBColumns.Season:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getSeason(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getSeason(), ""));
                break;

            case MusicDBColumns.Rating:
                double ans = data1.getRating() - data2.getRating();
                if(ans == 0.0D){
                    ret = 0;
                }else if(ans > 0.0D){
                    ret = 1;
                }else{
                    ret = -1;
                }
                break;

            case MusicDBColumns.Comment:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getComment(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getComment(), ""));
                break;

            case MusicDBColumns.TotalTracks:
                ret = data1.getTotalTracks() - data2.getTotalTracks();
                break;

            case MusicDBColumns.TotalDiscs:
                ret = data1.getTotalDiscs() - data2.getTotalDiscs();
                break;

            case MusicDBColumns.DiscNo:
                ret = data1.getDiscNo() - data2.getDiscNo();
                break;

            case MusicDBColumns.Lyrics:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getLyrics(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getLyrics(), ""));
                break;

            case MusicDBColumns.ParentCreation:
                ret = CodeCompare.StringA_minus_B_Special(
                        StringArrayListConverter.EncodeToString(data1.getParentCreation(), "")
                        ,StringArrayListConverter.EncodeToString(data2.getParentCreation(), ""));
                break;

            //デフォルトに到達するときは、すなわちコードの改修が不完全な可能性あり
            default:
                throw new IllegalArgumentException(this.getClass().toString() + "のisEquals()の引数のカラム名に対応するデータ取得に失敗しました。\nこのメソッドの改修が正しく行われているか確認してください。");
        }

        return ret;
    }


    /**
     * セットされたリストのデータのカラムの内容一覧を重複なしで抽出します。
     * @param columnName
     * @return
     */
    public ArrayList<String> getAllIncludedDataFromColumnWithoutDuplicate(String columnName){
        ArrayList<String> ret = new ArrayList<>();

        int arraySize = analyzeData.size();

        //配列の数だけ回るよー
        for (int i = 0; i < arraySize; i++) {

            ArrayList<String> gotList = getDataByString(columnName, i);

            int propertySize = gotList.size();  //↑で取得してきた該当するプロパティのArrayList<String>のサイズ
            int returnListSize = ret.size();    //このメソッドで返すArrayList<String>のサイズ
            boolean newFace = true;             //新規追加しなければいけない項目かどうかのフラグ

            //取得したデータが新しいものであるかどうかを一件一件確認する
            for (int j = 0; j < propertySize; j++) {

                //すでにあるかチェック
                for (int k = 0; k < returnListSize; k++) {
                    //今調べているプロパティの項目が、既に存在している事が判明した時点でTrueに入ってfalse
                    if(CodeCompare.StringA_minus_B(gotList.get(j), ret.get(k)) == 0){
                        newFace = false;
                    }
                }
                //もし、newface=trueなら、追加する
                if(newFace){
                    ret.add(gotList.get(j));
                }
            }
        }

        ret = SortArrayList.Sort(ret,SortType.ASC);
        return ret;
    }


    /**
     * リストの順番を反転させたものを返します
     * @return
     */
    public ArrayList<AudioFileInformation> reverse(){
        ArrayList<AudioFileInformation> reversed = analyzeData;
        int size = reversed.size();

        for (int i = 0; i < (size / 2); i++) {
            AudioFileInformation temp = reversed.get(i);
            reversed.set(i, reversed.get((size - 1) - i));
            reversed.set((size - 1) - i, temp);
        }
        return reversed;
    }

    /**
     * 引数の値でグループを作成します。同一の楽曲が複数のグループに所属する場合があります
     * @param columnName グループを作成する列名
     * @return グループ
     */
    public ArrayList<AudioGroupInformation> makeGroup(@NonNull String columnName){
        ArrayList<AudioGroupInformation> res = new ArrayList<>();
        //カラムごとに処理する
        //カラムと一致するグループがあればそこに入れる。なければ新規作成
        //一曲の中に情報が複数含まれている場合は、同じ曲が複数の曲に所属することになる

        //一曲ずつループ
        ArrayList<String> songList = getAllIncludedDataFromColumnWithoutDuplicate(columnName);
        int songListSize = songList.size();

        //先に全部グループを作っちゃう
        for (int i = 0; i < songListSize; i++) {
            AudioGroupInformation groupInformation  = new AudioGroupInformation();
            groupInformation.setSongList(new ArrayList<AudioFileInformation>());

            groupInformation.setGroupName(songList.get(i));
            res.add(groupInformation);
        }

        int resSize = songListSize;

        //それぞれの楽曲ごとにループを回して、データを入れていく
        int size = analyzeData.size();
        for (int i = 0; i < size; i++) {
            //一件ごとの処理
            //その楽曲のデータ要素を取得する
            ArrayList<String> recordData = getDataByString(columnName, i);
            int recordSize = recordData.size();

            //recordDataとグループを突合せて、一致したらグループに入れる
            for (int j = 0; j < recordSize; j++) {
                for (int k = 0; k < resSize; k++) {

                    //もし、その曲のメタデータと一致するグループ名があったらTrue
                    if(CodeCompare.StringA_minus_B(recordData.get(j), res.get(k).getGroupName()) == 0){
                        //楽曲を追加する
                        ArrayList<AudioFileInformation> temp = res.get(k).getSongList();
                        temp.add(analyzeData.get(i));
                        res.get(k).setSongList(temp);
                    }
                }
            }
        }

        return res;
    }

    /**
     * 一件のデータを文字列形式で取得します
     * @param columnName
     * @param index
     * @return
     */
    public ArrayList<String> getDataByString(@NonNull String columnName, int index){
        
        ArrayList<String> gotList = new ArrayList<>();

        //あらゆるデータをString型にして、ArrayListに突っ込む
        switch (columnName) {
            case MusicDBColumns.FilePath:
                gotList.add(analyzeData.get(index).getFilePath());
                break;

            case MusicDBColumns.Hash:
                gotList.add(analyzeData.get(index).getHash());
                break;

            case MusicDBColumns.NotExistFlag:
                gotList.add(String.valueOf(analyzeData.get(index).getNotExistFlag()));
                break;

            case MusicDBColumns.Title:
                gotList = analyzeData.get(index).getTitle();
                break;

            case MusicDBColumns.Artist:
                gotList = analyzeData.get(index).getArtist();
                break;

            case MusicDBColumns.AlbumArtist:
                gotList = analyzeData.get(index).getAlbumArtist();
                break;

            case MusicDBColumns.Album:
                gotList = analyzeData.get(index).getAlbum();
                break;

            case MusicDBColumns.Genre:
                gotList = analyzeData.get(index).getGenre();
                break;

            case MusicDBColumns.YomiTitle:
                gotList = analyzeData.get(index).getYomiTitle();
                break;

            case MusicDBColumns.YomiArtist:
                gotList = analyzeData.get(index).getYomiArtist();
                break;

            case MusicDBColumns.YomiAlbum:
                gotList = analyzeData.get(index).getYomiAlbum();
                break;

            case MusicDBColumns.YomiGenre:
                gotList = analyzeData.get(index).getYomiGenre();
                break;

            case MusicDBColumns.YomiAlbumArtist:
                gotList = analyzeData.get(index).getYomiAlbumArtist();
                break;

            case MusicDBColumns.ArtWorkPath:
                gotList = analyzeData.get(index).getArtWorkPath();
                break;

            case MusicDBColumns.PlaybackCount:
                gotList.add(String.valueOf(analyzeData.get(index).getPlaybackCount()));
                break;

            case MusicDBColumns.TrackNumber:
                gotList.add(String.valueOf(analyzeData.get(index).getTrackNumber()));
                break;

            //もし、日付の表示形式に不満が出れば、ここの処理を変更すること
            case MusicDBColumns.AddDateTime:
                gotList.add(String.valueOf(analyzeData.get(index).getAddDateTime()));
                break;

            case MusicDBColumns.LastPlayDateTime:
                gotList.add(String.valueOf(analyzeData.get(index).getLastPlayDateTime()));
                break;

            case MusicDBColumns.Year:
                gotList.add(String.valueOf(analyzeData.get(index).getYear()));
                break;

            case MusicDBColumns.Season:
                gotList = analyzeData.get(index).getSeason();
                break;

            case MusicDBColumns.Rating:
                gotList.add(String.valueOf(analyzeData.get(index).getRating()));
                break;

            case MusicDBColumns.Comment:
                gotList = analyzeData.get(index).getComment();
                break;

            case MusicDBColumns.TotalTracks:
                gotList.add(String.valueOf(analyzeData.get(index).getTotalTracks()));
                break;

            case MusicDBColumns.TotalDiscs:
                gotList.add(String.valueOf(analyzeData.get(index).getTotalDiscs()));
                break;

            case MusicDBColumns.DiscNo:
                gotList.add(String.valueOf(analyzeData.get(index).getDiscNo()));
                break;

            case MusicDBColumns.Lyrics:
                gotList = analyzeData.get(index).getLyrics();
                break;

            case MusicDBColumns.ParentCreation:
                gotList = analyzeData.get(index).getParentCreation();
                break;

            //デフォルトに到達するときは、すなわちコードの改修が不完全な可能性あり
            default:
                throw new IllegalArgumentException(this.getClass().toString() + "のgetDataByString()の引数のカラム名に対応するデータ取得に失敗しました。\nこのメソッドの改修が正しく行われているか確認してください。");
        }
        return gotList;
    }
}

