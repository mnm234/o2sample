package com.corleois.craft.craft_o2.Activities;

import android.Manifest;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.corleois.craft.craft_o2.MetaData.AudioInfoSetting;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.Utilities.UpdateDB;
import com.corleois.craft.craft_o2.Activities.fragments.Permission.NoticeDialogFragment;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.SearchSubFiles;

/**
 * Created by corleois on 2017/07/05.
 */

/**
 * パーミッション取得用のアクティビティだよ！！
 * 初回起動時はこの画面からパーミッション取得してね！
 * すでに取得していた場合はMusicSelectionActivityに飛ぶよ！！
 */
public class PermissionActivity extends AppCompatActivity implements NoticeDialogFragment.NoticeDialogListener,ActivityCompat.OnRequestPermissionsResultCallback,Runnable{
    private final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 1; //ファイルの読み取りのリクエストコードだよ！それぞれ一意にしてね今回はお試しの1
    private final int REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 2; //同じく書き込み用、とりま2
    private Thread thread = new Thread(this);

    int countSuccess = 0;//登録成功でカウントアップ
    int countFailed = 0;//登録失敗でカウントアップ
    AudioInfoSetting AFI;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AFI = new AudioInfoSetting(getApplicationContext().getFilesDir().getPath());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッション取得してるかチェック
            if (PermissionChecker.checkSelfPermission(PermissionActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                setContentView(R.layout.permission_layout);

//                AFI = new AudioInfoSetting(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getPath());
                //パーミッションがない場合はリクエストへGO!!
                //パーミッションの説明ダイアログを作成
                DialogFragment noticeDialogFragment = new NoticeDialogFragment();
                //パーミッションの説明ダイアログを表示
                noticeDialogFragment.show(getFragmentManager(), "noticeDialog");

            } else {
                //持ってたらとりあえずパンを焼くよ
                Toast.makeText(PermissionActivity.this, "READパーミッション取得済み", Toast.LENGTH_SHORT).show();

                //アプリ画面に飛ぶ
                AppStart();
            }
        }else if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1){
            //OSバージョン5.1以下の時の処理。インストール時に許可は与えられてるので端末内データをデータベースに登録

//            ArrayList<AudioFileInformation> arrayList = MusicDBFront.Select(null,this, true);
//            if(arrayList.size() == 0){
//                thread.start();
//                try {
//                    thread.join();
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//            }
            //アプリ画面に飛ぶ
            AppStart();
        }
    }

    private void AppStart(){
        //ちょっとDBへのデータ更新がお邪魔しますよっと by 油谷
//        UpdateDB updateDB = new UpdateDB(getApplicationContext());
//        updateDB.SearchAndUpdateDB(true);
//        updateDB.TrackTheNotExistRecord();

        Intent intent = new Intent(PermissionActivity.this,MusicSelectionActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AFI = null;
        thread = null;
        Log.d("kataduke","おかたづけー");
    }

    //ダイアログボックスの「OK」ボタンが押された時の処理
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        //パーミッション取得処理
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE
                }           ,REQUEST_CODE_READ_EXTERNAL_STORAGE);
    }

    //一応キャンセル的なボタンが押された時のリスナー
    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        //キャンセルボタンが押された処理
    }

    //パーミッション取得ダイアログボックスの表示後の処理
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            //取得できたパーミッションが読み取りの時
            case REQUEST_CODE_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // パーミッションが必要な処理を記述する
//                    AFI = new AudioInfoSetting(getApplicationContext().getFilesDir().getPath());
                    //thread.start();
                    //try {
                        //thread.join();
                        //アプリ画面に飛ぶ
                        AppStart();
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }

                } else {
                    // パーミッションが得られなかった時
                    // 処理を中断する・エラーメッセージを出す・アプリケーションを終了する等の処理を記述
                    //アプリ情報に飛ばすよ！！自分で権限許可してね！
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    //フラグメントの場合はgetContext().getPackageName()らしい
                    Uri uri = Uri.fromParts("package",getPackageName(),null);
                    intent.setData(uri);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void run() {

        //一括でInsert!!
        //読み込むディレクトリのパス(ミュージックディレクトリ)
        String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getPath();
        //メタデータとハッシュ値をセットするやーつ
        //コンストラクタでアートワークを保存するパスを指定

        String[] extentions = {"mp3","wav","mp4","ogg","midi","flac"};

        Log.d("addstart","挿入開始" + dirPath);   //いくぞ、データ挿入。音楽の貯蔵は充分か？

        MusicDBFront.Insert(AFI.MusicListSetting(SearchSubFiles.Search(dirPath, extentions)), true, getApplication());
        countSuccess = AFI.getCountSuccess();   //成功数のカウンタを反映
        countFailed = AFI.getCountFailed();     //失敗数のカウンタを反映

        Log.d("addFinish",countSuccess + "件挿入終了");  //クククッ…めっちゃあったッ！！
    }
}
