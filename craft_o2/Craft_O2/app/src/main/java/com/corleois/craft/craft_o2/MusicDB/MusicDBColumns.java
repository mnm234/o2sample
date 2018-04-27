package com.corleois.craft.craft_o2.MusicDB;

import java.util.ArrayList;

/**
 *     列名を定義したクラス。
 */
public final class MusicDBColumns {
    public static final String FilePath = "FilePath";
    public static final String Hash = "SHA256";
    public static final String NotExistFlag = "NotExistFlag";
    public static final String Title = "Title";
    public static final String Artist = "Artist";
    public static final String AlbumArtist = "AlbumArtist";
    public static final String Album = "Album";
    public static final String Genre = "Genre";
    public static final String YomiTitle = "YomiTitle";
    public static final String YomiArtist = "YomiArtist";
    public static final String YomiAlbum = "YomiAlbum";
    public static final String YomiGenre = "YomiGenre";
    public static final String YomiAlbumArtist = "YomiAlbumArtist";
    public static final String ArtWorkPath = "ArtWorkPath";
    public static final String PlaybackCount = "PlaybackCount";
    public static final String TrackNumber = "TrackNumber";
    public static final String AddDateTime = "AddDateTime";
    public static final String LastPlayDateTime = "LastPlayDateTime";
    public static final String Year = "Year";
    public static final String Season = "Season";
    public static final String Rating = "Rating";
    public static final String Comment = "Comment";
    public static final String TotalTracks = "TotalTracks";
    public static final String TotalDiscs = "TotalDiscs";
    public static final String DiscNo = "DiscNo";
    public static final String Lyrics = "Lyrics";
    public static final String ParentCreation = "ParentCreation";

    /**
     * すべての列名をリスト形式で返します
     * @return
     */
    public static ArrayList<String> ColumnList(){
        ArrayList<String> result = new ArrayList<>();
        result.add(FilePath);
        result.add(Hash);
        result.add(NotExistFlag);
        result.add(Title);
        result.add(Artist);
        result.add(AlbumArtist);
        result.add(Album);
        result.add(Genre);
        result.add(YomiTitle);
        result.add(YomiArtist);
        result.add(YomiAlbum);
        result.add(YomiGenre);
        result.add(YomiAlbumArtist);
        result.add(ArtWorkPath);
        result.add(PlaybackCount);
        result.add(TrackNumber);
        result.add(AddDateTime);
        result.add(LastPlayDateTime);
        result.add(Year);
        result.add(Season);
        result.add(Rating);
        result.add(Comment);
        result.add(TotalTracks);
        result.add(TotalDiscs);
        result.add(DiscNo);
        result.add(Lyrics);
        result.add(ParentCreation);

        return result;
    }
}
