package com.corleois.craft.craft_o2.MetaData;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;

import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.CreateChecksum;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.FileNameEscape;
import com.corleois.craft.craft_o2.CraftLibrary.ImageResizer;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static java.lang.Integer.parseInt;

/**
 * Created by Mato on 2017/06/18.
 */


public class AudioInfoSetting extends AppCompatActivity {

    private int countSuccess = 0;//登録成功でカウントアップ
    private int countFailed = 0;//登録失敗でカウントアップ
    private String ImageDirPath;//アートワークを保存するディレクトリのパス

    public AudioInfoSetting(String imageDirPath){
        ImageDirPath = imageDirPath;
    }
    /**
     * 引数で渡された  ArrayList内の音楽データのパスからハッシュ値及びメタデータを取得し、AudioFileInformationのArraylistで返却するクラスです。
     *
     * @param MusicPathList 登録したい複数の音楽データのパスを格納したArrayList
     * @return 利用可能な音声ファイル全件の情報
     */
    public ArrayList<AudioFileInformation> MusicListSetting(ArrayList<String> MusicPathList) {
        //一括追加のテスト
        //メタデータのやつを用意しーの
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        //audioFileInfoを格納するArrayList
        ArrayList<AudioFileInformation> AFIList = new ArrayList<>();
        //Fileをいれるのじゃぞ
        File file;

        //音楽データ1件ごとのファイルパスを一時的に格納する
        //String filePath;
        //同じくハッシュ値
        String hash_sha256 = "";
        //複数件のアーティスト対策のartistlist
        ArrayList<String> artistlist;
        //ディレクトリ以下の音楽データを走査

        //ファイルを書き込むためのアレ
        FileOutputStream fos = null;

        Log.d("addSetList", MusicPathList.size() + "件のデータの準備を開始");

        //画面の幅と高さを獲得。縦長のとき固定の値だって
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        //末尾まで回すよ！
//        for (int i = 0; i < MusicPathList.size(); i++) {
        int size = MusicPathList.size();
        for (int i = 0; i < size; i++) {

            //一件ごとに使うデータはループ内で初期化する
            artistlist = new ArrayList<>();

            try {
                //filePath = MusicPathList.get(i);
                file = new File(MusicPathList.get(i));
                //パスを取得するやつにセット
                mmr.setDataSource(MusicPathList.get(i));

                //ハッシュを作るやつにファイルを投げつける
//                hash_sha256 = CreateChecksum.printDigest(CreateChecksum.getFileDigest(file));
                //戻ってきたハッシュとファイルパスをもとにAudioFileInfoを生成

                AudioFileInformation audioFileInformation = new AudioFileInformation(MusicPathList.get(i), "");

                Log.d("addHashset", (i + 1) + "件目のハッシュ作成 : " + MusicPathList.get(i));
                Log.d("addss",String.valueOf(countSuccess));
                try {
                    //タイトルをセット
                    String title="";
                    if(mmr.extractMetadata(mmr.METADATA_KEY_TITLE) != null) {
                        audioFileInformation.addTitle(mmr.extractMetadata(mmr.METADATA_KEY_TITLE));
                        title = mmr.extractMetadata(mmr.METADATA_KEY_TITLE);
                    }else{
                        audioFileInformation.addTitle(file.getName());
                    }
                    Log.d("Title",title);
                    mmr.extractMetadata(mmr.METADATA_KEY_AUTHOR);
                    //アーティスト名セット
                    //MMRで取得したアーティスト情報をコンバータでArrayListにする、複数件のチェックのためだよ
                    if(mmr.extractMetadata(mmr.METADATA_KEY_ARTIST) != null) {
                        Log.d("Artist",mmr.extractMetadata(mmr.METADATA_KEY_ARTIST));

                        artistlist = StringArrayListConverter.DecodeToStringArrayList(mmr.extractMetadata(mmr.METADATA_KEY_ARTIST), "/");
                    }else{
                        Log.d("Artist","アーティスト名が取得できないよ！！");

                        artistlist.add("");
                    }
                    audioFileInformation.setArtist(artistlist);

                    //アルバムアーティスト
                    if(mmr.extractMetadata(mmr.METADATA_KEY_ALBUMARTIST) != null){
                        audioFileInformation.addAlbumArtist(mmr.extractMetadata(mmr.METADATA_KEY_ALBUMARTIST));
                    }else {
                        audioFileInformation.addAlbumArtist("");
                    }

                    //アルバム名
                    String Album = "";
                    if(mmr.extractMetadata(mmr.METADATA_KEY_ALBUM) != null) {
                        Album = mmr.extractMetadata(mmr.METADATA_KEY_ALBUM);
                        //audioFileInformation.addAlbum(mmr.extractMetadata(mmr.METADATA_KEY_ALBUM));
                        audioFileInformation.addAlbum(Album);
                    }else {
                        audioFileInformation.addAlbum("");
                    }

                    //ジャンル
                    if(mmr.extractMetadata(mmr.METADATA_KEY_GENRE) != null) {
                        audioFileInformation.addGenre(mmr.extractMetadata(mmr.METADATA_KEY_GENRE));
                    }else{
                        audioFileInformation.addGenre("");
                    }


                    //トラックナンバーセット StringでくるからparseIntするよ(条件をparseIntの可否にした方がいいかも)
//                    int trackno = (mmr.extractMetadata(mmr.METADATA_KEY_NUM_TRACKS) == "null") ? 0 : parseInt(mmr.extractMetadata(mmr.METADATA_KEY_NUM_TRACKS));
//                    audioFileInformation.setTrackNumber(trackno);

                    /**
                     * トラックナンバー周りの実装が正しくないみたいなので、修正してちょっとだけ大改造機能追加するよー by きょーじゅ
                     */
                    //トラックナンバーセット StringでくるからparseIntするよ(条件をparseIntの可否にした方がいいかも)
                    //楽曲は[現在のトラック番号/全体の曲数]で来るので、例のクラスを使ってちょっと戯れ
                    {
                        //設定されたかを管理するフラグ
                        boolean configred = false;

                        String string = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER);
                        //トラック番号が設定されていない場合や、全体のトラック数片方だけ、あるいは現在のトラック番号片方だけの可能性もあるので
                        //ちゃんと設定されていないと登録されないようにして回避しておく
                        if(string != null) {
                            ArrayList<String> tracks = StringArrayListConverter.Decode(string, "/");

                            //正しくメタデータがとれていれば、ここにTrueで入る
                            if(tracks.size() == 2) {
                                int trackno = parseInt(tracks.get(0));
                                int allTrackNo = parseInt(tracks.get(1));
                                audioFileInformation.setTrackNumber(trackno);
                                audioFileInformation.setTotalTracks(allTrackNo);

                                Log.d("AIS@TrackNo", mmr.extractMetadata(mmr.METADATA_KEY_CD_TRACK_NUMBER));
                                configred = true;
                            }
                        }

                        //弾かれた場合などは0で置いておく
                        if(! configred){
                            audioFileInformation.setTrackNumber(0);
                            audioFileInformation.setTotalTracks(0);
                        }
                    }
                    /**
                     * 劇的アウトローここまで
                     */

                    //今の時間を入れるよ
                    try {
                        Date date = new Date();
                        audioFileInformation.setAddDateTime(date);
                    }catch (Exception e){Log.e("AIS","今の時間セットでエラー");}
                    try {
                        SimpleDateFormat sDF = new SimpleDateFormat("yyyy");
                        if(mmr.extractMetadata(mmr.METADATA_KEY_YEAR) != null){
                            Log.d("setYear",mmr.extractMetadata(mmr.METADATA_KEY_YEAR));
                            Date year = sDF.parse(mmr.extractMetadata(mmr.METADATA_KEY_YEAR));
//                            String year = mmr.extractMetadata(mmr.METADATA_KEY_YEAR);
                            audioFileInformation.setYear(year);
                        }else {
                            Log.d("setYear",mmr.extractMetadata(mmr.METADATA_KEY_YEAR));
                            audioFileInformation.setYear((String) null);
                        }

                    } catch (Exception e) {Log.e("AIS","年のセットでエラー");}

                    String dirName = "";
                    if(!Album.equals("")){
                        dirName = Album;
                    }else{
                        if(!title.equals("")){
                            dirName = title;
                        }
                    }

                    File imgfile = new File(ImageDirPath, FileNameEscape.Escape(dirName)+".png");
                    if (imgfile.exists()) {
                        audioFileInformation.addArtWorkPath(imgfile.getPath());
                        Log.d("aaa","既にあったよ");
                    } else {
                        if(mmr.getEmbeddedPicture() != null) {
                            byte[] data = mmr.getEmbeddedPicture();
                            if (null != data) {
                                Log.d("byte-data", String.valueOf(data));
                                try {
                                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);

                                    bmp = ImageResizer.resizeTooBigBitmap(bmp);

                                    Log.d("Pict", "Success");
                                    fos = new FileOutputStream(imgfile);
                                    // pngで保存
                                    bmp.compress(Bitmap.CompressFormat.PNG, 5, fos);
                                    // 保存処理終了

                                    fos.close();
                                    bmp.recycle();

                                    //保存した画像のパスをafiに設定
                                    audioFileInformation.addArtWorkPath(imgfile.getPath());
                                    Log.d("saveImageFilePath", "セーブしたよー！" + imgfile.getPath());
                                } catch (Exception e) {
                                    Log.e("Error", "" + e.toString());
                                    Log.d("Pict", "Failed");
                                    try {
                                        //ストリームはオブジェクトがあったら必ず閉じる
                                        if(fos != null){
                                            fos.close();
                                        }
                                    }catch (IOException ex){
                                        Log.e("Error",ex.getMessage());
                                    }
                                }
                            }
                        }else {
                            audioFileInformation.addArtWorkPath("");
                        }
                    }
                    //挿入するAudioFileInfoをリストに登録するよ
                    AFIList.add(audioFileInformation);
                    //ここまでうまく行ったら成功数をカウントアップ
                    countSuccess += 1;
                    Log.d("addSuccess", countSuccess + "/" + MusicPathList.size());
                    TextNotificationRelay.sendText("メタデータ取得中\n" + String.valueOf(countSuccess) + "/" + MusicPathList.size() +"件");
                } catch (Exception e) {
                    Log.e("AIS","メタデータ取得でミスってるよー" + audioFileInformation.getFilePath());
                    //メタデータ取得時にException発生で失敗カウントアップ
                    countFailed += 1;
                    Log.d("addFailed", countFailed + "/" + MusicPathList.size());
                }
            } catch (Exception e) {
                //e.printStackTrace();
                Log.e("AIS","ハッシュ値生成でミスってるよー");
                //ハッシュ値生成でミスっても失敗数をカウントアップ
                countFailed += 1;
                Log.d("addFailed", countFailed + "/" + MusicPathList.size());
            }
        }
        //よみこみしゅーりょー
        //MusicMetaDataRetrieverをかいほー
        mmr.release();
        //リスト返却しえーっす
        return AFIList;
    }

    //成功数を返すよー
    public int getCountSuccess() {
        return countSuccess;
    }

    //失敗数を返すよー
    public int getCountFailed() {
        return countFailed;
    }

}