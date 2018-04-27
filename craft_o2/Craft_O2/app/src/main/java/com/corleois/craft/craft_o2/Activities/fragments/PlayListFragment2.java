package com.corleois.craft.craft_o2.Activities.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.corleois.craft.craft_o2.Activities.Interfaces.DatabaseUpdateNotifyListener;
import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.SearchSubFiles;
import com.corleois.craft.craft_o2.Activities.Interfaces.AudioGroupRecyclerViewEventListener;
import com.corleois.craft.craft_o2.CraftLibrary.SortType;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformationAnalyzer;
import com.corleois.craft.craft_o2.MetaData.AudioGroupInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.MusicDB.MusicDBFront;
import com.corleois.craft.craft_o2.MusicDB.SQLiteConditionElement;
import com.corleois.craft.craft_o2.MusicDB.SQLiteFractalCondition;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.SelectedMusic.SelectedMusicList;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.GroupListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.SongListRecyclerViewAdapter;
import com.corleois.craft.craft_o2.playlist.FetchPlaylist;
import com.corleois.craft.craft_o2.playlist.m3u8SaveRead;

import java.util.ArrayList;

/**
 * プレイリスト一覧のフラグメント
 */
public class PlayListFragment2 extends Fragment implements AudioGroupRecyclerViewEventListener,DatabaseUpdateNotifyListener{

	private View rootView;

    private ArrayList<AudioGroupInformation> audioGroupInformations;
    private ArrayList<AudioFileInformation> audioFileInformations;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter<GroupListRecyclerViewAdapter.ViewHolder> viewHolderAdapter;
    private RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder> viewHolderAdapter2;
    private ItemTouchHelper anim;

    private int nowEnteringGroup = -1;  //現在表示している楽曲リストのindex

    private AudioGroupInformation nowSelectGroup;


    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);

		rootView = inflater.inflate(R.layout.fragment_music_recycler_selection, container, false);

        recyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerView);

        nowSelectGroup = null;

        createPlaylist(true);

		return rootView;
	}

	public void createPlaylist(boolean includeAutoGenerateList){
        nowSelectGroup = null;
        //グループを作成
        audioGroupInformations = getPlayList(includeAutoGenerateList);

        layoutManager = new GridLayoutManager(getContext(), 3);
        recyclerView.setLayoutManager(layoutManager);

        viewHolderAdapter = new GroupListRecyclerViewAdapter(audioGroupInformations,
                "playList",
                this,
                (MusicSelectionActivity) getActivity());
        if(anim != null){
            anim.attachToRecyclerView(null);
        }
        recyclerView.setAdapter(viewHolderAdapter);
        TextNotificationRelay.sendText("");
    }

    /**
     * プレイリストを取得します。
     * @param includeAutoGenerateList 自動生成されたプレイリストを含めるかどうか
     * @return
     */
	public ArrayList<AudioGroupInformation> getPlayList(boolean includeAutoGenerateList){
        ArrayList<AudioGroupInformation> res = null;
        //まず最初に自動生成されたリストを獲得
        if(includeAutoGenerateList){
            res = makeAutoGeneratePlayList();
        }

        //もし空っぽなら、オブジェクトを新規作成
        if(res == null){
            res = new ArrayList<>();
        }

        //プレイリストからデータを取ってくるよー
        //m3u8ファイルの検索
        String[] extensions = {"m3u8"};
        ArrayList<String> arrayPlayList = SearchSubFiles.Search(getActivity().getApplicationContext().getFilesDir().getPath(), extensions);

        //Log.d("Directry!!!", getActivity().getApplicationContext().getFilesDir().getPath());
        //結果がnullでないならばTrue
        if(arrayPlayList != null) {
            for (int i = 0; i < arrayPlayList.size(); i++) {

                //拡張子とディレクトリ名を排除し、ファイル名だけを表示させるため、tempに一旦格納する
                String temp = arrayPlayList.get(i);
                //ディレクトリを排除
                temp = temp.replace(getActivity().getApplicationContext().getFilesDir().getPath()+"/","");
                //拡張子を排除
                temp = temp.replace(".m3u8","");

                //グループにリストを格納する
                AudioGroupInformation groupInformation = new AudioGroupInformation();
                ArrayList<AudioFileInformation> audioFileInformations;
                audioFileInformations = FetchPlaylist.getList(temp, getActivity());

                //リストのAFIをセット
                groupInformation.setSongList(audioFileInformations);
                groupInformation.setGroupName(temp);

                //ArrayにAGIを格納
                res.add(groupInformation);
            }
        }
        return res;
    }

    /**
     * 自動生成されたプレイリストを返します。
     * @return
     */
    public ArrayList<AudioGroupInformation> makeAutoGeneratePlayList(){
        //TODO:自動生成するコードをここで書く
        return null;
    }



	/**
	 * コンテキストメニューに関する処理
	 */
	// 最後に長押ししたリストのアイテム
	int lastListItemLongClickPosition;

	// コンテキストメニューのアイテムのID
	private static final int CONTEXT_RENAME = 0;
    private static final int CONTEXT_DELETE = 1;
    private static final int CONTEXT_COPY = 2;

	private static final String EXTRA_PLAYLIST_NAME = "intent.extra.PLAYLIST_NAME";

	/**
	 * コンテキストメニューが表示されるときに呼ばれる。
	 * 表示するアイテムの設定をする
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
        CharSequence playlistName = ((TextView) v.findViewById(R.id.group_title)).getText();

		Intent intent = new Intent();
		intent.putExtra(EXTRA_PLAYLIST_NAME, playlistName);
		menu.setHeaderTitle(playlistName);
		menu.add(0, CONTEXT_RENAME, 0, "プレイリスト名を変更する").setIntent(intent);
        menu.add(0, CONTEXT_DELETE, 0, "プレイリストを削除する").setIntent(intent);
        menu.add(0, CONTEXT_COPY, 0, "プレイリストをコピーする").setIntent(intent);
	}




	/**
	 * コンテキストメニューのアイテムが押された時に呼ばれる
	 */
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		final String playlistName;
		switch (item.getItemId()) {
			case CONTEXT_RENAME:
				// TODO: プレイリスト名を変更する処理＠完了
				playlistName = item.getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);    //変更前のプレイリスト名が入っている
                //Toast.makeText(getActivity().getApplicationContext(), playlistName, Toast.LENGTH_SHORT).show();
                //new AlertDialog.Builder(getActivity()).setTitle("Hi").show();
                final EditText text = new EditText(getActivity());
                text.setInputType(InputType.TYPE_CLASS_TEXT);
                text.setText(playlistName);
                text.setSelection(playlistName.length());

                new AlertDialog.Builder(getActivity())
                        .setTitle("新しい名前を入力してください")
                        .setView(text)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //新しい名前を獲得したら、リネームする
                                m3u8SaveRead saveRead = new m3u8SaveRead(getActivity());
                                //まずは上書き禁止でリネーム
                                int result = saveRead.renamePlayList(playlistName, text.getText().toString(),false);

                                //ファイル名の変更の処理の結果で判別
                                switch (result){
                                    //無効なファイル名だと言われて返ってきた場合
                                    case m3u8SaveRead.INVALID_NAME:
                                        TextView textView = new TextView(getActivity());
                                        textView.setText("ファイル名に無効な文字列が含まれています");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("保存に失敗しました")
                                                .setView(textView)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー。何もしない
                                                    }
                                                })
                                                .show();
                                        break;

                                    //ファイルが既にあると言われた場合
                                    case m3u8SaveRead.ALREADY_EXISTS:
                                        TextView textView1 = new TextView(getActivity());
                                        textView1.setText("新しい名前のプレイリストは既に存在します。\n既存のプレイリストを削除して続行しますか？");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("確認")
                                                .setView(textView1)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー。上書きで保存しちゃう
                                                        m3u8SaveRead saveRead2 = new m3u8SaveRead(getActivity());
                                                        saveRead2.renamePlayList(playlistName, text.getText().toString(),true);

                                                        //リストをもう一度作り直して表示
                                                        createPlaylist(true);

                                                    }
                                                })
                                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //キャンセルされたとき。何もしない
                                                    }
                                                })
                                                .show();
                                        break;
                                    case m3u8SaveRead.SAME_NAME:
                                        TextView textView2 = new TextView(getActivity());
                                        textView2.setText("変更前と変更後のプレイリストの名前が同じです");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("保存に失敗しました")
                                                .setView(textView2)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー何もしない
                                                    }
                                                })
                                                .show();
                                        break;
                                    default:
                                        //普通に書き換えが成功するとこっち
                                        //リストには旧名のリストがあるので、それを探す

                                        for (int i = 0; i < audioGroupInformations.size(); i++) {
                                            //旧名のプレイリストを見つけたら
                                            if(audioGroupInformations.get(i).getGroupName().equals(playlistName)){
                                                //新しい名前のリストと置き換える
                                                ArrayList<AudioFileInformation> temp = FetchPlaylist.getList(text.getText().toString(), getContext());
                                                AudioGroupInformation repGroup = new AudioGroupInformation();
                                                repGroup.setSongList(temp);
                                                repGroup.setGroupName(text.getText().toString());

                                                audioGroupInformations.set(i, repGroup);
                                                //名前が変わったと通知する
                                                viewHolderAdapter.notifyItemChanged(i);
                                                break;
                                            }
                                        }

                                }

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //特になにもしない
                            }
                        })
                        .show();

                break;
			case CONTEXT_DELETE:
				// TODO: プレイリストを削除する処理＠完了
				//有無を言わさず削除する
                playlistName = item.getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);
                m3u8SaveRead saveRead = new m3u8SaveRead(getActivity().getApplicationContext());
                saveRead.deleteList(playlistName);

                //リサイクラービューからの削除処理
                for (int i = 0; i < audioGroupInformations.size(); i++) {
                    //もし、削除されたリスト名と同名の物を見つけたら、削除して通知
                    if(audioGroupInformations.get(i).getGroupName().equals(playlistName)){
                        audioGroupInformations.remove(i);
                        viewHolderAdapter.notifyItemRemoved(i);
                    }
                }
                break;

            case CONTEXT_COPY:
                //コピーする処理
                playlistName = item.getIntent().getStringExtra(EXTRA_PLAYLIST_NAME);

                final EditText text1 = new EditText(getActivity());
                text1.setInputType(InputType.TYPE_CLASS_TEXT);
                text1.setText(playlistName);
                text1.setSelection(playlistName.length());

                new AlertDialog.Builder(getActivity())
                        .setTitle("新しい名前を入力してください")
                        .setView(text1)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //新しい名前を獲得したら、リネームする
                                m3u8SaveRead saveRead = new m3u8SaveRead(getActivity());
                                //まずは上書き禁止でリネーム
                                int result = saveRead.copyList(playlistName, text1.getText().toString(),false);

                                //ファイル名の変更の処理の結果で判別
                                switch (result){
                                    //無効なファイル名だと言われて返ってきた場合
                                    case m3u8SaveRead.INVALID_NAME:
                                        TextView textView = new TextView(getActivity());
                                        textView.setText("ファイル名に無効な文字列が含まれています");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("保存に失敗しました")
                                                .setView(textView)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー。何もしない
                                                    }
                                                })
                                                .show();
                                        break;

                                    //ファイルが既にあると言われた場合
                                    case m3u8SaveRead.ALREADY_EXISTS:
                                        TextView textView1 = new TextView(getActivity());
                                        textView1.setText("新しい名前のプレイリストは既に存在します。\n既存のプレイリストを削除して続行しますか？");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("確認")
                                                .setView(textView1)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー。上書きで保存しちゃう
                                                        m3u8SaveRead saveRead2 = new m3u8SaveRead(getActivity());
                                                        saveRead2.copyList(playlistName, text1.getText().toString(),true);

                                                        //リストをもう一度作り直して表示
                                                        createPlaylist(true);
                                                    }
                                                })
                                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //キャンセルされたとき。何もしない
                                                    }
                                                })
                                                .show();
                                        break;
                                    case m3u8SaveRead.SAME_NAME:
                                        TextView textView2 = new TextView(getActivity());
                                        textView2.setText("変更前と変更後のプレイリストの名前が同じです");

                                        new AlertDialog.Builder(getActivity())
                                                .setTitle("保存に失敗しました")
                                                .setView(textView2)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        //OKを押されたときのリスナー何もしない
                                                    }
                                                })
                                                .show();
                                        break;
                                    default:
                                        //普通にコピーが成功するとこっち
                                        //プレイリストの最後尾に追加
                                        ArrayList<AudioFileInformation> temp1 = FetchPlaylist.getList(text1.getText().toString(), getContext());

                                        AudioGroupInformation temp = new AudioGroupInformation();
                                        temp.setGroupName(text1.getText().toString());
                                        temp.setSongList(temp1);

                                        audioGroupInformations.add(temp);
                                        viewHolderAdapter.notifyItemInserted(audioGroupInformations.size() - 1);

                                }

                            }
                        })
                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //特になにもしないけれど
                            }
                        })
                        .show();
                break;
		}
		return true;
	}

    /**
     * グループがタップされたら走る
     * @param groupInformation
     */
    @Override
    public void GroupClick(final AudioGroupInformation groupInformation, final int index) {
        //ここでSongListFragment2へ切り替える
        nowSelectGroup = groupInformation;
        nowEnteringGroup = index;

        //v.getParent()でrecyclerViewがゲットできる
        audioFileInformations = groupInformation.getChildGroupAFI(true);
        //プレイリストはソートしては意味がないので、そのまま次の処理へ

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        viewHolderAdapter2 = new SongListRecyclerViewAdapter(audioFileInformations, "playList", true, (MusicSelectionActivity) getActivity(), this);
        recyclerView.setAdapter(viewHolderAdapter2);
        anim = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                //Log.d("animation", (viewHolder == null ? "null" : viewHolder.getAdapterPosition()) + "→" + (target == null ? "null" : target.getAdapterPosition()));
                int from =  viewHolder.getAdapterPosition();
                int to = target.getAdapterPosition();

                //プレイリストも入れ替える
                String listName = audioGroupInformations.get(index).getGroupName();
                m3u8SaveRead saveRead = new m3u8SaveRead(getContext());
                ArrayList<String> strings = saveRead.openList(listName);

                //ファイルの方の入れ替え
                String temp = strings.get(from);
                strings.set(from, strings.get(to));
                strings.set(to, temp);
                saveRead.saveList(strings,listName);

                //リストの方の入れ替え
                AudioFileInformation temp1 = audioFileInformations.get(from);
                audioFileInformations.set(from, audioFileInformations.get(to));
                audioFileInformations.set(to, temp1);

                viewHolderAdapter2.notifyItemMoved(from, to);

                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int pos = viewHolder.getAdapterPosition();
                //プレイリストからも削除する
                String listName = audioGroupInformations.get(index).getGroupName();
                m3u8SaveRead saveRead = new m3u8SaveRead(getContext());
                ArrayList<String> strings = saveRead.openList(listName);
                //その前に選択中リストから削除する
                SelectedMusicList.removeSelectedFile("playList", strings.get(pos));

                strings.remove(pos);
                saveRead.saveList(strings,listName);


                audioFileInformations.remove(pos);
                viewHolderAdapter2.notifyItemRemoved(pos);
            }
        });
        anim.attachToRecyclerView(recyclerView);



        ((MusicSelectionActivity) getActivity()).setMethod_onKeyDown(new MusicSelectionActivity.OnKeyDownListener(){
            @Override
            public boolean onKeyDown(int keyCode, KeyEvent event) {
                if(keyCode == KeyEvent.KEYCODE_BACK) {
                    createPlaylist(true);
                    ((MusicSelectionActivity) getActivity()).setDefaultKeyDownAction();
                }
                return false;
            }
        });
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
     * DB更新完了通知が来たら
     */
    @Override
    public void onDatabaseUpdated() {

        //選択中のグループがなかったらtrue
        if(nowSelectGroup == null){
            createPlaylist(true);
        }else{
            //選択中のグループがあったらこっち
            //プレイリストを再取得
            audioGroupInformations = getPlayList(true);

            //同じプレイリストがあるかどうか探す
            for (int i = 0; i < audioGroupInformations.size(); i++) {
                //同じプレイリスト名を見つけたらtrue
                if(audioGroupInformations.get(i).getGroupName().equals(nowSelectGroup.getGroupName())){
                    GroupClick(audioGroupInformations.get(i), i);
                    return;
                }
            }
            //同じプレイリストが見つからなかったら
            //プレイリストグループ画面に戻ろう
            createPlaylist(true);

        }
    }
}
