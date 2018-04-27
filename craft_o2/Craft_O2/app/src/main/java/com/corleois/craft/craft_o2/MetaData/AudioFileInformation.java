package com.corleois.craft.craft_o2.MetaData;

/**
 * Created by 2150254 on 2017/06/08.
 */

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.OriginalExceptions.GabagabaArgumentException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * オーディオファイルの情報を交換するためのオブジェクト。
 */
public class AudioFileInformation {
    private String FilePath;
    private String Hash;

    private int NotExistFlag;

    private ArrayList<String> Title;
    private ArrayList<String> Artist;
    private ArrayList<String> AlbumArtist;
    private ArrayList<String> Album;
    private ArrayList<String> Genre;
    private ArrayList<String> YomiTitle;
    private ArrayList<String> YomiArtist;
    private ArrayList<String> YomiAlbum;
    private ArrayList<String> YomiGenre;
    private ArrayList<String> YomiAlbumArtist;
    private ArrayList<String> ArtWorkPath;
    private int PlaybackCount;
    private int TrackNumber;
    private String AddDateTime;
    private String LastPlayDateTime;
    private String Year;
    private ArrayList<String> Season;

    private Double Rating;

    private ArrayList<String> Comment;
    private int TotalTracks;
    private int TotalDiscs;
    private int DiscNo;
    private ArrayList<String> Lyrics;
    private ArrayList<String> ParentCreation;


    /**
     * こんすとらくたー
     */
    public AudioFileInformation(String FilePath, String Hash) {

        this.FilePath = FilePath;
        this.Hash = Hash;

        this.NotExistFlag = 0;

        this.Title = new ArrayList<>();
        this.Artist = new ArrayList<>();
        this.AlbumArtist = new ArrayList<>();
        this.Album = new ArrayList<>();
        this.Genre = new ArrayList<>();
        this.YomiTitle = new ArrayList<>();
        this.YomiArtist = new ArrayList<>();
        this.YomiAlbum = new ArrayList<>();
        this.YomiGenre = new ArrayList<>();
        this.YomiAlbumArtist = new ArrayList<>();
        this.ArtWorkPath = new ArrayList<>();

        this.PlaybackCount = 0;
        this.TrackNumber = 0;
        setAddDateTime(new Date());
        this.Season = new ArrayList<>();
        setRating(0.0D);
        this.Comment = new ArrayList<>();
        setTotalDiscs(0);
        setDiscNo(0);
        this.Lyrics = new ArrayList<>();
        this.ParentCreation = new ArrayList<>();
    }


    //ファイルパスを取得したり設定したりする
    public void setFilePath(String path) {
        this.FilePath = path;
    }

    public String getFilePath() {
        return this.FilePath;
    }

    //ハッシュを取得したり設定したりする
    public void setHash(String HashString) {
        this.Hash = HashString;
    }

    public String getHash() {
        return this.Hash;
    }

    //データが既に存在するかどうかのフラグを設定する
    public void setNotExistFlag(int NotExistFlag) {
        this.NotExistFlag = NotExistFlag;
    }

    public int getNotExistFlag() {
        return this.NotExistFlag;
    }

    //タイトルを設定する
    public void setTitle(ArrayList<String> title) {
        this.Title = title;
    }

    public ArrayList<String> getTitle() {
        return this.Title;
    }

    public void clearTitle() {
        this.Title.clear();
    }

    public void addTitle(String title) {
        this.Title.add(title);
    }

    public void removeTitle(String title) {
        this.Title.remove(title);
    }

    //アーティストを設定する
    public void setArtist(ArrayList<String> artist) {
        this.Artist = artist;
    }

    public ArrayList<String> getArtist() {
        return this.Artist;
    }

    public void clearArtist() {
        this.Artist.clear();
    }

    public void addArtist(String Artist) {
        this.Artist.add(Artist);
    }

    public void removeArtist(String Artist) {
        this.Artist.remove(Artist);
    }

    //アルバムアーティストを設定する
    public void setAlbumArtist(ArrayList<String> AlbumArtist) {
        this.AlbumArtist = AlbumArtist;
    }

    public ArrayList<String> getAlbumArtist() {
        return this.AlbumArtist;
    }

    public void clearAlbumArtist() {
        this.AlbumArtist.clear();
    }

    public void addAlbumArtist(String AlbumArtist) {
        this.AlbumArtist.add(AlbumArtist);
    }

    public void removeAlbumArtist(String AlbumArtist) {
        this.AlbumArtist.remove(AlbumArtist);
    }

    //アルバムを設定する
    public void setAlbum(ArrayList<String> Album) {
        this.Album = Album;
    }

    public ArrayList<String> getAlbum() {
        return this.Album;
    }

    public void clearAlbum() {
        this.Album.clear();
    }

    public void addAlbum(String Album) {
        this.Album.add(Album);
    }

    public void removeAlbum(String Album) {
        this.Album.remove(Album);
    }

    //Genreを設定する
    public void setGenre(ArrayList<String> Genre) {
        this.Genre = Genre;
    }

    public ArrayList<String> getGenre() {
        return this.Genre;
    }

    public void clearGenre() {
        this.Genre.clear();
    }

    public void addGenre(String Genre) {
        this.Genre.add(Genre);
    }

    public void removeGenre(String Genre) {
        this.Genre.remove(Genre);
    }

    //タイトルの読みを設定する
    public void setYomiTitle(ArrayList<String> YomiTitle) {
        this.YomiTitle = YomiTitle;
    }

    public ArrayList<String> getYomiTitle() {
        return this.YomiTitle;
    }

    public void clearYomiTitle() {
        this.YomiTitle.clear();
    }

    public void addYomiTitle(String YomiTitle) {
        this.YomiTitle.add(YomiTitle);
    }

    public void removeYomiTitle(String YomiTitle) {
        this.YomiTitle.remove(YomiTitle);
    }

    //アーティストのよみを設定する
    public void setYomiArtist(ArrayList<String> YomiArtist) {
        this.YomiArtist = YomiArtist;
    }

    public ArrayList<String> getYomiArtist() {
        return this.YomiArtist;
    }

    public void clearYomiArtist() {
        this.YomiArtist.clear();
    }

    public void addYomiArtist(String YomiArtist) {
        this.YomiArtist.add(YomiArtist);
    }

    public void removeYomiArtist(String YomiArtist) {
        this.YomiArtist.remove(YomiArtist);
    }

    //アルバムのよみを設定する
    public void setYomiAlbum(ArrayList<String> YomiAlbum) {
        this.YomiAlbum = YomiAlbum;
    }

    public ArrayList<String> getYomiAlbum() {
        return this.YomiAlbum;
    }

    public void clearYomiAlbum() {
        this.YomiAlbum.clear();
    }

    public void addYomiAlbum(String YomiAlbum) {
        this.YomiAlbum.add(YomiAlbum);
    }

    public void removeYomiAlbum(String YomiAlbum) {
        this.YomiAlbum.remove(YomiAlbum);
    }

    //ジャンルのよみを設定する
    public void setYomiGenre(ArrayList<String> YomiGenre) {
        this.YomiGenre = YomiGenre;
    }

    public ArrayList<String> getYomiGenre() {
        return this.YomiGenre;
    }

    public void clearYomiGenre() {
        this.YomiGenre.clear();
    }

    public void addYomiGenre(String YomiGenre) {
        this.YomiGenre.add(YomiGenre);
    }

    public void removeYomiGenre(String YomiGenre) {
        this.YomiGenre.remove(YomiGenre);
    }

    //アルバムアーティストの読みを設定する
    public void setYomiAlbumArtist(ArrayList<String> YomiAlbumArtist) {
        this.YomiAlbumArtist = YomiAlbumArtist;
    }

    public ArrayList<String> getYomiAlbumArtist() {
        return this.YomiAlbumArtist;
    }

    public void clearYomiAlbumArtist() {
        this.YomiAlbumArtist.clear();
    }

    public void addYomiAlbumArtist(String YomiAlbumArtist) {
        this.YomiAlbumArtist.add(YomiAlbumArtist);
    }

    public void removeYomiAlbumArtist(String YomiAlbumArtist) {
        this.YomiAlbumArtist.remove(YomiAlbumArtist);
    }

    //アートワークのパスを設定する
    public void setArtWorkPath(ArrayList<String> ArtWorkPath) {
        this.ArtWorkPath = ArtWorkPath;
    }

    public ArrayList<String> getArtWorkPath() {
        return this.ArtWorkPath;
    }

    public void clearArtWorkPath() {
        this.ArtWorkPath.clear();
    }

    public void addArtWorkPath(String ArtWorkPath) {
        this.ArtWorkPath.add(ArtWorkPath);
    }

    public void removeArtWorkPath(String ArtWorkPath) {
        this.ArtWorkPath.remove(ArtWorkPath);
    }

    //再生回数を設定する
    public void setPlaybackCount(int playbackCount) {
        //セットされる再生回数が負数のときは常に0に設定する
        this.PlaybackCount = (playbackCount < 0) ? 0 : playbackCount;
    }

    public int getPlaybackCount() {
        return this.PlaybackCount;
    }

    /**
     * 再生回数をインクリメントします。
     * 符号付き32ビット整数の最大値に到達するまでカウントアップし、Trueを返します
     * 最大値に到達していた場合、カンストしてFalseを返します。
     *
     * @return カウントアップしたかどうか
     */
    public boolean PlaybackCountIncrement() {
        //持ちうる整数の最大値に到達していなかったらT
        if (this.PlaybackCount != 0x7FFFFFFF) {
            this.PlaybackCount++;
            return true;
        } else {
            return false;
        }
    }

    //曲のトラック番号を設定する
    public void setTrackNumber(int trackNumber) {
        //トラック番号が0→トラック番号未設定
        //トラック番号がそれ以外の負数→異常値、0に設定
        this.TrackNumber = (trackNumber < 0) ? 0 : trackNumber;
    }

    public int getTrackNumber() {
        return this.TrackNumber;
    }

    //追加日時を設定する

    /**
     * 追加日時を設定
     *
     * @param AddDateTime
     * @return
     */
    public boolean setAddDateTime(Date AddDateTime) {
        try {
            String DateTimeString = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            DateTimeString = sdf.format(AddDateTime);
            this.AddDateTime = DateTimeString;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setAddDateTime(String AddDateTime){
        this.AddDateTime = AddDateTime;
    }

    public void setAddDateTime(Long addDateTime){
        //Long→Date
        Date date = new Date();
        date.setTime(addDateTime);

        //Stringにして格納
        if(!setAddDateTime(date)){
            //失敗したらこっち
            setAddDateTime(new Date());
        }
    }

    public Date getAddDateTime() {
        try {
            String DateTimeString = this.AddDateTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date DateTime;
            DateTime = sdf.parse(DateTimeString);
            return DateTime;
        } catch (Exception e) {
            return null;
        }
    }

    public String getAddDateTimeByString() {
        return this.AddDateTime;
    }


    public boolean setLastPlayDateTime(Date LastPlayDateTime) {
        try {
            String DateTimeString = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            DateTimeString = sdf.format(LastPlayDateTime);
            this.LastPlayDateTime = DateTimeString;
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setLastPlayDateTime(String LastPlayDateTime) {
        this.LastPlayDateTime = LastPlayDateTime;
    }

    public Date getLastPlayDateTime() {
        try {
            String DateTimeString = this.LastPlayDateTime;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date DateTime;
            DateTime = sdf.parse(DateTimeString);
            return DateTime;
        } catch (Exception e) {
            return null;
        }
    }

    public String getLastPlayDateTimeByString() {
        return this.LastPlayDateTime;
    }

    public boolean setYear(Date Year) {
        //nullチェック
        if (Year == null) {
//            Log.d("setYear", "nullだったよーん");
            this.Year = "";
            return true;
        }

        Log.d("setYear", Year.toString());
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy", Locale.getDefault());

            String date = sdf.format(Year);
//            Log.d("setYear","String:"+date);
//            this.Year = Year;
            this.Year = date;
            return true;
        } catch (Exception e) {
            Log.e("YEAR","セット失敗！"+ "/" + Year + "/" + e.getMessage());
            return false;
        }
    }

    public void setYear(String Year) {
        this.Year = Year;
    }

    public Date getYear() {
        try {
            String DateTimeString = this.Year;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy",Locale.getDefault());
            Date DateTime = sdf.parse(DateTimeString);
            Log.d("getYear",DateTime.toString());
            return  DateTime;
//            return DateTimeString;
        } catch (Exception e) {
            Log.e("getYear", e.getMessage());
            return null;
        }
    }

    public String getYearByString() {
        return this.Year;
    }

    //Seasonを設定する
    public void setSeason(ArrayList<String> Season) {
        this.Season = Season;
    }

    public ArrayList<String> getSeason() {
        return this.Season;
    }

    public void clearSeason() {
        this.Season.clear();
    }

    public void addSeason(String Season) {
        this.Season.add(Season);
    }

    public void removeSeason(String Season) {
        this.Season.remove(Season);
    }

    //Ratingを設定する

    /**
     * レーティングを設定します。
     * 定義外の値は、レーティングで使用される最小値または最大値に修正されます。
     * 返り値は、値が変更なく代入されたかを示します。
     *
     * @param rating
     * @return
     */
    public boolean setRating(Double rating) {
        //値チェック。nullは評価0.0Dとみなす
        if (rating == null || rating < 0.0D) {
            this.Rating = 0.0D;
            return false;
        }
        if (rating > 5.0D) {
            this.Rating = 5.0D;
            return false;
        }
        this.Rating = rating;
        return true;
    }

    public Double getRating() {
        return this.Rating;
    }

    //Commentを設定する
    public void setComment(ArrayList<String> Comment) {
        this.Comment = Comment;
    }

    public ArrayList<String> getComment() {
        return this.Comment;
    }

    public void clearComment() {
        this.Comment.clear();
    }

    public void addComment(String Comment) {
        this.Comment.add(Comment);
    }

    public void removeComment(String Comment) {
        this.Comment.remove(Comment);
    }


    //TotalTracksを設定する
    public void setTotalTracks(int TotalTracks) {
        this.TotalTracks = (TotalTracks < 0) ? 0 : TotalTracks;
    }

    public int getTotalTracks() {
        return this.TotalTracks;
    }

    //TotalDiscsを設定する
    public void setTotalDiscs(int TotalDiscs) {
        this.TotalDiscs = (TotalDiscs < 0) ? 0 : TotalDiscs;
    }

    public int getTotalDiscs() {
        return this.TotalDiscs;
    }

    //DiscNoを設定する
    public void setDiscNo(int DiscNo) {
        this.DiscNo = (DiscNo < 0) ? 0 : DiscNo;
    }

    public int getDiscNo() {
        return this.DiscNo;
    }

    //Lyricsを設定する
    public void setLyrics(ArrayList<String> Lyrics) {
        this.Lyrics = Lyrics;
    }

    public ArrayList<String> getLyrics() {
        return this.Lyrics;
    }

    public void clearLyrics() {
        this.Lyrics.clear();
    }

    public void addLyrics(String Lyrics) {
        this.Lyrics.add(Lyrics);
    }

    public void removeLyrics(String Lyrics) {
        this.Lyrics.remove(Lyrics);
    }

    //ParentCreationを設定する
    public void setParentCreation(ArrayList<String> ParentCreation) {
        this.ParentCreation = ParentCreation;
    }

    public ArrayList<String> getParentCreation() {
        return this.ParentCreation;
    }

    public void clearParentCreation() {
        this.ParentCreation.clear();
    }

    public void addParentCreation(String ParentCreation) {
        this.ParentCreation.add(ParentCreation);
    }

    public void removeParentCreation(String ParentCreation) {
        this.ParentCreation.remove(ParentCreation);
    }

    /**
     * DBのカラム名を使ってデータを取得できるメソッド。
     * 型変換は各自頑張ってください
     *
     * @return 何かのデータ
     */
    public Object getDataByDBColumnName(@NonNull String columnName) {

        //カラム名のリストを取得する
        ArrayList<String> columnList = MusicDBColumns.ColumnList();

        {
            //指定されたカラム名が実際に存在するかどうかを調べる
            boolean valid = false;
            for (int i = 0; i < columnList.size(); i++) {
                valid = columnList.get(i).equals(columnName) | valid;
            }

            //ここに来て、trueになってなかったら、カラム名が不正とみなしてぐっばいしーゆあげーん
            if (!valid) {
                throw new GabagabaArgumentException(this.getClass().toString() + "のgetDataByDBColumnName()の引数に存在しないカラム名が入力されました");
            }
        }

        //カラム名と対応したメソッドを呼び出して返す.
        //もっとスマートにできる方法があればよかったけど……
        switch (columnName) {
            case MusicDBColumns.FilePath:
                return getFilePath();

            case MusicDBColumns.Hash:
                return getHash();

            case MusicDBColumns.NotExistFlag:
                return getNotExistFlag();

            case MusicDBColumns.Title:
                return getTitle();

            case MusicDBColumns.Artist:
                return getArtist();

            case MusicDBColumns.AlbumArtist:
                return getAlbumArtist();

            case MusicDBColumns.Album:
                return getAlbum();

            case MusicDBColumns.Genre:
                return getGenre();

            case MusicDBColumns.YomiTitle:
                return getYomiTitle();

            case MusicDBColumns.YomiArtist:
                return getYomiArtist();

            case MusicDBColumns.YomiAlbum:
                return getAlbum();

            case MusicDBColumns.YomiGenre:
                return getYomiGenre();

            case MusicDBColumns.YomiAlbumArtist:
                return getYomiAlbumArtist();

            case MusicDBColumns.ArtWorkPath:
                return getArtWorkPath();

            case MusicDBColumns.PlaybackCount:
                return getPlaybackCount();

            case MusicDBColumns.TrackNumber:
                return getTrackNumber();

            case MusicDBColumns.AddDateTime:
                return getAddDateTime();

            case MusicDBColumns.LastPlayDateTime:
                return getLastPlayDateTime();

            case MusicDBColumns.Year:
                return getYear();

            case MusicDBColumns.Season:
                return getSeason();

            case MusicDBColumns.Rating:
                return getRating();

            case MusicDBColumns.Comment:
                return getComment();

            case MusicDBColumns.TotalTracks:
                return getTotalTracks();

            case MusicDBColumns.TotalDiscs:
                return getTotalDiscs();

            case MusicDBColumns.DiscNo:
                return getDiscNo();

            case MusicDBColumns.Lyrics:
                return getLyrics();

            case MusicDBColumns.ParentCreation:
                return getParentCreation();

            //デフォルトに到達するときは、すなわちコードの改修が不完全な可能性あり
            default:
                throw new IllegalArgumentException(this.getClass().toString() + "のgetDataByDBColumnName()の引数のカラム名に対応するデータ取得に失敗しました。\nこのメソッドの改修が正しく行われているか確認してください。");
        }
    }
}