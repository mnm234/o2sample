package com.corleois.craft.craft_o2.Activities.fragments;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
 * アルバム一覧のフラグメント
 */
public class AlbumListFragment2 extends Fragment implements AudioGroupRecyclerViewEventListener, DatabaseUpdateNotifyListener, SearchQueryListener{

	private ArrayList<AudioGroupInformation> audioGroupInformations;
    private ArrayList<AudioFileInformation> audioFileInformations;
    private AudioGroupInformation groupInformation;

	private View rootView;
	private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter<GroupListRecyclerViewAdapter.ViewHolder> viewHolderAdapter;
    private RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder> viewHolderAdapter2;

    //検索中はこの文字列が!=null
    private ArrayList<AudioFileInformation> query;

    // コンテキストメニューのアイテムのID
    private static final int CONTEXT_ADDQUEUE = 0;
    private static final int CONTEXT_ADDSELECT = 1;
    private static final int CONTEXT_ADDNONSELECT = 2;
    private static final int CONTEXT_COPYTOPLAYLIST = 3;

    private static final String EXTRA_GROUP_SELECT = "intent.extra.GROUP_SELECT";


    //選択中のグループを記憶する
    private AudioGroupInformation nowSelectGroup;

    //このオブジェクトのこと
    private AlbumListFragment2 albumListFragment2 = this;
    private createAlbumListThread createAlbumListThread;
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
                        "albumList",
                        false,
                        (MusicSelectionActivity) getActivity(),
                        albumListFragment2);
                recyclerView.setAdapter(viewHolderAdapter2);

                //もし、選択中のグループに変数が入っていない場合、それはグループリストからタップされて入ってきたとき
                //そしてそれはtrue
                if (nowSelectGroup == null) {
                    ((MusicSelectionActivity) getActivity()).setMethod_onKeyDown(new MusicSelectionActivity.OnKeyDownListener(){
                        @Override
                        public boolean onKeyDown(int keyCode, KeyEvent event) {
                            if(keyCode == KeyEvent.KEYCODE_BACK) {
                                TextNotificationRelay.sendText("読込中");
                                createAlbumList(query);
                                ((MusicSelectionActivity) getActivity()).setDefaultKeyDownAction();
                            }
                            return false;
                        }
                    });
                }
                nowSelectGroup = groupInformation;
                createTrackListThread = null;
                TextNotificationRelay.sendText("");
                //作業してないよって意味のnullにする

                //AGIクラスだったら
            }else if(msg.obj.getClass().equals(AudioGroupInformation.class)){
                //グループリストの表示処理をおこなう
                layoutManager = new GridLayoutManager(getContext(), 3);
                recyclerView.setLayoutManager(layoutManager);
                viewHolderAdapter = new GroupListRecyclerViewAdapter(audioGroupInformations,
                        "albumList",
                        albumListFragment2,
                        (MusicSelectionActivity) getActivity());
                recyclerView.setAdapter(viewHolderAdapter);
                TextNotificationRelay.sendText("");
                //作業してないよって意味のnull
                createAlbumListThread = null;
            }
        }
    };

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		rootView = inflater.inflate(R.layout.fragment_music_recycler_selection, container, false);

		recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        nowSelectGroup = null;

        //アルバムリストを表示する
        createAlbumList(null);

		return rootView;
	}


	/**
	 * ここで、グループを作成する
	 */
    private void createAlbumList(ArrayList<AudioFileInformation> songList){

        //グループリストを表示するとき、選択中のグループはないのでnullを入れる
        nowSelectGroup = null;

        //もし、スレッドがわちゃわちゃしてたら
        if(createAlbumListThread != null){
            createAlbumListThread.abort();
            createAlbumListThread = null;
        }

        //別スレッド処理だぜー
        createAlbumListThread = new createAlbumListThread(songList);
        createAlbumListThread.start();
	}

    /**
     * アルバムリストを作るスレッドクラスです
     * 処理終了後、画面更新の依頼をハンドラにぶん投げます
     */
	private class createAlbumListThread extends Thread{
        ArrayList<AudioFileInformation> songList;
        private boolean abort;

        /**
         * コンストラクタです
         * @param songList
         */
        public createAlbumListThread(ArrayList<AudioFileInformation> songList){
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

                songList = DBCache.getResultCache(AlbumListFragment2.class);
                if (songList == null) {
                    songList = MusicDBFront.Select(null, getActivity(), true);
                    DBCache.setResultCache(AlbumListFragment2.class, songList);
                }
            }else{
                //nullでないならこっち
                query = songList;   //検索結果を記憶
            }

            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(songList);
            audioGroupInformations = analyzer.makeGroup(MusicDBColumns.Album); //←ここでアルバム一覧グループを作成

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

        //なんか別スレッドでワチャワチャしてたら、画面を更新しないようお願いする
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
            audioFileInformations = analyzer.Sort(MusicDBColumns.TrackNumber, SortType.ASC);

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
     * グループアイテムが長押しされたら呼ばれる
     * @param groupInformation
     */
    @Override
    public boolean GroupLongClick(AudioGroupInformation groupInformation, int index) {

        return false;
    }


//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
//        super.onCreateContextMenu(menu, v, menuInfo);
//        CharSequence playlistName = ((TextView) v.findViewById(R.id.group_title)).getText();
//
//        Intent intent = new Intent();
//        intent.putExtra(EXTRA_GROUP_SELECT, playlistName);
//        menu.setHeaderTitle(playlistName);
//        menu.add(0, CONTEXT_ADDQUEUE, 0, "再生キューに追加する").setIntent(intent);
//        menu.add(0, CONTEXT_ADDSELECT, 0, "グループ内を全選択する").setIntent(intent);
//        menu.add(0, CONTEXT_ADDSELECT, 0, "グループ内の全選択を解除する").setIntent(intent);
//        menu.add(0, CONTEXT_COPYTOPLAYLIST, 0, "プレイリストをコピーする").setIntent(intent);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        return super.onContextItemSelected(item);
//    }

    /**
     * グループアイテムが作成された後にやりたいことがあったら呼ばれる
      * @param viewHolder
     * @param groupInformation
     */
    @Override
    public void GroupViewBinded(GroupListRecyclerViewAdapter.ViewHolder viewHolder, AudioGroupInformation groupInformation) {
        registerForContextMenu(viewHolder.itemView);
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
     * DBの更新が完了したときに呼ばれる
     */
    @Override
    public void onDatabaseUpdated() {
        //選択中のグループがなかったらtrue
        if(nowSelectGroup == null){
            createAlbumList(query);
        }else{
            //選択中のグループがあったらこっち

            //グループ名はアルバム名になっている
            String albumTitle = nowSelectGroup.getGroupName();

            //同じアルバム名のリストをもう一度取ってくる
            SQLiteConditionElement element = new SQLiteConditionElement(SQLiteConditionElement.EQUAL, MusicDBColumns.Album, albumTitle, null);
            SQLiteFractalCondition condition = new SQLiteFractalCondition(element);
            ArrayList<AudioFileInformation> newList = MusicDBFront.Select(condition, getContext(), true);

            //ソートする
            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(newList);
            newList = analyzer.Sort(MusicDBColumns.TrackNumber, SortType.ASC);

            //グループリストに入れ直す
            AudioGroupInformation newGroup = new AudioGroupInformation();
            newGroup.setSongList(newList);
            newGroup.setGroupName(albumTitle);

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
        createAlbumList(audioFileInformations);
    }

    /**
     * アルバムリストのスレッドを返します
     * @return
     */
    public AlbumListFragment2.createAlbumListThread getCreateAlbumListThread() {
        return createAlbumListThread;
    }

}