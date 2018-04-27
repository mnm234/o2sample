package com.corleois.craft.craft_o2.CraftLibrary.FileManipulator;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Mato on 2017/06/20.
 */

public class SearchSubFiles {

    /**
     * 引数で受け取ったディレクトリ以下のサブフォルダを含む全てのデータを走査し、指定された拡張子を持つデータのファイルパスをArrayListで返すクラスです
     * @param DirectoryPath 走査したいディレクトリのパス
     * @param Extensions 検索したい拡張子の配列(小文字)
     * @return 発見されたデータ全てのファイルパス
     */
    public static ArrayList<String> Search(String DirectoryPath,String[] Extensions) {
        File[] files = new File(DirectoryPath).listFiles();

        ArrayList<String> songList = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isFile()) {
                    songList.addAll(Search(files[i].getAbsolutePath(),Extensions));
                }
                else {
                    for(String ext : Extensions) {
                        //ファイル名を一旦小文字にして最後が「.↑で指定したやつ」の場合のみ処理
                        if (files[i].getName().toLowerCase().endsWith("." + ext)) {
                            songList.add(files[i].getAbsolutePath());
                        }
                    }
                }
            }
        }
        return  songList;
    }

    public static ArrayList Search(String DirectoryPath) {
        File[] files = new File(DirectoryPath).listFiles();
        //ファイル検索条件、小文字で指定してね
        String[] AudioFileType = {"mp3","wav"};
        ArrayList<String> songList = new ArrayList<>();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isFile()) {
                    songList.addAll(Search(files[i].getAbsolutePath()));
                }
                else {
                    for(String type : AudioFileType) {
                        //ファイル名を一旦小文字にして最後が「.↑で指定したやつ」の場合のみ処理
                        if (files[i].getName().toLowerCase().endsWith("." + type)) {
                            songList.add(files[i].getAbsolutePath());
                        }
                    }

                }
            }
        }

        return  songList;

    }
}