package com.corleois.craft.craft_o2.CraftLibrary;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * Created by StarLink on 2017/08/23.
 */

public class ImageResizer {
    /**
     * 画面の横幅あるいは縦幅よりも大きな画像を、画面に収まる大きさまで縮小します。
     * もともと画面に収まる画像は処理されずそのまま返されます。
     * @return
     */
    public static Bitmap resizeTooBigBitmap(@NonNull Bitmap bmp){
        //画面の幅と高さを獲得。縦長のとき固定の値だって
        DisplayMetrics displayMetrics = Resources.getSystem().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        //もし、横幅のサイズが画面の幅または長さを超過していたらリサイズ
        //画像の大きさに対して100%未満ならリサイズする。シンプルだね
        double scale = (double) width / (double) bmp.getWidth();
        scale = (((double) height / (double) bmp.getHeight()) < scale) ? (double) height / (double) bmp.getHeight() : scale;    //縦と横、どっちのほうが画面に対して比率的に大きいかをチェック

//                        Log.d("画像サイズ", /*"幅:" + bmp.getWidth() + " / 高さ:" + bmp.getHeight() + " / 画面幅:" + width + " / 画面高さ:" + height +*/ " / スケール:" + scale);
        //画像の縮小が必要と判定されたとき
        if(scale < 1.0D) {
            //縦横比を維持したまま縮小するよー
            bmp = Bitmap.createScaledBitmap(bmp, (int) (bmp.getWidth() * scale), (int) (bmp.getHeight() * scale), true);
        }
     return bmp;
    }
}
