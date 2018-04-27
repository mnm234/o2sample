package com.corleois.craft.craft_o2.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.Preference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;
import com.corleois.craft.craft_o2.MusicDB.Utilities.UpdateDB;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.Activities.fragments.MusicPlayerFragment;

import java.util.ArrayList;

public class SettingsActivity extends AppCompatActivity {

	private LinearLayout oneEarMode_view;
	private int oneEarMode;
	private ImageView leftEar;
	private ImageView rightEar;
	private TextView equalizer;
	private TextView tagEdit;
	private TextView envSetting;
	private Switch privateSwich;
	private TextView eula;

    private SettingsActivity settingsActivity;

	Controller ctr = new Controller();
	static private final int PRIVATE_ON = 6;
	static private final int PRIVATE_OFF = 7;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

		setTitle(getResources().getString(R.string.app_name) + " - 設定");

        settingsActivity = this;

		// [片耳モード]項目
		oneEarMode_view = (LinearLayout)findViewById(R.id.one_ear_mode);
		oneEarMode_view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				switch (oneEarMode) {
					case 0:
						//0(両耳モード)の時、1(右耳モード)に
						leftEar.setVisibility(View.INVISIBLE);
						break;
					case 1:
						//1(右耳モード)の時、2(左耳モード)に
						leftEar.setVisibility(View.VISIBLE);
						rightEar.setVisibility(View.INVISIBLE);
						break;
					case 2:
						//2(左耳モード)の時、0(両耳モード)に
						rightEar.setVisibility(View.VISIBLE);
						break;
				}
				oneEarMode = (oneEarMode + 1) % 3;
				ctr.updateEar(oneEarMode);
                //O2が終了しても設定を維持できるよう、プレファレンスで処理する
                SharedPreferences write = getSharedPreferences("commonSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = write.edit();
                editor.putInt("oneEar", oneEarMode);
                editor.apply();
                //保存完了
			}
		});
		oneEarMode = 0;
		leftEar = (ImageView)findViewById(R.id.ear_left);
		rightEar = (ImageView)findViewById(R.id.ear_right);

		//プライベートモード
		privateSwich = (Switch)findViewById(R.id.private_mode);
        //設定を読み出しておく

		privateSwich.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				//プライベートモードを切り替える

				//O2が終了しても設定を維持できるよう、プレファレンスで処理する
                SharedPreferences write = getSharedPreferences("commonSettings", MODE_PRIVATE);
                SharedPreferences.Editor editor = write.edit();
                editor.putBoolean("private", b);
                editor.apply();
                //保存完了
			}
		});

		// [イコライザー]項目
		equalizer = (TextView) findViewById(R.id.equalizer);
		equalizer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingsActivity.this, EqualizerSettingsActivity.class);
				startActivity(intent);
			}
		});

		// [タグ編集]項目
		tagEdit = (TextView)findViewById(R.id.tagEdit);
		tagEdit.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				String filePath = ctr.GetNowFilePath();
				Intent intent = new Intent(SettingsActivity.this,TagSettingActivity.class);
				intent.putExtra("filePath",filePath);
				startActivity(intent);
			}
		});

		// [環境設定]項目
		envSetting = (TextView)findViewById(R.id.cleanUp);
		envSetting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				//データベースをクリーンアップする

                AlertDialog.Builder builder = new AlertDialog.Builder(settingsActivity);
                builder.setTitle("データベースのクリーンアップ");

                final TextView textView = new TextView(settingsActivity);
                textView.setText(
                        "O2独自のデータベースから、存在しないメディアファイルの情報を削除します。\n" +
                        "操作を続行すると、以下の情報が失われます\n" +
                        "・存在しないメディアファイルに編集していたメタデータ\n" +
                        "\n" +
                        "以下のファイルや情報は失われません\n" +
                        "・端末内に存在する楽曲ファイル\n" +
                        "・端末内で共有しているデータベースの楽曲情報" +
                        "\n" +
                        "\n" +
                        "続行しますか？");
                builder.setView(textView);
                builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //何もしない
                    }
                });

                builder.setPositiveButton("続行する", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //続行するときはこっち
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(settingsActivity);
                        builder1.setTitle("警告！");

                        TextView textView1 = new TextView(settingsActivity);
                        textView1.setText("この操作で失われる情報は、もとに戻すことができません。\n" +
                                "続行を選択すると、バックグラウンドで処理が開始されます");
                        textView1.setTextColor(Color.RED);

                        TextView textView2 = new TextView(settingsActivity);
                        textView2.setText("本当によろしいですか？");

                        LinearLayout linearLayout = new LinearLayout(settingsActivity);
                        linearLayout.setOrientation(LinearLayout.VERTICAL);
                        linearLayout.addView(textView1);
                        linearLayout.addView(textView2);

                        builder1.setView(linearLayout);
                        builder1.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //何もしない
                            }
                        });
                        builder1.setPositiveButton("続行", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //続行するときはこっち
                                HandlerThread thread = new HandlerThread("cleanUpDB");
                                thread.start();
                                Handler handler = new Handler(thread.getLooper());
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        UpdateDB updateDB = new UpdateDB(settingsActivity);
                                        //まずはデータベースを更新
                                        //データの削除を極力避ける
                                        updateDB.SearchAndUpdateDB(true);
                                        updateDB.CheckPathIsExists();
                                        updateDB.TrackTheNotExistRecord();

                                        //最後に、存在しないフラグが立っているレコードを削除
                                        SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.NotExistFlag, "1", null);
                                        SQLiteFractalCondition condition = new SQLiteFractalCondition(element);
                                        MusicDBFront.Delete(condition, settingsActivity);
                                    }
                                });

                            }
                        });
                        builder1.show();
                    }
                });
                builder.show();

			}
		});

		// [使用規約]項目
		eula = (TextView)findViewById(R.id.EULAViewer);
		eula.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(SettingsActivity.this,EULAViewerActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
    public void onResume(){
        super.onResume();
        //設定をプレファレンスから読み出す
        SharedPreferences read = getSharedPreferences("commonSettings", MODE_PRIVATE);
        //プライベートモード
        privateSwich.setChecked(read.getBoolean("private", false));   //プライベートモードを設定

        //片耳イヤホンモード
        oneEarMode = read.getInt("oneEar", 0);
        //読み出し用に改変してます
        switch (oneEarMode) {
            case 0:
                //0(両耳モード)
                leftEar.setVisibility(View.VISIBLE);
                rightEar.setVisibility(View.VISIBLE);
                break;
            case 1:
                //1(右耳モード)
                leftEar.setVisibility(View.INVISIBLE);
                rightEar.setVisibility(View.VISIBLE);
                break;
            case 2:
                //2(左耳モード)
                rightEar.setVisibility(View.INVISIBLE);
                leftEar.setVisibility(View.VISIBLE);
                break;
        }
      /*
                 *0:両耳モード
                 *1:左耳モード
                 *2:右耳モード
                 */
        ctr.updateEar(oneEarMode);
    }
}
