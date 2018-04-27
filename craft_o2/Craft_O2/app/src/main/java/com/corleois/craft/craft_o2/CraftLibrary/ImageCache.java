package com.corleois.craft.craft_o2.CraftLibrary;

/**
 * Created by StarLink on 2017/08/02.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

/**
 * 処理に時間のかかる画像のI/Oを短縮するための、画像キャッシュクラスです。
 */
public class ImageCache {
    private static int maxEntries = 128;    //画像の最大エントリ数
    private static LruCache<String, Bitmap> cache = new LruCache<>(maxEntries);
    private static LruCache<Integer, Bitmap> resCache = new LruCache<>(maxEntries);  //内部リソースのやつ

    /**
     * 画像をクラスが保持するキャッシュまたは示されたパスから取得します。
     * @param filePath 取得する画像
     * @return 画像データ
     */
    public static Bitmap getImage(String filePath){

        if(filePath == null){
            return null;
        }

        Bitmap bitmap = cache.get(filePath);

        //もしキャッシュになかったら、キャッシュに追加して読み込み
        if(bitmap == null){
            updateCache(filePath);
            bitmap = cache.get(filePath);
        }

        return bitmap;
    }

    /**
     * 画像をクラスが保持するキャッシュまたは示されたリソースから取得します。
     * @param resources 取得する画像
     * @param context
     * @return 画像データ
     */
    public static Bitmap getImage(int resources, Context context){
        Bitmap bitmap = resCache.get(resources);

        //もしキャッシュになかったら、キャッシュに追加して読み込み
        if(bitmap == null){
            updateCache(resources, context);
            bitmap = resCache.get(resources);
        }

        return bitmap;
    }


    /**
     * 画像のキャッシュを、最新のものに更新します。
     * @param filePath
     */
    public static void updateCache(String filePath){
        if(filePath == null){
            return;
        }

        Bitmap bitmap = BitmapFactory.decodeFile(filePath);
        if(bitmap == null){
            return;
        }
        cache.put(filePath, bitmap);
    }

    /**
     * 画像のキャッシュを、最新のものに更新します。
     * @param resources リソースID
     * @param context
     */
    public static void updateCache(int resources, Context context){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resources);
        resCache.put(resources, bitmap);
    }

    /**
     * キャッシュに、引数のファイルパスの画像があれば削除します。
     * なければきっと何も起きません。
     * @param filePath
     */
    public static void removeCache(String filePath){
        cache.remove(filePath);
    }

    /**
     * キャッシュに、引数のファイルパスの画像があれば削除します。
     * なければきっと何も起きません。
     * @param resources
     */
    public static void removeCache(int resources){
        resCache.remove(resources);
    }

    /**
     * キャッシュを全削除し、何もキャッシュされていない初期状態に戻します。
     */
    public static void flashCache(){
        cache = null;
        resCache = null;

        cache = new LruCache<>(maxEntries);
        resCache = new LruCache<>(maxEntries);
    }
}
