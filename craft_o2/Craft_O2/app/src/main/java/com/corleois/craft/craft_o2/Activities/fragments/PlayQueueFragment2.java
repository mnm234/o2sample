package com.corleois.craft.craft_o2.Activities.fragments;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.corleois.craft.craft_o2.Activities.Interfaces.DatabaseUpdateNotifyListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.SearchQueryListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.GroupListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.SongListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MetaData.AudioGroupInformation;
import com.corleois.craft.craft_o2.MusicDB.Utilities.DBCache;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.SelectedMusic.SelectedMusicList;

import java.util.ArrayList;


/**
 * 再生キューのフラグメント
 */

public class PlayQueueFragment2 extends Fragment implements DatabaseUpdateNotifyListener,SearchQueryListener {

    private MusicPlayerFragment musicPlayerFragment;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder> viewHolderAdapter;
    private ArrayList<AudioFileInformation> songList = new ArrayList<AudioFileInformation>();

    //スワイプ・移動時のアニメーション制御オブジェクト
    private ItemTouchHelper anim;

//    private SongListRecyclerViewAdapter sLRVA = new SongListRecyclerViewAdapter();
//このオブジェクトのこと
private PlayQueueFragment2 playQueueFragment2 = this;
    private createQueueListThread createQueueListThread;

    //このオブジェクトのイベントハンドラ
    private Handler fragmentHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {

            //送られてきたオブジェクトが、AFIクラスだったら
            if(msg.obj.getClass().equals(AudioFileInformation.class)){
                //表示を行う
                layoutManager = new LinearLayoutManager(getContext());
                recyclerView.setLayoutManager(layoutManager);

                viewHolderAdapter = new SongListRecyclerViewAdapter(songList,
                        "queueList",
                        true,
                        (MusicSelectionActivity) getActivity(),
                        playQueueFragment2);
                recyclerView.setAdapter(viewHolderAdapter);

                //アニメーション設定。これがないと順番入れ替えやスワイプが機能しない
                anim = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                        //Log.d("animation", (viewHolder == null ? "null" : viewHolder.getAdapterPosition()) + "→" + (target == null ? "null" : target.getAdapterPosition()));
                        int from =  viewHolder.getAdapterPosition();
                        int to = target.getAdapterPosition();

                        //再生キュー側も入れ替える
                        PlaybackQueue.ExchangeByIndex(from, to);


                        //リストの方の入れ替え
                        AudioFileInformation temp1 = songList.get(from);
                        songList.set(from, songList.get(to));
                        songList.set(to, temp1);
                        viewHolderAdapter.notifyItemMoved(from, to);

                        return true;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                        int pos = viewHolder.getAdapterPosition();
                        //再生キューから削除する
                        PlaybackQueue.RemoveByIndex(pos);
                        //選択中リストから削除する
                        SelectedMusicList.removeSelectedFile("queueList", songList.get(pos).getFilePath());

                        songList.remove(pos);
                        viewHolderAdapter.notifyItemRemoved(pos);
                        viewHolderAdapter.notifyDataSetChanged();
                    }
                });
                anim.attachToRecyclerView(recyclerView);
            }
        }
    };

    private boolean createQueueList(){
        Log.d("Queue","キューのリストを作る準備をするよ");


        //以前のタスクが走っていればキャンセル
        if(createQueueListThread != null){
            createQueueListThread.abort();
            createQueueListThread = null;
        }

        //キューリストの生成を開始
        createQueueListThread = new createQueueListThread();
        createQueueListThread.start();

        return true;
    }

    /**
     * 再生キューをバックで読み出すスレッドです
     */
    public class createQueueListThread extends Thread {

        private boolean abort;
        public createQueueListThread(){
            abort = false;
        }

        @Override
        public void run() {
            songList = new ArrayList<>();

            //全件回していく
            for (int i = 0; i < PlaybackQueue.Count(); i++) {
                //キューのレコードがNULLじゃなかったら
                if (PlaybackQueue.GetRecord(i) != null) {
                    //AFIを求めてリストに入れる
                    AudioFileInformation information = DBCache.SelectByFilePath(PlaybackQueue.GetRecord(i).FilePath, getContext());
                    //ファイル存在しないフラグが立ってないかチェック
                    if(information.getNotExistFlag() == 1){
                        //立ってたらキューに入れても仕方がない
                        continue;
                    }
                    songList.add(information);
                }else{
                    Log.d("QueuePath","Null");
                }
            }
            if(abort) return;

            Message message = Message.obtain();
            message.obj = new AudioFileInformation("","");
            fragmentHandler.sendMessage(message);
        }

        public void abort(){
            setPriority(MIN_PRIORITY);
            abort = true;
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

        musicPlayerFragment = (MusicPlayerFragment) getFragmentManager().findFragmentById(R.id.playerFragment);

//        rootView = inflater.inflate(R.layout.fragment_music_selection, container, false);
        View rootView = inflater.inflate(R.layout.fragment_music_recycler_selection, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        createQueueList();

        return rootView;
    }



    /**
     * 画面を更新します
     */
    public void refresh(){
        if(viewHolderAdapter != null) {
            viewHolderAdapter.notifyDataSetChanged();
        }
    }


    public void setSongList(ArrayList<AudioFileInformation> songList) {
        this.songList = songList;
    }

    @Override
    public void onDatabaseUpdated() {
        //リストをもう一更新する
        createQueueList();
    }

    /**
     * 検索結果を受け取ると呼ばれるメソッドです
     *
     * @param audioFileInformations
     */
    @Override
    public void onReceiveSearchResult(ArrayList<AudioFileInformation> audioFileInformations) {
        createQueueList();
    }
}
