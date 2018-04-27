package com.corleois.craft.craft_o2.MusicDB.Utilities;

/**
 * Created by StarLink on 2017/07/19.
 */

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.CreateChecksum;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.FileNameEscape;
import com.corleois.craft.craft_o2.CraftLibrary.ImageResizer;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformationAnalyzer;
import com.corleois.craft.craft_o2.MetaData.AudioInfoSetting;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.SearchSubFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

/**
 * DBへの更新をよろしくやってくれるクラス
 */
public class UpdateDB {

    //登録するデータの拡張子。対応しているものなのでここに書いておくのが1番安定（かな）
    private String[] extensions = {
            "mp3","MP3",
            "wav","WAV",
            "mp4","MP4","m4a","M4A",
            "ogg","OGG",
            "mid","MID","midi","MIDI",
            "flac","FLAC"};
    private ArrayList<String> distination;

    private Context context;

    /**
     * コンストラクタ。指定されたDirectoryを探す用。
     * もしnullが引数で入ると、標準用のコンストラクタが動きます。
     * @param SearchDirectories
     * @param context
     */
    public UpdateDB(ArrayList<String> SearchDirectories, Context context){
        //nullの場合は、デフォルトの初期化を行って終了
        if(SearchDirectories == null){
            Init(context);
            return;
        }
        distination = SearchDirectories;
    }

    /**
     * コンストラクタ。探すDirectoryは特に標準でいい時用
     *
     */
    public UpdateDB(Context context)
    {
        Init(context);
    }

    /**
     * コンストラクタ内の処理。主にデフォルト用
     * @param context コンテキスト
     */
    private void Init(Context context){
        this.context = context;

        distination = new ArrayList<>();
        //本体のMusicディレクトリのパスを追加
        distination.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath());
        //他に標準で入れたいディレクトリのパスがあったら、ここで処理する

    }


    /**
     * 端末内のデータを走査して、データベースへのデータの登録を行うメソッドです。
     * 処理をする際、内部ではマルチスレッドで処理されますが、引数によりあたかもシングルスレッドで動作をしているかのような振る舞いにすることができます。
     * つまり、引数がTrueの場合、同一スレッドで実行しているときのように処理終了まで待ち、引数がFalseの場合、バックグランドで処理が動き続けるということです。
     * 本日のおすすめはFalseです。
     * @param waitForThread
     */
    public void SearchAndUpdateDB(boolean waitForThread){

        //Android標準のDBからインポートする
        MusicContentTaker taker = new MusicContentTaker(context);
        MusicDBFront.Insert(taker.getMusicDataFromInternalFlash(), false, context);
        MusicDBFront.Insert(taker.getMusicDataFromExternalStorage(), false, context);

        //重複して検査することにならないかチェック。
        // 重複を発見すると、よりルートディレクトリに近い方のパス設定が優先される
        boolean[] needRemove = new boolean[distination.size()];
        for (int i = 0; i < needRemove.length; i++) {
            needRemove[i] = false;
        }

        for (int i = 0; i < distination.size(); i++) {
            for (int j = 0; j < distination.size(); j++) {
                //パスが一致しておらず、かつ要素番号が同一でない場合T
                if(! distination.get(i).equals(distination.get(j)) && i != j){

                    //その変数が既にtrueもしくは、jの中にiが含まれている場合は、除去をTrueに
                    needRemove[j] = (distination.get(j).contains(distination.get(i)) || needRemove[j]);

                    //パスが一致し、要素番号が同一でない場合T
                }else if(i != j){
                    //jの方を取り除く
                    needRemove[j] = true;
                }
            }
        }

        //除去が必要と判定された要素を取り除く
        for (int i = 0; i < distination.size(); i++) {
            if(needRemove[i]){
                distination.remove(i);
            }
        }

        //前処理完了、ここから処理開始

        Thread[] threads = new Thread[distination.size()];
        for (int i = 0; i < threads.length; i++) {
            //スレッドの名前を探索するディレクトリの頂点にしておく。
            //これでたぶん識別できるはず
            threads[i] = new Thread(distination.get(i)){
                //スレッドの実行コードを書く
                @Override
                public void run() {
                    super.run();

                    //処理優先度は10段階中の3くらい。5が標準
                    this.setPriority(5);

                    //名前がディレクトリパスになっているのでそれを取得
                    String path = this.getName();

                    //このスレッドの根のディレクトリと、見つけたい拡張子を指定してファイルパスを取ってくる
                    ArrayList<String> pathList = SearchSubFiles.Search(path,extensions);

                    /**
                     * フラッシュメモリへのアクセス負荷を下げるための処理
                     * 同一のファイルパスがDBにあることが判明した時点で、そのファイルパスのメタ情報は取得しない。
                     * メタ情報にハッシュ値を生成する処理があり、ファイルサイズが大きなものは、シーケンシャルアクセスといえど時間がかかるため
                     */

                    //そのパスがInsertしなければいけないものかどうかを判別するフラグ配列
                    ArrayList<Boolean> needInserts = new ArrayList<>();
                    //DBに同じファイルパスがあるか問い合わせる
                    for (int j = 0; j < pathList.size(); j++) {

                        //同名のファイルパスは0か1しかない
                        //データがあればFalse、なければTrue
                        boolean needInsert = (DBCache.SelectByFilePath(pathList.get(j),context) == null);

                        //結果を配列リストに追加する
                        needInserts.add(needInsert);
                    }

                    {
                        //応用for文。データが更新すると、カウンタ変数はカウントアップされずにもう一度ループする
                        boolean removedFlg;
                        for (int j = 0; j < pathList.size(); j = removedFlg ? j : (j + 1)) {
                            removedFlg = false;

                            //もし、挿入処理が必要でないならば、除去
                            if( ! needInserts.get(j)){
                                pathList.remove(j);
                                needInserts.remove(j);
                                removedFlg = true;
                            }
                        }
                    }
                    /**
                     * 負荷軽減のための処理ここまで
                     */

                    //画像のイメージ保存先パスを引数にオブジェクト生成
                    AudioInfoSetting audioInfoSetting = new AudioInfoSetting(context.getFilesDir().getPath());

                    //条件に適合する
                    ArrayList<AudioFileInformation> audioFileInformations = audioInfoSetting.MusicListSetting(pathList);
                    //データベースにデータを挿入する。重複するものがあったりしたら、DBのデータを優先して保護する
                    ArrayList<AudioFileInformation> retInfo = MusicDBFront.Insert(audioFileInformations, false, context);

                    Log.d("UpdateDBThread","処理終了: 挿入失敗件数 "+ retInfo.size()+" / "+ audioFileInformations.size() +" 件");
                }
            };

            //スレッドを作ったらすぐ実行
            threads[i].run();
        }

        //存在しないファイルにチェックを付ける処理も行う
        Thread existChkThread = new Thread("exists"){
            @Override
            public void run() {
                super.run();
                CheckPathIsExists();
            }
        };
        existChkThread.run();

        //もし同一スレッド扱いをする場合
        if(waitForThread){
            //すべてのスレッドが終了するまで待つ
            //前から順番に終了まで待てば、最後のスレッドが終了したときにはすべて終了になるはず
            for (int i = 0; i < threads.length; i++) {
                //もしスレッドが生きていたならTrue
                if(threads[i].isAlive()){
                    try {
                        threads[i].join();
                    }catch (InterruptedException e){
                        Log.d("UpdateDB","処理中断/終了検知");
                    }
                }
            }

            //存在するかチェックもまだなら待つ
            if(existChkThread.isAlive()){
                try{
                    existChkThread.join();
                }catch (InterruptedException e){

                }
            }
        }

    }

    /**
     * データベースへ登録されたファイルパスが、今も存在するかをチェックします。
     * ファイルの存在のみをチェックするので、データが変更されたかどうかについては関知しません。
     * そのかわりそこそこ速いはずです。だがシングルスレッド。
     * @return ストレージ上になかったレコード情報
     */
    public ArrayList<AudioFileInformation> CheckPathIsExists(){
        //データ全件取得
        ArrayList<AudioFileInformation> audioFileInformations = MusicDBFront.Select(null, context, true);

        //切り離し（削除）のマークが完了した存在しないファイルのリスト
        ArrayList<AudioFileInformation> prepareToDetach = new ArrayList<>();

        //件数分だけ取得
        for (int i = 0; i < audioFileInformations.size(); i++) {
            File file = new File(audioFileInformations.get(i).getFilePath());

            //もし、ファイルが存在しなかったらTrue
            if( ! file.exists()){
                //更新内容を作成
                ContentValues values = new ContentValues();
                values.put(MusicDBColumns.NotExistFlag,1);

                //更新する条件を作成
                SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL,MusicDBColumns.FilePath,audioFileInformations.get(i).getFilePath(),null);
                SQLiteFractalCondition condition = new SQLiteFractalCondition(element);

                 MusicDBFront.Update(values, condition, true, context);

                prepareToDetach.add(audioFileInformations.get(i));
            }
        }

        return prepareToDetach;
    }


    /**
     * DB上に情報がありながら、実際のファイルが存在しない楽曲が、別レコードでDBに登録されていないか調査します。
     * 同一のファイルが別レコードで登録されていた場合、追加日が新しい方のレコードを削除し、追加日が古い方のレコードに、現在存在するファイルパスに上書きします。
     * どちらかといえば、追加日が古い方のレコードに、ユーザーが編集したタグ情報が保存されている可能性が高いためです。
     */
    public void TrackTheNotExistRecord(){
        //存在しないファイルフラグが立ってるレコードのみを抽出する検索条件の作成
        SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.NotExistFlag, "1","");
        SQLiteFractalCondition condition = new SQLiteFractalCondition(element);
        //検索
        ArrayList<AudioFileInformation> badRecords = MusicDBFront.Select(condition, context, true);

        //データがない場合は終了
        if(badRecords == null || badRecords.size() == 0){
            return;
        }

        //データがある場合は続行

        //一件一件追跡する
        int size = badRecords.size();
        for (int i = 0; i < size; i++) {
            //一件あたりの追跡コード

            //ハッシュ値が同一で、ファイルパスが異なるものがないかDBから探す
            //検索条件設定
            SQLiteConditionElement element1 = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.Hash, badRecords.get(i).getHash(), "");
            SQLiteConditionElement element2 = new SQLiteConditionElement(SQLiteConditionElement.NOT_EQUAL, MusicDBColumns.FilePath, badRecords.get(i).getFilePath(), "");
            SQLiteFractalCondition condition1 = new SQLiteFractalCondition(element1);
            SQLiteFractalCondition condition2 = new SQLiteFractalCondition(element2);

            SQLiteFractalCondition masterCondition = new SQLiteFractalCondition(SQLiteFractalCondition.AND, condition1, condition2);

            //検索
            ArrayList<AudioFileInformation> informations = MusicDBFront.Select(masterCondition, context, true);

            //もし、同じハッシュで別パスのファイルが見つかったらTrue
            if(informations != null && informations.size() > 0){
                Log.d("fileTracker", "informationsize:" + informations.size());
                //最新登録日順にソート。複数件存在する可能性もあるので
                AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
                analyzer.setAnalyzeData(informations);
                informations = analyzer.Sort(MusicDBColumns.AddDateTime, SortType.DESC);

                //先頭は一番新しい日付のはずなので、このレコードからファイルパスを取得する
                String newFilePath = informations.get(0).getFilePath();

                //ファイルパスは主キーなので、それを利用してレコードを削除する
                SQLiteConditionElement element3 = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.FilePath, newFilePath,"");
                SQLiteFractalCondition condition3 = new SQLiteFractalCondition(element3);
                MusicDBFront.Delete(condition3, context);

                //失われていたレコードのfilepathとフラグを修正する
                SQLiteConditionElement element4 = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.FilePath, badRecords.get(i).getFilePath(), "");
                SQLiteFractalCondition condition4 = new SQLiteFractalCondition(element4);

                ContentValues values = new ContentValues();
                values.put(MusicDBColumns.FilePath, newFilePath);
                values.put(MusicDBColumns.NotExistFlag, 0);
                MusicDBFront.Update(values, condition4, true, context);

                //これで、一件あたりの操作は終了
            }
        }
    }

    /**
     * アルバムアートとチェックサムを作る
     */
    public void attachToAlbumArtAndCheckSum(){
        SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.Hash, "", null);
        SQLiteFractalCondition condition = new SQLiteFractalCondition(element);

        //ハッシュが空文字の場合、アルバムアートとハッシュを設定する
        ArrayList<AudioFileInformation> informations = MusicDBFront.Select(condition, context, true);

        //全件回す
        int size = informations.size();
        for (int i = 0; i < size; i++) {
            TextNotificationRelay.sendText("アルバムアート取得中\n" + i +" / " + size + "件");

            //そもそもメタタグ情報として画像を含めることが規格上できないとわかっているものは、ここの正規表現に追記して処理減らす
            if(informations.get(i).getFilePath().matches("^.*[mid|MID|midi|MIDI|wav|WAV|wave|WAVE]$")){
                //Log.d("アルバムアート","ぱするやーで");
                informations.get(i).setArtWorkPath(StringArrayListConverter.Decode("",";"));
                continue;
            }
            //アルバムアート取得
            File file = new File(informations.get(i).getFilePath());

            try {
                //画像を引っ張ってくる
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(informations.get(i).getFilePath());

                byte[] data = mmr.getEmbeddedPicture();

                //なんか画像があったら処理
                if (data != null) {
                    //アルバムアートの名前を絶対取ってくるマン
                    String dirName = null;

                    //何かアルバム名があったら
                    if (informations.get(i).getAlbum() != null) {
                        //後ろから探しまーす
                        for (int j = informations.get(i).getAlbum().size() - 1; j >= 0; j--) {
                            //文字が入ってたらtrue
                            if (informations.get(i).getAlbum().get(j).length() > 0) {
                                dirName = informations.get(i).getAlbum().get(j);
                            }
                        }
                    }

                    //もし、名前がなくてタイトルがありそうなら
                    if (dirName == null && informations.get(i).getTitle() != null) {
                        //後ろから探しまーす
                        for (int j = informations.get(i).getTitle().size() - 1; j >= 0; j--) {
                            //文字が入ってたらtrue
                            if (informations.get(i).getTitle().get(j).length() > 0) {
                                dirName = informations.get(i).getTitle().get(j);
                            }
                        }
                    }

                    //それでもなかったら
                    if (dirName == null) {
                        //最後の手段ファイル名
                        dirName = file.getName();
                    }

                    //とにかく画像生成だっ！！
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                    //画像をいい感じにリサイズ
                    bmp = ImageResizer.resizeTooBigBitmap(bmp);

                    //ファイルオブジェクトを作ってみてー
                    File imgfile = new File(context.getFilesDir().getPath(), FileNameEscape.Escape(dirName) + ".png");
                    //画像データがなかったら！
                    if (!imgfile.exists()) {
                        //慎ましやかに保存

                        FileOutputStream fos = new FileOutputStream(imgfile);
                        // pngで保存
                        bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        // 保存処理終了
                        fos.close();
                        Log.d("画像アタッチ", "新規保存：" + imgfile.getPath());
                    }else{
                        //画像データがあったら
                        //まあちょっと待て。画像が同じか確認しようじゃないか……
                        FileInputStream fileInputStream = new FileInputStream(imgfile);
                        Bitmap storageBitmap = BitmapFactory.decodeStream(fileInputStream);
                        fileInputStream.close();

                        //画像が違ってたらtrue
                        if(! storageBitmap.sameAs(bmp)){
                            //なんや同じアルバムでも画像ちゃうやんけ！ しゃーない気効かせて保存したるやん！
                            //タイトルは同一のものがある可能性があるから、ファイルパスにするやで！
                            imgfile = new File(context.getFilesDir().getPath(),FileNameEscape.Escape(file.getName()) + ".png");
                            FileOutputStream fos = new FileOutputStream(imgfile);
                            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);

                            fos.close();
                            Log.d("画像アタッチ", "違う名前で保存：" + imgfile.getPath());
                        }else {
                            Log.d("画像アタッチ", "同一の画像：" + imgfile.getPath());
                        }


                    }

                    informations.get(i).setArtWorkPath(StringArrayListConverter.Decode(imgfile.getPath(),";"));
                }else {
                    //dataがnullだったら
                    informations.get(i).setArtWorkPath(StringArrayListConverter.Decode("",";"));
                }
            } catch(Exception e){
                //トラブっても空文字
                e.printStackTrace();
                informations.get(i).setArtWorkPath(StringArrayListConverter.Decode("",";"));
            }
        }

        for (int i = 0; i < size; i++) {
            TextNotificationRelay.sendText("ハッシュ値取得中\n" + i +" / " + size + "件");
            //ハッシュを作るやつにファイルを投げつける
            String hash_sha256;
            try {
                File file = new File(informations.get(i).getFilePath());
                hash_sha256 = CreateChecksum.printDigest(CreateChecksum.getFileDigest(file));
                informations.get(i).setHash(hash_sha256);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        TextNotificationRelay.sendText("データベース更新中");
        //上書き
        MusicDBFront.Insert(informations, true, context);
    }
}
