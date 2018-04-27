package com.corleois.craft.craft_o2.Activities.fragments;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.corleois.craft.craft_o2.Activities.Interfaces.DatabaseUpdateNotifyListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.SearchQueryListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;
import com.corleois.craft.craft_o2.Activities.Interfaces.AudioGroupRecyclerViewEventListener;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformationAnalyzer;
import com.corleois.craft.craft_o2.MetaData.AudioGroupInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.GroupListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.SongListRecyclerViewAdapter;

import java.util.ArrayList;

/**
 * アーティスト一覧のフラグメント
 */
public class ArtistListFragment2 extends Fragment implements AudioGroupRecyclerViewEventListener,DatabaseUpdateNotifyListener, SearchQueryListener{

	private ArrayList<AudioGroupInformation> audioGroupInformations;
    private ArrayList<AudioFileInformation> audioFileInformations;
    private AudioGroupInformation groupInformation;

	private View rootView;
	private RecyclerView recyclerView;
	private RecyclerView.LayoutManager layoutManager;
	private RecyclerView.Adapter<GroupListRecyclerViewAdapter.ViewHolder> viewHolderAdapter;
    private RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder> viewHolderAdapter2;

	private ArrayList<AudioFileInformation> query;

	//選択中のグループを記憶する
	private AudioGroupInformation nowSelectGroup;
    //このオブジェクトのこと
    private ArtistListFragment2 artistListFragment2 = this;
    private createArtistListThread createArtistListThread;
    private createTrackList createTrackListThread;

    //このオブジェクトのイベントハンドラ
    private Handler fragmentHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            //送られてきたオブジェクトが、AFIクラスだったら
            if(msg.obj.getClass().equals(AudioFileInformation.class)){
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                viewHolderAdapter2 = new SongListRecyclerViewAdapter(audioFileInformations,
                        "artistList",
                        false,
                        (MusicSelectionActivity) getActivity(),
                        artistListFragment2);
                recyclerView.setAdapter(viewHolderAdapter2);

                //もし、選択中のグループに変数が入っていない場合、それはグループリストからタップされて入ってきたとき
                //そしてそれはtrue
                if (nowSelectGroup == null) {
                    ((MusicSelectionActivity) getActivity()).setMethod_onKeyDown(new MusicSelectionActivity.OnKeyDownListener(){
                        @Override
                        public boolean onKeyDown(int keyCode, KeyEvent event) {
                            if(keyCode == KeyEvent.KEYCODE_BACK) {
                                TextNotificationRelay.sendText("読込中");
                                createArtistList(query);
                                ((MusicSelectionActivity) getActivity()).setDefaultKeyDownAction();
                            }
                            return false;
                        }
                    });
                }
                nowSelectGroup = groupInformation;
                //作業してないよって意味のnullにする
                createTrackListThread = null;
                TextNotificationRelay.sendText("");

                //AGIクラスだったら
            }else if(msg.obj.getClass().equals(AudioGroupInformation.class)){
                //グループリストの表示処理をおこなう
                layoutManager = new GridLayoutManager(getContext(), 3);
                recyclerView.setLayoutManager(layoutManager);
                viewHolderAdapter = new GroupListRecyclerViewAdapter(audioGroupInformations,
                        "artistList",
                        artistListFragment2,
                        (MusicSelectionActivity) getActivity());
                recyclerView.setAdapter(viewHolderAdapter);
                TextNotificationRelay.sendText("");
                //作業してないよって意味のnullにする
                createArtistListThread = null;
            }
        }
    };


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
		rootView = inflater.inflate(R.layout.fragment_music_recycler_selection, container, false);
		recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        nowSelectGroup = null;

		//アーティストリストを表示する
		createArtistList(null);

		return rootView;
	}


	/**
	 * ここで、グループを作成する
	 */
	private void createArtistList(ArrayList<AudioFileInformation> songList){

        query = songList;
        nowSelectGroup = null;
        //もし、スレッドがわちゃわちゃしてたら表示しないようお願いする
        if(createArtistListThread != null){
            createArtistListThread.abort();
            createArtistListThread = null;
        }
        createArtistListThread = new createArtistListThread(songList);
        createArtistListThread.start();
	}

    /**
     * アルバムリストを作るスレッドクラスです
     * 処理終了後、画面更新の依頼をハンドラにぶん投げます
     */
    private class createArtistListThread extends Thread{
        private ArrayList<AudioFileInformation> songList;
        private boolean abort;
        /**
         * コンストラクタです
         * @param songList
         */
        public createArtistListThread(ArrayList<AudioFileInformation> songList){
            this.songList = songList;
            abort = false;
        }

        @Override
        public void run() {
            //音楽情報をDBから検索
            //引数がnullなら全件検索
            if(songList == null) {
                //全件検索、つまり検索条件なし
                query = null;

                songList = DBCache.getResultCache(ArtistListFragment2.class);
                if (songList == null) {
                    songList = MusicDBFront.Select(null, getActivity(), true);
                    DBCache.setResultCache(ArtistListFragment2.class, songList);
                }
            }else{
                //nullでないならこっち
                query = songList;   //検索結果を記憶
            }

            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(songList);
            audioGroupInformations = analyzer.makeGroup(MusicDBColumns.Artist); //←ここでアーティスト一覧グループを作成

            if(abort){
                return;
            }

            //あとは、描画処理になるので、ハンドラにブツを投げつけて更新してもらう
            Message message = Message.obtain();
            message.obj = new AudioGroupInformation();
            fragmentHandler.sendMessage(message);
        }
        public void abort(){
            setPriority(MIN_PRIORITY);
            abort = true;
        }

    }


    /**
	 * グループアイテムがクリックされたら呼ばれる
	 */

	@Override
	public void GroupClick(AudioGroupInformation groupInformation, int index) {
        this.groupInformation = groupInformation;
        TextNotificationRelay.sendText("ロード中");

        //ワチャワチャしてたら画面を更新しないようお願いする
        if(createTrackListThread != null){
            createTrackListThread.abort();
            createTrackListThread = null;
        }

        createTrackListThread = new createTrackList(groupInformation, index);
        createTrackListThread.start();
    }

    /**
     * 曲リストを表示するスレッドクラスです。
     * 処理終了後、画面更新の依頼をハンドラにぶん投げます
     */
    private class createTrackList extends Thread{
        private AudioGroupInformation groupInformation;
        private int index;
        private boolean abort;

        public createTrackList(AudioGroupInformation groupInformation, int index){
            this.groupInformation = groupInformation;
            this.index = index;
            abort = false;
        }

        @Override
        public void run() {
            //ここでSongListFragment2へ切り替える
            //v.getParent()でrecyclerViewがゲットできる
            audioFileInformations = groupInformation.getChildGroupAFI(true);
            //そしてソートしておかなきゃ
            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(audioFileInformations);
            audioFileInformations = analyzer.Sort(MusicDBColumns.Title, SortType.ASC);

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
            nowSelectGroup = null;
            setPriority(MIN_PRIORITY);
            abort = true;
        }
    }


    /**
	 * グループアイテムが長押しされたら呼ばれる
	 * @param groupInformation
	 */
	@Override
	public boolean GroupLongClick(AudioGroupInformation groupInformation, int index) {
        return false;
	}

    /**
     * グループアイテムが作成された後にやりたいことがあったら呼ばれる
     * @param groupInformation
     */
    @Override
    public void GroupViewBinded(GroupListRecyclerViewAdapter.ViewHolder viewHolder, AudioGroupInformation groupInformation) {

    }

    /**
     * 画面を更新します
     */
    public void refresh(){
        if(viewHolderAdapter != null) {
            viewHolderAdapter.notifyDataSetChanged();
        }

        if(viewHolderAdapter2 != null) {
            viewHolderAdapter2.notifyDataSetChanged();
        }
    }

	/**
	 * DB更新完了通知が来たら
	 */
	@Override
	public void onDatabaseUpdated() {

		//選択中のグループがなかったらtrue
		if(nowSelectGroup == null){
			createArtistList(query);
		}else{
			//選択中のグループがあったらこっち

			//グループ名はアーティスト名になっている
			String ArtistTitle = nowSelectGroup.getGroupName();

			//同じアルバム名のリストをもう一度取ってくる
			SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.Artist, ArtistTitle, null);
			SQLiteFractalCondition condition = new SQLiteFractalCondition(element);
			ArrayList<AudioFileInformation> newList = MusicDBFront.Select(condition, getContext(), true);

            //ソートする
            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(newList);
            newList = analyzer.Sort(MusicDBColumns.TrackNumber, SortType.ASC);


			//グループリストに入れ直す
			AudioGroupInformation newGroup = new AudioGroupInformation();
			newGroup.setSongList(newList);
			newGroup.setGroupName(ArtistTitle);

			GroupClick(newGroup, 0);
		}
	}

	/**
	 * 検索結果を受け取ると呼ばれるメソッドです
	 *
	 * @param audioFileInformations
	 */
	@Override
	public void onReceiveSearchResult(ArrayList<AudioFileInformation> audioFileInformations) {
		createArtistList(audioFileInformations);
	}

    /**
     * アーティストリスト生成スレッドです。
     * 中身があれば（実行していれば）null以外が返ります
     * @return
     */
    public ArtistListFragment2.createArtistListThread getCreateArtistListThread() {
        return createArtistListThread;
    }
}