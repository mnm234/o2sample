package com.corleois.craft.craft_o2.Activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.audiofx.Equalizer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.EqualizerSettings.EQConfig;
import com.corleois.craft.craft_o2.EqualizerSettings.EQsetting;
import com.corleois.craft.craft_o2.R;

import java.util.ArrayList;

/**
 * イコライザ設定が行えるアクティビティ
 */
public class EqualizerSettingsActivity extends AppCompatActivity {

    private EQConfig eqConfig;
    private int maxdB;  //調節できる増幅率の最大値
    private int mindB;  //調節できる増幅率の最小値
    private ArrayList<EQsetting> EQSettings;        //イコライザー設定の配列を保存する
    private ArrayList<EQsetting> backupEQSettings;  //復元用のイコライザー設定配列を保存する

    private int[] seekBarLevelTextViewId;   //シークバーの設定された音量を表示するテキストビューのIDを持つ配列
    private int[] seekBarLevelId;           //シークバーのIDを持つ配列

    private int[] mixedBarLevelTextViewId;  //ミックスする方の数値ID
    private int[] progressBarLevelId;       //プログレスバーの方のID

    private EQsetting edittingSettings;     //編集中の変更前イコライザー設定を保持する。
    private EQsetting edittingSettingsRef;  //リストにある編集中のオブジェクトの参照を格納する。
    private int currentShowListindex;       //現在表示中（編集中）のインデックス番号を格納する

    private static final String DEFAULT = "[標準] ";
    private static final String ENABLED = "[有効] ";
    private static final String DISABLED = "[　　] ";
    private static String TEMPNAME = "$temp";        //未保存設定一時退避機能

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_equalizer_settings);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //オリジナルのEQクラスを作る
        eqConfig = new EQConfig(this);

        //調節できる増幅率の範囲を取得して表示する
        mindB = eqConfig.getMindB();
        maxdB = eqConfig.getMaxdB();
        setMixDBRange(maxdB, mindB);

        //デフォルト設定、カスタム設定のすべてを取ってくる
        EQSettings = eqConfig.getAllEQSettings();
        backupEQSettings = eqConfig.getAllEQSettings();
        Log.d("物体", EQSettings.get(0) == backupEQSettings.get(0) ? "同じ" : "違う");

        //調節できるバンド数を獲得する
        int bands = eqConfig.getBandCount();
        //一時保存しておくバンド数も必要なので、取ったついでに格納しておく

        //各バンドの中心周波数を取ってくる
        int[] centerFreqs = eqConfig.getBandCenterFreqs();

        //重ねがけ表示系のLinearLayoutを取ってくる
        //周波数
        LinearLayout freqlinearLayout = (LinearLayout) findViewById(R.id.MixedBandsLinearLayoutOfHz);
        //増幅率
        final Equalizer.Settings settings = eqConfig.getCombinedSettings();
        LinearLayout dBlinearLayout = (LinearLayout) findViewById(R.id.MixedBandsLinearLayoutOfdB);
        //プログレスバー
        LinearLayout mixedProgressBar = (LinearLayout) findViewById(R.id.mixedProgressBarsLinearLayout);

        /**
         * ここからは後半戦準備
         */
        //
        LinearLayout editBaseLinearLayout = (LinearLayout) findViewById(R.id.baseEditLinearLayout);

        //イコライザの各バンドの数値を表示するテキストビューのリソースIDを格納する配列
        seekBarLevelTextViewId = new int[bands];
        //イコライザのシークバーのリソースIDを格納する配列
        seekBarLevelId = new int[bands];

        //ミックスする方のテキストビューのID格納配列
        mixedBarLevelTextViewId = new int[bands];
        //プログレスバーのID配列
        progressBarLevelId = new int[bands];

        //生成したビューのIDを一時格納する変数
        int viewId;

        //バンドの数だけテキストビューを作って追加する
        for (int i = 0; i < bands; i++) {
            /**
             * 前半戦
             */
            //周波数から
            TextView freqTextView = new TextView(this);
            //テキスト後端揃え
            freqTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            //レイアウトの形式を設定する
            freqTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            freqTextView.setText(String.valueOf(centerFreqs[i]) + "Hz ");
            freqlinearLayout.addView(freqTextView);

            //------------------------------------------------------------------
            //次は増幅率設定
            TextView dbTextView = new TextView(this);
            //テキスト後端揃え
            dbTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            //レイアウトの形式を設定する
            dbTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            //バンドごとの増幅率をループごとに取得して、浮動小数点数に入れる
            double level = settings.bandLevels[i];

            //IDを割り振っておく
            viewId = View.generateViewId();
            mixedBarLevelTextViewId[i] = viewId;
            dbTextView.setId(viewId);

            dbTextView.setText(String.format("%6.2f", (level / 100)) + "dB");
            dBlinearLayout.addView(dbTextView);
            //------------------------------------------------------------------

            //前半戦最後のプログレスバー
            //水平のプログレスバーは、ちょっと特殊な処理が必要
            int id = Resources.getSystem().getIdentifier("progressBarStyleHorizontal", "attr", "android");
            ProgressBar progressBar = new ProgressBar(this, null, id);

            //最大値、現行の値を、数字の範囲を0からの値に補正しながら処理する
            progressBar.setMax(Math.abs(mindB) + maxdB);
            progressBar.setProgress((int) (Math.abs(mindB) + (level)));
            progressBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            //ID割り振り
            viewId = View.generateViewId();
            progressBarLevelId[i] = viewId;
            progressBar.setId(viewId);

            mixedProgressBar.addView(progressBar);


            /**
             * 後半戦
             */

            //1グループはLinearLayoutが2つ
            //上段は中心周波数と増幅率
            //下段はシークバーによる設定

            //じょうだん抜きで進めるのはちょっと辛い
            //先に上段から
            LinearLayout bandBoostTextLinearLayout = new LinearLayout(this);
            //水平に並べる設定にする
            bandBoostTextLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            bandBoostTextLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            TextView explain = new TextView(this);

            //周波数の説明を書き加えていくだけ
/*            String soundText = "";
            if(centerFreqs[i] <= 80){
                soundText += "重低音";
            }
            if(centerFreqs[i] > 80 && centerFreqs[i] <= 150){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "低音";
            }
            if(centerFreqs[i] > 80 && centerFreqs[i] <= 750){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "男性ボーカル";
            }
            if(centerFreqs[i] > 250 && centerFreqs[i] <= 1000){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "女性ボーカル";
            }
            if(centerFreqs[i] > 1000 && centerFreqs[i] <= 4000){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "旋律";
            }
            if(centerFreqs[i] > 4000 && centerFreqs[i] <= 8000){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "シャープさ";
            }
            if(centerFreqs[i] > 8000 && centerFreqs[i] <= 20000){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "空気感";
            }
            if(centerFreqs[i] > 20000){
                soundText += (soundText.length() > 0) ? " / " : "";
                soundText += "超音波";
            }

            explain.setText(soundText);
*/
            explain.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            explain.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
//            bandBoostTextLinearLayout.addView(explain);

            //中心周波数のTextviewオブジェクトは再利用できないので、別に生成する
            TextView bandFreqTextView = new TextView(this);
            //テキスト後端揃え
            bandFreqTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_END);
            //レイアウトの形式を設定する
            bandFreqTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            bandFreqTextView.setText(/*soundText + " " + */String.valueOf(centerFreqs[i]) + "Hz ");

            bandBoostTextLinearLayout.addView(bandFreqTextView);

            //増幅率に関しては、とりあえず0dBで設定して、その後に設定をロードして書き換える
            TextView bandNTextView = new TextView(this);
            //LinearLayout内での配置設定をセットする
            bandNTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            //テキストはビューの始まる方に寄せておく
            bandNTextView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

            //リソースIDを割り振っておく
            viewId = View.generateViewId();
            seekBarLevelTextViewId[i] = viewId;
            bandNTextView.setId(viewId);

            //設定値0dBをセット
            bandNTextView.setText(String.format("%6.2f", ((double) 0)) + "dB");

            bandBoostTextLinearLayout.addView(bandNTextView);
            editBaseLinearLayout.addView(bandBoostTextLinearLayout);

            //------------------------------------------------------------------
            //次は下の段
            //シークバーのあるLinearLayoutを作成
            LinearLayout seekBarLinearLayout = new LinearLayout(this);
            seekBarLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            seekBarLinearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

            //最小音量のテキストビュー
            TextView mindBTextView = new TextView(this);
            mindBTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mindBTextView.setText((double) (mindB / 100) + "dB");
            //追加
            seekBarLinearLayout.addView(mindBTextView);
            //------------------------------------------------------------------

            //次はシークバー
            SeekBar seekBar = new SeekBar(this);
            seekBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
            //最大値設定
            seekBar.setMax(Math.abs(mindB) + maxdB);
            //現在値設定（0dB決め打ち）
            seekBar.setProgress(maxdB);

            //ID生成と記録
            viewId = View.generateViewId();
            seekBarLevelId[i] = viewId;
            seekBar.setId(viewId);

            //追加
            seekBarLinearLayout.addView(seekBar);

            //各シークバーにループ処理でリスナーを追加していく
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    //変更されたら値をテキストビューに表示して、編集配列にも値を入れておく
                    syncSeekbarAndTextViewAndListValue(seekBar.getId());

                    //そしてmixを更新する
                    updateMixedSetting();
                    Log.d("LISTENER", String.valueOf(progress));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            //------------------------------------------------------------------

            //最大音量のテキストビュー
            TextView maxdBTextView = new TextView(this);
            maxdBTextView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
            maxdBTextView.setText("+" + (double) (maxdB / 100) + "dB");
            //追加
            seekBarLinearLayout.addView(maxdBTextView);

            editBaseLinearLayout.addView(seekBarLinearLayout);
        }


        //画面の配置が終わったので、リスナーや初期設定を取り付ける
        //EditTextのカーソル位置を、後端にセットする
        final EditText editText = (EditText) findViewById(R.id.equalizerNameEditText);
        editText.setSelection(editText.getText().length());

        //削除ボタンが使えるときだけ削除できるようにする
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //テキスト変更に対してリアルタイムに反応する
            }

            @Override
            public void afterTextChanged(Editable s) {
                //編集後に呼ばれる
                EditText text = (EditText) findViewById(R.id.equalizerNameEditText);
                Button delbutton = (Button) findViewById(R.id.delButton);

                //もし、カスタム設定で実在する名前ならば削除ボタンを有効、それ以外はは無効に
                delbutton.setEnabled((searchPresetIndexByName(text.getText().toString(), true) >= 0));
            }
        });

        //固定配置系ボタンのリスナー
        //プリセットボタンの初期化
        Button preset = (Button) findViewById(R.id.presetSpinnerLike);
        //プリセットが押されたら
        preset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // デフォルト系の設定をスピナーもといボタンに登録してみる
                //デフォルトの設定を取得してくる

                //返ってきたものがnullじゃなかったら
                if (EQSettings != null) {

                    //デフォルト設定の数だけ配列に格納する必要があるので宣言する
                    String[] itemNames = new String[EQSettings.size()];

                    //表示するためのリスト配列に値を入れていく
                    for (int i = 0; i < itemNames.length; i++) {
                        //適用状態を表示する
                        //ここだけバックアップの状態を表示する
                        itemNames[i] = (EQSettings.get(i).getEnabled()) ? ENABLED : DISABLED;

                        //書き換え可能なものは、通常の記述
                        if (EQSettings.get(i).getCanOverWrite()) {
                            itemNames[i] += (EQSettings.get(i).getPresetName());

                        } else {
                            //書き換えできないものにはデフォルトの文字列をつける
                            itemNames[i] += (DEFAULT + EQSettings.get(i).getPresetName());
                        }

                        //$つきの未保存設定様がいらしたら、名前を書き換える
                        //itemNames[i] = itemNames[i].replace(TEMPNAME,"（未保存の設定）");
                    }

                    //戻るボタンを押したとき、参照型が指すオブジェクトが孤立した状態になっている
                    //孤立したオブジェクトは見捨てて、編集前の値に戻ったリストを指すようにしておく
                    edittingSettingsRef = EQSettings.get(currentShowListindex);
                    showEqualizerSettings(currentShowListindex);


                    //そしてアラートダイアログを作成する
                    AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);

                    //タイトルを付ける
                    builder.setTitle("イコライザー設定");

                    //アラートダイアログにリスト配列が通されたときの動作を設定する
                    builder.setItems(itemNames, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //リストが選択されたときのプログラムをここに書く
                            //まずはその要素番号を取ってくる

                            //要素番号獲得成功
                            //Toast.makeText(EqualizerSettingsActivity.this, String.valueOf(which), Toast.LENGTH_SHORT).show();
                            //設定が変更されていても、保存していない場合は変更を破棄する
                            //オブジェクトを複製する
                            EQsetting writeBack = createEQSetting(
                                    backupEQSettings.get(which).getPresetName(),
                                    backupEQSettings.get(which).getSettings().bandLevels,
                                    EQSettings.get(which).getEnabled());
                            writeBack.setCanOverWrite(backupEQSettings.get(which).getCanOverWrite());

                            //置き換える
                            EQSettings.set(which, writeBack);

                            //選択された番号のイコライザー設定を表示する
                            showEqualizerSettings(which);
                        }
                    });
                    //表示する
                    builder.show();
                }
            }
        });


        //保存ボタンが押されたら
        Button saveButton = (Button) findViewById(R.id.saveButton);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //新規保存か上書き保存か判別しないといけない
                //まずはその名前が使用できるかどうかをチェックする

                EditText namedText = (EditText) findViewById(R.id.equalizerNameEditText);

                //Stringに名前を入れておく
                String name = namedText.getText().toString();

                //まず何らかの理由で取得が失敗した場合
                if (name == null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                    builder.setTitle("処理を続行できません");
                    builder.setMessage("保存するプリセット名が取得できませんでした");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ボタンが押されたら特にすることをここに書く
                            //今のところはないんだけれども
                        }
                    });
                    //表示する
                    builder.show();
                    //そして終了
                    return;
                }

                //次に空文字を入力してきた場合
                if (name.equals("")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                    builder.setTitle("保存できません");
                    builder.setMessage("プリセット名が空欄です");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ボタンが押されたら特にすることをここに書く
                            //今のところはないんだけれども
                        }
                    });
                    //表示する
                    builder.show();
                    //そして終了
                    return;
                }

                //;か@か*があったら、それは保存時に処理できないので、アラートを出して対処する
                if (name.contains(";") || name.contains("@") || name.contains("$")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                    builder.setTitle("保存できません");
                    builder.setMessage("\";\"や\"@\"や\"$\"はプリセット名に含めることができません");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ボタンが押されたら特にすることをここに書く
                            //今のところはないんだけれども
                        }
                    });
                    //表示する
                    builder.show();
                    //そして終了
                    return;
                }

                //プリセット名と同名で存在するかチェック
                int index = searchPresetIndexByName(name, false);
//                Toast.makeText(EqualizerSettingsActivity.this, String.valueOf(index), Toast.LENGTH_SHORT).show();

                //もし、デフォルトに全く同一のプリセット名が存在したらtrue
                if (index >= 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                    builder.setTitle("標準プリセットの保存処理");
                    builder.setMessage("希望の処理を選択してください。\n名前を変えて保存する場合、以下に変更保存されます。\n名前：" + name + "(ユーザー)");
                    builder.setPositiveButton("名前を変えて保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //ボタンが押されたら特にすることをここに書く
                            //続けるフラグをtrueにする
                            EditText namedText = (EditText) findViewById(R.id.equalizerNameEditText);
                            //Stringに名前を入れておく
                            String name = namedText.getText().toString() + "(ユーザー)";

                            //セーブする前に、有効無効の情報をバックアップから戻しておく
                            //でないとすごい音になるかもしれないから
                            EQSettings.get(currentShowListindex).setEnabled(backupEQSettings.get(currentShowListindex).getEnabled());
                            //そしてセーブ
                            saveEdittedSetting(name);

                            //編集画面をリロードして、削除ボタンが使えるようにする
                            showEqualizerSettings(searchPresetIndexByName(name, true));

                        }
                    });
                    builder.setNeutralButton("中止", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //なにもしない
                        }
                    });
                    builder.setNegativeButton("有効/無効のみ保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //有効無効のみ保存
                            backupEQSettings.get(currentShowListindex).setEnabled(edittingSettingsRef.getEnabled());
                            eqConfig.setAllEQsettings(backupEQSettings);
                            eqConfig.saveCustomSettings(EqualizerSettingsActivity.this, currentShowListindex);
                        }
                    });
                    //表示する
                    builder.show();
                    return;
                }
                //何事もなかったら通常保存
                //セーブする前に、有効無効の情報をバックアップから戻しておく
                //でないとすごい音になるかもしれないから
                EQSettings.get(currentShowListindex).setEnabled(backupEQSettings.get(currentShowListindex).getEnabled());
                //そしてセーブ
                saveEdittedSetting(name);
                //編集画面をリロードして、削除ボタンが使えるようにする
                showEqualizerSettings(searchPresetIndexByName(name, true));
            }
        });

        //イコライザの有効ボタンがトグルされたときの動作を設定
        ToggleButton enableToggleButton = (ToggleButton) findViewById(R.id.equalizerEnableToggleButton);
        enableToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //チェックが変わったら、現在のプリセット名と等しい設定があるかを検索する
                EditText namedText = (EditText) findViewById(R.id.equalizerNameEditText);
                String name = namedText.getText().toString();

                for (int i = 0; i < EQSettings.size(); i++) {
                    //同名のプリセットなら、その値に設定する
                    if (EQSettings.get(i).getPresetName().equals(name)) {
                        EQSettings.get(i).setEnabled(isChecked);

                        //そして、表示を更新する
                        updateMixedSetting();

                        //同名はないはずなのでここで去る
                        return;
                    }
                }
                //ここに来たときは未保存で有効にしたいとかそういうの
                //編集中のイコライザ設定が格納される設定をトグルしておく
                edittingSettingsRef.setEnabled(isChecked);
            }
        });

        //削除ボタンが押されたときの動作を設定
        Button delButton = (Button) findViewById(R.id.delButton);
        delButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ボタンが押されたら、そのときの名前で削除処理を行う
                //まずは確認してから
                AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                builder.setTitle("プリセットの削除");
                builder.setMessage("プリセットから削除します。\n"
                        + "名前：" + ((EditText) findViewById(R.id.equalizerNameEditText)).getText().toString()
                        + "\nよろしいですか？");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ボタンが押されたら特にすることをここに書く

                        EditText namedText = (EditText) findViewById(R.id.equalizerNameEditText);
                        //Stringに名前を入れておく
                        String name = namedText.getText().toString();

                        //同名の設定を削除する。重複はないはずだけど、重複しているものはすべて削除
                        for (int i = 0; i < EQSettings.size(); i++) {
                            //カスタム設定で、かつプリセット名が一致したら
                            if (EQSettings.get(i).getPresetName().equals(name) && EQSettings.get(i).getCanOverWrite()) {
                                EQSettings.remove(i);
                                //バックアップの方からも削除する
                                backupEQSettings.remove(i);

                                //そして、削除した設定の1つ後ろ、あるいは最後尾を表示する
                                showEqualizerSettings((i < EQSettings.size()) ? i : EQSettings.size() - 1);
                            }
                        }
                        //削除したので保存しておく
                        eqConfig.setAllEQsettings(EQSettings);
                        //最後に表示した設定が消滅したので、負数を入れておく。識別子はここからの代入が分かるように-2
                        eqConfig.saveCustomSettings(EqualizerSettingsActivity.this, -2);

                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //なにもしない
                    }
                });
                //表示する
                builder.show();
            }
        });

        //復元ボタンが押されたら
        Button restoreButton = (Button) findViewById(R.id.restoreButton);
        restoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(EqualizerSettingsActivity.this);
                builder.setTitle(EQSettings.get(currentShowListindex).getPresetName() + "の再読み込み");
                builder.setMessage("現在の変更を破棄し、最後に保存された設定に復元します。\nよろしいですか？");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //ボタンが押されたら特にすることをここに書く
                        restoreUnChangedSetting(currentShowListindex);
                    }
                });
                builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //なにもしない
                    }
                });
                //表示する
                builder.show();
            }
        });

        //int index = searchPresetIndexByName(TEMPNAME,true);
        //もし、テンポラリ設定のオブジェクトが設定になかったら
/*        if(index < 0){
            //テンポラリ設定のオブジェクトを作る
            edittingSettings = createEQSetting(TEMPNAME,edittingBandLevels,false);
        }else{
            //設定にあったら
            //$tempのデータを編集中の設定に格納する
            edittingSettings = EQSettings.get(index);

            //それを最初に表示する画面として持っておく
            showEqualizerSettings(index);
            
            //読みだしたらあとは不要
            String name = TEMPNAME;
            //$tempと同名の設定を削除する。重複はないはずだけど、重複しているものはすべて削除
            for (int i = 0; i < EQSettings.size(); i++) {
                //カスタム設定で、かつプリセット名が一致したら
                if(EQSettings.get(i).getPresetName().equals(name) && EQSettings.get(i).getCanOverWrite()){
                    EQSettings.remove(i);
                }
            }
            //削除したので保存しておく
            eqConfig.setAllEQsettings(EQSettings);
            eqConfig.saveCustomSettings(EqualizerSettingsActivity.this);
        }
        */

        //一時退避用の変数にオブジェクトをつけておく
        //edittingSettings = createEQSetting(TEMPNAME,new short[seekBarLevelId.length],true);

        //最後に表示されていた設定をプリファレンスから読み出して表示する
        int showindex = eqConfig.getLastShowedIndex();

        //もし、そのインデックス番号が参照できないインデックスを指していた場合True
        if (showindex < 0 || showindex >= EQSettings.size()) {
            //リストの先頭の設定を表示するように設定。ここを変えると好きに変更できる
            showindex = 0;
        }

        showEqualizerSettings(showindex);
    }


    /**
     * 編集された設定を名前をつけて上書きまたは新規保存します
     *
     * @param name
     */
    private void saveEdittedSetting(String name) {
        int index;
        //使用できる名前であれば、次はカスタム設定の中から同名の設定名があるかをチェック
        index = searchPresetIndexByName(name, true);

        //直下の分岐双方で使う変数をここで確保しておく
        //トグルボタンの有効/無効状態を取得してくる
        ToggleButton apply = (ToggleButton) findViewById(R.id.equalizerEnableToggleButton);
        //状態に応じた設定オブジェクトを一旦作成
        EQsetting newFace = createEQSetting(name, edittingSettingsRef.getSettings().bandLevels, apply.isChecked());
        //新顔のクローン作成
        EQsetting cloneFace = createEQSetting(newFace.getPresetName(), newFace.getSettings().bandLevels, newFace.getEnabled());

        //もし、インデックスが負数、あるいは標準プリセット名と同名の場合は、新規保存判定
        if (index < 0 || (!EQSettings.get(index).getCanOverWrite())) {

            //新顔を設定。バックアップデータの配列も同期
            EQSettings.add(newFace);
            backupEQSettings.add(cloneFace);
            //新しい設定ができたので、現在表示している設定番号もそれに応じて変更される
            currentShowListindex = EQSettings.size() - 1;

        } else {
            //そうでない場合は、同名の設定の値を書き換えて対応する
            //バックアップも更新
            EQSettings.set(index, newFace);

            backupEQSettings.set(index, cloneFace);
            currentShowListindex = index;
        }

        //保存というからには、リストをセーブする
        //設定をオブジェクトに渡す
        eqConfig.setAllEQsettings(backupEQSettings);

        //そして保存する
        eqConfig.saveCustomSettings(EqualizerSettingsActivity.this, currentShowListindex);
    }

    /**
     * 保存・適用可能な形式のイコライザ設定を返します。
     * 返される設定はカスタムとして扱われます。
     *
     * @return
     */
    private EQsetting createEQSetting(String name, short[] bandvalues, boolean enabled) {

        //イコライザ設定を作ります
        Equalizer.Settings settings = new Equalizer.Settings();
        //バンド数を設定する
        settings.numBands = (short) bandvalues.length;

        //バンドの各個の値を設定する
        settings.bandLevels = new short[settings.numBands];
        for (int i = 0; i < settings.numBands; i++) {
            settings.bandLevels[i] = bandvalues[i];
        }

        //プリセット番号はカスタムの-1
        settings.curPreset = (short) -1;

        //拡張イコライザ設定を作る
        EQsetting result = new EQsetting(settings, enabled);
        //カスタム設定なので、書き換え可能に設定する
        result.setCanOverWrite(true);
        //名前をつける
        result.setPresetName(name);

        return result;
    }

    /**
     * 指定されたインデックスに該当するイコライザ設定を編集画面にセットします。
     * 異常値は処理されません
     *
     * @param index 表示される設定リストの要素番号
     */
    private void showEqualizerSettings(int index) {

        //もし、番号が非対応の値であった場合は何もしない
        if (index < 0 || index >= EQSettings.size()) {
            return;
        }

        //有効な値だったら、いま表示している設定の番号を変更する
        currentShowListindex = index;

        //オブジェクト参照を取得する
        //値の変更はこのオブジェクトに対して行う
        edittingSettingsRef = EQSettings.get(index);

        //表示する設定のコピーを作成して、保存されなかったときように一時退避させておく
        edittingSettings = new EQsetting(EQSettings.get(index).getSettings(), EQSettings.get(index).getEnabled());
        //EQSettingsは通常カスタム設定用に作ってあるので、上書き属性を取得して更新しておく
        edittingSettings.setCanOverWrite(EQSettings.get(index).getCanOverWrite());
        //名前が未設定なので、これも追加しておく
        edittingSettings.setPresetName(EQSettings.get(index).getPresetName());

        //nowEditPresetName = edittingSettings.getPresetName();

        //edittingSettings.setPresetName(TEMPNAME);

        //タイトルをセットする
        EditText eqTitle = (EditText) findViewById(R.id.equalizerNameEditText);
        String title = edittingSettingsRef.getPresetName();

        //未保存の設定の場合は、空文字列を入れる
        //title = title.replace(TEMPNAME,"");
        eqTitle.setText(title);
        //カーソルを後端にセット
        eqTitle.setSelection(eqTitle.getText().length());


        //削除ボタンが使えるかどうかを設定する
        Button deleteButton = (Button) findViewById(R.id.delButton);
        deleteButton.setEnabled(edittingSettingsRef.getCanOverWrite());

        //各スライダの値を変更する。
        // スライダの値を変更すると、イベントの連鎖で自動的にテキストの値も変わるはず
        for (int i = 0; i < seekBarLevelId.length; i++) {
            //登録済のIDを使って更新するよー
            SeekBar seekBar = (SeekBar) findViewById(seekBarLevelId[i]);
            seekBar.setProgress(Math.abs(mindB) + edittingSettingsRef.getSettings().bandLevels[i]);

            Log.d("スライダ読み込み更新前", String.valueOf(Math.abs(mindB) + EQSettings.get(index).getSettings().bandLevels[i]));
            Log.d("スライダ読み込み更新後", String.valueOf(seekBar.getProgress()));
        }

        //次は有効無効のトグルボタンをセット
        ToggleButton enableButton = (ToggleButton) findViewById(R.id.equalizerEnableToggleButton);
        enableButton.setChecked(edittingSettingsRef.getEnabled());

        //内部ではこの設定からの直接呼び出しをFalseにする
        //EQSettings.get(index).setEnabled(false);

    }


    /**
     * イコライザのシークバーのリソースIDが持つ現在の値を、対応するテキストビューや、
     * メモリ上の設定オブジェクトに反映させます。
     *
     * @param seekbarResId
     */
    private void syncSeekbarAndTextViewAndListValue(int seekbarResId) {
        SeekBar master = (SeekBar) findViewById(seekbarResId);

        //対応する要素番号を探索する
        int i;
        //配列を探索可能で、かつIDが一致しない場合はインクリメントする
        for (i = 0; (i < seekBarLevelId.length && seekBarLevelId[i] != master.getId()); i++) ;

        //もし、配列になかったら、何もせずバイバイ
        if (i == seekBarLevelId.length) {
            return;
        }

        //ここへのコードパスは、一致したIDがあったということ
        //対応するテキストビューを取得して文字列を設定する
        TextView slave = (TextView) findViewById(seekBarLevelTextViewId[i]);
        slave.setText(String.format("%6.2f", ((double) master.getProgress() + mindB) / 100) + "dB");

        //そして、対応するバンド番号にも値を格納する
        Equalizer.Settings setting = edittingSettingsRef.getSettings();
        setting.bandLevels[i] = (short) (master.getProgress() + mindB);
        edittingSettingsRef.setSettings(setting);


        //最後に、一時編集用変数を更新する
        Log.d("Sync", String.valueOf(master.getProgress() + mindB));
    }


    /**
     * Equalizerで設定可能な増幅率の最大最小の画面表示の混合された方を更新します
     *
     * @param max ミリデシベル
     * @param min ミリデシベル
     */
    private void setMixDBRange(int max, int min) {
        TextView minMix = (TextView) findViewById(R.id.mindBMix);
        TextView maxMix = (TextView) findViewById(R.id.maxdBMax);

        minMix.setText((String.valueOf((double) min / 100)) + "dB");
        maxMix.setText("+" + (String.valueOf((double) max / 100)) + "dB");
    }

    /**
     * 検索ワードと同名のプリセットがあるか検索します。
     * 存在しない場合は-1が返されます
     *
     * @param presetName   検索するプリセットの名前
     * @param canOverwrite 検索するプリセットの書き換え属性
     * @return インデックス番号
     */
    private int searchPresetIndexByName(String presetName, boolean canOverwrite) {
        int i;
        //登録済の設定の中を探して、あればその数値を返す
        for (i = 0; i < EQSettings.size(); i++) {
            //同名の設定で、かつ上書き可能属性が条件と一致しているか
            if (EQSettings.get(i).getPresetName().equals(presetName)
                    && (EQSettings.get(i).getCanOverWrite() == canOverwrite)) {
                return i;
            }
        }
        //なかったら負数を返す
        return -1;
    }


    /**
     * 重ねがけイコライザの値を更新します
     */
    private void updateMixedSetting() {
        //テンポラリの設定を追加して渡す
//        EQSettings.add(edittingSettings);

        eqConfig.setAllEQsettings(EQSettings);

        //現行の設定のミックス結果を取得
        Equalizer.Settings settings = new Equalizer.Settings();
        settings = eqConfig.getCombinedSettings();

        //プログレスバーのIDの数だけ処理する
        for (int i = 0; i < progressBarLevelId.length; i++) {
            //プログレスバーの値を更新
            ProgressBar bar = (ProgressBar) findViewById(progressBarLevelId[i]);
            bar.setProgress(settings.bandLevels[i] + Math.abs(mindB));
            Log.d("MIX", String.valueOf(settings.bandLevels[i]));
            //テキストビューの値を更新
            TextView view = (TextView) findViewById(mixedBarLevelTextViewId[i]);
            view.setText(String.format("%6.2f", ((double) settings.bandLevels[i]) / 100) + "dB");
        }

        /**
         * ここにイコライザ設定が変わった旨のシステム設定を送る
         */
        settingTransmission();
    }

    /**
     * 現在の変更を破棄して、最後に保存された設定を復元します
     *
     * @param index
     */
    public void restoreUnChangedSetting(int index) {
        //直前の編集画面の値の設定を放棄する。

        Log.d("currentIndex", String.valueOf(index));

        //設定のバックアップから設定をコピーして書き戻す
        EQsetting restore = createEQSetting(
                backupEQSettings.get(index).getPresetName(),
                backupEQSettings.get(index).getSettings().bandLevels,
                backupEQSettings.get(index).getEnabled());
        restore.setCanOverWrite(backupEQSettings.get(index).getCanOverWrite());

        EQSettings.set(index, restore);
        showEqualizerSettings(index);
        //Toast.makeText(EqualizerSettingsActivity.this, index+"：放棄完了", Toast.LENGTH_SHORT).show();
    }

    /**
     * Mixされたイコライザ設定を送信します。
     */
    private void settingTransmission() {
        boolean enable = false; //イコライザ設定を有効にするかどうか
        for (int i = 0; i < EQSettings.size(); i++) {
            //もし、イコライザが有効なものがあったら
            if (EQSettings.get(i).getEnabled()) {
                enable = true;
                break;//ループ脱出
            }
        }
        Equalizer.Settings packet = eqConfig.getCombinedSettings();

        //バンドの数だけの配列を作る

        //送信準備完了
//        enable;
//        packet;

        //まずコントローラを作ります
        Controller ctrl = Controller.getController();

        //投げます
        ctrl.updateEQSetting(enable, packet);

    }
}
