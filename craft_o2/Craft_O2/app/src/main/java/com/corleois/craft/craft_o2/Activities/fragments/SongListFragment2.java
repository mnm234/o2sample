package com.corleois.craft.craft_o2.Activities.fragments;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.corleois.craft.craft_o2.Activities.Interfaces.DatabaseUpdateNotifyListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.SearchQueryListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.SongListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformationAnalyzer;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;
import com.corleois.craft.craft_o2.R;

import java.util.ArrayList;

/**
 * 曲一覧のフラグメント
 */
public class SongListFragment2 extends Fragment implements DatabaseUpdateNotifyListener ,SearchQueryListener{

	private RecyclerView recyclerView;
	private RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder> viewHolderAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<AudioFileInformation> songList;

    private ArrayList<AudioFileInformation> query;
    private MusicPlayerFragment musicPlayerFragment;

    //このオブジェクトのこと
    private SongListFragment2 songListFragment2 = this;
    private createTrackList createTrackListThread;

    //このオブジェクトのイベントハンドラ
    private Handler fragmentHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            //送られてきたオブジェクトが、AFIクラスだったら
            if(msg.obj.getClass().equals(AudioFileInformation.class)){
                //リストを表示
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                viewHolderAdapter = new SongListRecyclerViewAdapter(songList,
                        "songList",
                        false,
                        (MusicSelectionActivity) getActivity(),
                        songListFragment2);
                recyclerView.setAdapter(viewHolderAdapter);

                TextNotificationRelay.sendText("");
                //作業してないよって意味のnullにする
                createTrackListThread = null;
            }
        }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        View rootView = inflater.inflate(R.layout.fragment_music_recycler_selection, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        refreshSongList(null);

        ViewCompat.setNestedScrollingEnabled(recyclerView, true);

        return rootView;
	}


	public void refreshSongList(ArrayList<AudioFileInformation> audioFileInformations){

        //作業中なら処理を中止する
        if (createTrackListThread != null) {
            createTrackListThread.abort();
            createTrackListThread = null;
        }

        createTrackListThread = new createTrackList(audioFileInformations);
        createTrackListThread.start();

    }


    /**
     * 曲リストを表示するスレッドクラスです。
     * 処理終了後、画面更新の依頼をハンドラにぶん投げます
     */
    private class createTrackList extends Thread{
        private ArrayList<AudioFileInformation> audioFileInformations;
        private boolean abort;

        public createTrackList(ArrayList<AudioFileInformation> audioFileInformations){
            this.audioFileInformations = audioFileInformations;
            abort = false;
        }

        @Override
        public void run() {
            //楽曲リストが渡されていなかったら、全件検索
            if(audioFileInformations == null) {
                query = null;
                //まずは結果がキャッシュされてないか確認して、キャッシュされていなかったら、DBに聞いてみる
                songList = DBCache.getResultCache(this.getClass());
                if (songList == null) {
                    //音楽情報をDBから検索
                    songList = MusicDBFront.Select(null, getActivity(), true);
                    //ソートしてみるテスト
                    AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
                    analyzer.setAnalyzeData(songList);
                    songList = analyzer.Sort(MusicDBColumns.Title, SortType.ASC);

                    DBCache.setResultCache(this.getClass(), songList);
                }
            }else{
                //ソートしてみるテスト
                songList = audioFileInformations;
                AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
                analyzer.setAnalyzeData(songList);
                songList = analyzer.Sort(MusicDBColumns.Title, SortType.ASC);
                query = songList;
            }

            //中断処理が入っていたらtrue
            if(abort){
                return;
            }

            //画面更新の依頼を出す
            Message message = Message.obtain();
            message.obj = new AudioFileInformation("","");
            fragmentHandler.sendMessage(message);
        }

        public void abort(){
            setPriority(MIN_PRIORITY);
            abort = true;
        }
    }

    /**
     * 画面を更新します
     */
	public void refresh(){
        if(viewHolderAdapter != null) {
            viewHolderAdapter.notifyDataSetChanged();
        }
    }


    /**
     * DB更新完了通知がきたら
     */
    @Override
    public void onDatabaseUpdated() {

        //リストをもう一更新する
        refreshSongList(null);

    }

    /**
     * 検索結果を受け取ると呼ばれるメソッドです
     *
     * @param audioFileInformations
     */
    @Override
    public void onReceiveSearchResult(ArrayList<AudioFileInformation> audioFileInformations) {
        refreshSongList(audioFileInformations);
    }

    /**
     * 楽曲リスト生成スレッドを返します
     * @return
     */
    public createTrackList getCreateTrackListThread() {
        return createTrackListThread;
    }

}