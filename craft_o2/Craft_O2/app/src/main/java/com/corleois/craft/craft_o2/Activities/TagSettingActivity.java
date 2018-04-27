package com.corleois.craft.craft_o2.Activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;

import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.FileNameEscape;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;
import com.corleois.craft.craft_o2.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Mato on 2017/06/27.
 */

public class TagSettingActivity extends Activity {

    static private final String PREF_KEY_TITLE = "Pref_Key_Tag_Title";
    static private final String PREF_KEY_ARTIST = "Pref_Key_Tag_Artist";
    static private final String PREF_KEY_ALBUM = "Pref_Key_Tag_Album";
    static private final String PREF_KEY_GENRE ="Pref_Key_Tag_Genre";
    static private final String PREF_KEY_ALBUMARTIST = "Pref_Key_Tag_AlbumArtist";
    static private final String PREF_KEY_YEAR = "Pref_Key_Tag_Year";
    static private final SimpleDateFormat smf = new SimpleDateFormat("yyyy");
    static private boolean flg = false;     //変更検知くんEX
    static private SQLiteFractalCondition fractalCondition;
    static private String Title = "";
    static private String Artist = "";
    static private String Album = "";
    static private String Genre  = "";
    static private String AlbumArtist;
    static private String Year = "0000";

    static private Intent intent = new Intent();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Controller ctr = new Controller();
//        String editing = "";
//        if(ctr.GetNowFilePath() == null ){
//            finish();
//        }else{
//            editing = ctr.GetNowFilePath();
//            Log.d("検索項目",editing);
//        }


        flg = false;
        intent = getIntent();
        String Filepath = intent.getStringExtra("filePath");

        //検索項目の設定　ファイルパスの完全一致
        SQLiteConditionElement conditionElement =
                new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.FilePath,Filepath,"");
        //え？なんで彼もstaticなのかって？それは後で分かるサ（UpDateするタイミングの問題です）
        fractalCondition = new SQLiteFractalCondition(conditionElement);
        //検索結果をAudioFileInfoに格納、ファイルパスだから択一のはず
        ArrayList<AudioFileInformation> List = MusicDBFront.Select(fractalCondition,this, true);

        if(List.size() == 0){
            Log.d("検索結果","0件");
            finish();
            return;
        }

        AudioFileInformation afi = List.get(0);

        //DBから持ってきた各項目をそれぞれ取り出す、ここの項目はstaticでprivateなやつ
        Title = (afi.getTitle() == null) ? "" : StringArrayListConverter.EncodeToString(afi.getTitle(), "\n");
        Artist = (afi.getArtist() == null) ? "" : StringArrayListConverter.EncodeToString(afi.getArtist(), "\n");
        Album = (afi.getAlbum() == null) ? "" : StringArrayListConverter.EncodeToString(afi.getAlbum(), "\n");
        Genre = (afi.getGenre() == null) ? "" : StringArrayListConverter.EncodeToString(afi.getGenre(), "\n");
        AlbumArtist = (afi.getGenre() == null) ? "" : StringArrayListConverter.EncodeToString(afi.getAlbumArtist(), "\n");
        try{
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy", Locale.getDefault());
            String dateStr = simpleDateFormat.format(afi.getYear());
            Year = (afi.getYear() == null || afi.getYear().equals("")) ? "0000" : dateStr;
        }catch (Exception e){
            Year = "0000";
        }
        // PrefFragmentの呼び出し
        //このクラス呼び出されたら設定画面開くよー
        PrefFragment prefFragment = new PrefFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();
    }

    //戻るボタン検知くん
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_BACK) {
            //変更があるとき～
            if (flg) {
                //ダイアログをつくり～
                new AlertDialog.Builder(this)
                        .setTitle("更新確認")
                        .setMessage("タグ情報が変更されています、更新しますか？")
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            //ダイアログOKクリック時
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put(MusicDBColumns.Title,Title.replaceAll("\\n","\\;"));
                                contentValues.put(MusicDBColumns.Artist,Artist.replaceAll("\\n","\\;"));
                                contentValues.put(MusicDBColumns.Album,Album.replaceAll("\\n","\\;"));
                                contentValues.put(MusicDBColumns.Genre,Genre.replaceAll("\\n","\\;"));
                                contentValues.put(MusicDBColumns.AlbumArtist,AlbumArtist.replaceAll("\\n","\\;"));
                                contentValues.put(MusicDBColumns.Year,Year);
                                MusicDBFront.Update(contentValues,fractalCondition,true,TagSettingActivity.this);
                                DBCache.flash();
                                finish();
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            //ダイアログキャンセルクリック時
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //突然のfinish（突然ではない）
                                finish();
                            }
                        })
                        .create()
                        .show();
            }else {
                //またも突然の別れ
                finish();
            }
        }
        return false;
    }

    // 設定画面のPrefFragmentクラス
    //インナークラスとかいうやつですね、たぶん
    public static class PrefFragment extends PreferenceFragment {
        //String[] strs ={Title,Artist,Album,Genre};
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            //タグ設定画面を読み込んでくるよー
            addPreferencesFromResource(R.xml.tag_set);
            //初期設定ニキー
            setFirstSetting();

        }

        // 設定値が変更されたときのリスナーを登録
        @Override
        public void onResume() {
            //画面表示時に呼び出されるらしいっす
            super.onResume();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(listener);
        }

        // 設定値が変更されたときのリスナー登録を解除
        @Override
        public void onPause() {
            //画面非表示時に(ry
            super.onPause();
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.unregisterOnSharedPreferenceChangeListener(listener);
        }

        // 設定変更時に、Summaryを更新
        private SharedPreferences.OnSharedPreferenceChangeListener listener
                = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(
                    SharedPreferences sharedPreferences, String key) {
                if (key.equals(PREF_KEY_TITLE)) {
                    setSummaryTitle();
                } else if (key.equals(PREF_KEY_ARTIST)) {
                    setSummaryArtist();
                } else if (key.equals(PREF_KEY_ALBUM)) {
                    setSummaryAlbum();
                } else if (key.equals(PREF_KEY_GENRE)) {
                    setSummaryGenre();
                } else if (key.equals(PREF_KEY_ALBUMARTIST)) {
                    setSummaryAlbumArtist();
                } else if (key.equals(PREF_KEY_YEAR)) {
                    setSummaryYear();
                }
                //設定変更後、フラグを立てる、死亡か恋哀かは知らんぞぃ
                flg = true;
            }
        };

        /**
         * Sammaryの設定
         */
        //読み込み時の初期設定
        public void setFirstSetting() {
            EditTextPreference prefFraction;
            //DBより検索したタイトルをprefに設定
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_TITLE);
            prefFraction.setText(Title);
            setSummaryTitle();
            //アーティスト
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_ARTIST);
            prefFraction.setText(Artist);
            setSummaryArtist();
            //アルバム
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_ALBUM);
            prefFraction.setText(Album);
            setSummaryAlbum();
            //ジャンル
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_GENRE);
            prefFraction.setText(Genre);
            setSummaryGenre();
            //アルバムアーティスト
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_ALBUMARTIST);
            prefFraction.setText(AlbumArtist);
            setSummaryAlbumArtist();
            //年
            prefFraction = (EditTextPreference) findPreference(PREF_KEY_YEAR);
            prefFraction.setText(Year);
            setSummaryYear();
        }

        // Title の Summary を設定
        public void setSummaryTitle() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_TITLE);
            //設定値 → Summary
            Title = FileNameEscape.Escape(prefFraction.getText());
            prefFraction.setSummary(Title);
        }

        // Artist の Summary を設定
        private void setSummaryArtist() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_ARTIST);
            //設定値 → Summary
            Artist = FileNameEscape.Escape(prefFraction.getText());
            prefFraction.setSummary(Artist);
        }

        //Album
        private void setSummaryAlbum() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_ALBUM);
            //設定値 → Summary
            Album = FileNameEscape.Escape(prefFraction.getText());
            prefFraction.setSummary(prefFraction.getText());
        }

        //Genre
        private void setSummaryGenre() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_GENRE);
            //設定値 → Summary
            prefFraction.setSummary(prefFraction.getText());
            Genre = prefFraction.getText();
        }

        //AlbumArtist
        private void setSummaryAlbumArtist() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_ALBUMARTIST);
            //設定値 → Summary
            prefFraction.setSummary(prefFraction.getText());
            AlbumArtist = prefFraction.getText();
        }

        //年
        private void setSummaryYear() {
            EditTextPreference prefFraction = (EditTextPreference) findPreference(PREF_KEY_YEAR);
            //設定値 → Summary
            prefFraction.setSummary(prefFraction.getText());
            Year = prefFraction.getText();
        }
    }
}
