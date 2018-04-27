package com.corleois.craft.craft_o2.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

import com.corleois.craft.craft_o2.R;

public class EULAActivity extends AppCompatActivity {

    //契約の改訂番号
    private static final int VERSION = R.integer.agreementVersion;
    private static final String fileName = "EULA_AGREE";
    private static final String agree = "AGREE";
    private static final String version = "VERSION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //プリファレンスからの読み出しを行う
        String readStr = "";
        SharedPreferences preferences = this.getSharedPreferences(fileName, Context.MODE_PRIVATE);

        //もし、すでに現行のバージョンの使用許諾がとれていたら、画面を自動でぶっ飛ばす
        if( preferences.getBoolean(agree,false) && preferences.getInt(version, -1) == VERSION){
            gotoNextActivity();
            return;
        }


        setContentView(R.layout.activity_eula);

        //ボタンのリスナーを作るよっと
        AppCompatCheckBox checkBox = (AppCompatCheckBox) findViewById(R.id.readChkBox);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //チェックが変わったらここが呼ばれる
                Button agree = (Button) findViewById(R.id.agreeButton);

                //読んだらボタンが押せて、読まなかったら押せない
                agree.setEnabled(isChecked);
            }
        });

        Button agree = (Button) findViewById(R.id.agreeButton);
        agree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同意するボタンがクリックされたら
                //まずは設定を保存
                saveAgreement(true);

                //>>>>パーミッションへGO!<<<<<<
                gotoNextActivity();
            }
        });

        Button disagree = (Button) findViewById(R.id.disagreeButton);
        disagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //同意しないボタンが押されたら
                //終了させる

                finish();
            }
        });

    }

    /**
     * 使用許諾契約に関して、同意したかどうかを保存します。
     * 内部では、使用許諾契約に同意した時点での改訂番号も同時に保存されます。
     * @param isAgree
     */
    public void saveAgreement(boolean isAgree){
        SharedPreferences preferences = this.getSharedPreferences(fileName,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        //同意の有無と改訂番号を保存
        editor.putBoolean(agree,isAgree);
        editor.putInt(version,VERSION);
        editor.apply();
    }

    /**
     * 次のアクティビティに行きます
     */
    private void gotoNextActivity(){
        Intent intent = new Intent(EULAActivity.this, PermissionActivity.class);
        startActivity(intent);
        finish();
    }
}
