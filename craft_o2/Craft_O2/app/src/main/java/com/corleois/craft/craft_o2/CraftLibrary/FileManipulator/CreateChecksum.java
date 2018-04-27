package com.corleois.craft.craft_o2.CraftLibrary.FileManipulator;

/**
 * Created by Mato on 2017/06/19.
 */
import java.io.File;
import java.io.FileInputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;

/*
ここのコードに関しては
http://www.ilovex.co.jp/blog/system/industrysystem/javasha-256.html
の記事をほぼ丸コピしました、念のため目を通しておいてください
 */

public class CreateChecksum {
    //MessageDigestClassに指定するアルゴリズム名
    private static final String ALGORITHM = "SHA-256";

    /**
     * チェックサムファイルを生成します。
     * @param src 生成元のファイル
     * @param sum 生成するチェックサムファイル
     */
    /*
    public static void createChecksumFile(File src, File sum) throws IOException, Exception {
        String sRecord = printDigest(getFileDigest(src));
        PrintStream ps = null;
        try {
            // Streamを作成します。
            ps = new PrintStream(new FileOutputStream(sum), false, "windows-31j");
            ps.println(sRecord);
        } catch (IOException e) {
            throw e;
        } finally {
            if (ps != null) {
                try {ps.flush();} catch (Exception e) {}
                try {ps.close();} catch (Exception e) {}
            }
        }
    }
    */

    /**
     * ファイルからダイジェストを生成します。
     * @param path ダイジェストを生成するファイル
     * @return ダイジェスト(16進数)
     */
    public static byte[] getFileDigest(File path) throws Exception {
        // ファイルの中身からダイジェストを生成する
        /*メッセージダイジェストクラスのインスタンス化
        引数には上記で記述したアルゴリズムを指定する
         */

        MessageDigest md = MessageDigest.getInstance(ALGORITHM);


        int  size = (int)path.length();
        byte bi[] = new byte[size];//ファイルサイズのbyte配列を用意。
        FileInputStream is = new FileInputStream(path);
        is.read(bi);//ファイルバイナリーを一括読み取る。
        is.close();	//ファイルを閉じる。
        md.update(bi);
        //byte[] hash = md.digest();//ハッシュ値を得る(下でreturnしてるのと同じ)

        /*
        DigestInputStream ds = new DigestInputStream(new FileInputStream(path),md);
        FileInputStream in = null;
        try {
            //in = new FileInputStream(path);
            // dat配列の先頭からlenまでのダイジェストを計算する
            byte[] dat = md.digest();

            /*
            int len;
            while ((len = in.read(dat)) >= 0) {
                md.update(dat, 0, len);
            }*/
/*
        } finally {
            if (in != null) {
                try{in.close();} catch (Exception e){}
            }
        }*/
        return md.digest();
    }

    /**
     * ダイジェスト(16進数)からダイジェスト(文字列)を生成します。
     * @param digest ダイジェスト(16進数)
     * @return ダイジェスト(文字列)
     */
    public static String printDigest(byte[] digest) {
        String sSum = "";
        for (int i = 0; i < digest.length; i++) {

            // byte型では128～255が負値になっているので補正
            int d = digest[i];
            if (d < 0) {
                d += 256;
            }

            // 0～15は16進数で1桁のため、2桁になるよう頭に0を追加
            if (d < 16) {
                sSum += "0";
            }
            sSum += Integer.toString(d, 16);
        }

        return sSum;
    }

}