package com.corleois.craft.craft_o2.Activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotification;
import com.corleois.craft.craft_o2.Activities.Interfaces.TextNotificationRelay;
import com.corleois.craft.craft_o2.Activities.fragments.PlayQueueFragment2;
import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.CraftLibrary.ApplicationContextHolder;
import com.corleois.craft.craft_o2.CraftLibrary.FileManipulator.SearchSubFiles;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.MusicDB.Utilities.UpdateDB;
import com.corleois.craft.craft_o2.MusicPlayerMotionController;
import com.corleois.craft.craft_o2.PlaybackQueue.AudioFileQueueList;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.SelectedMusic.SelectedMusicList;
import com.corleois.craft.craft_o2.Activities.fragments.AlbumListFragment2;
import com.corleois.craft.craft_o2.Activities.fragments.ArtistListFragment2;
import com.corleois.craft.craft_o2.Activities.fragments.MusicPlayerFragment;
import com.corleois.craft.craft_o2.Activities.fragments.PlayListFragment2;
import com.corleois.craft.craft_o2.Activities.fragments.SongListFragment2;
import com.corleois.craft.craft_o2.VagueSearch;
import com.corleois.craft.craft_o2.playlist.m3u8SaveRead;

import java.util.ArrayList;

import static java.lang.Thread.sleep;


public class MusicSelectionActivity extends AppCompatActivity implements TextNotification{

	private Toolbar toolbar;
	private SearchView searchView;
	private ViewPager viewPager;

    private AudioFileQueueList selectingList;

    private int selectedTab;

	private MusicPlayerFragment musicPlayerFragment;
    private Activity thisActivity;

    private AlbumListFragment2 albumListFragment2;
    private ArtistListFragment2 artistListFragment2;
    private SongListFragment2 songListFragment2;
    private PlayListFragment2 playListFragment2;
    private PlayQueueFragment2 playQueueFragment2;


    private volatile TextView notificationText;  //通知のテキストビュー
    private Handler activityHandler = new Handler(){    //テキストビューに通知を行うためのハンドラ
        //どっかのスレッドからメッセージゲット！
        @Override
        public void handleMessage(Message message){
            //もし、メッセージオブジェクトがString型だったらTrue
            if(message.obj.getClass().equals(String.class)){
                showTextMessage((String) message.obj);

                //もし、渡されたのがboolean型だったら、更新
            }else if(message.obj.getClass().equals(Boolean.class)){

            allFragmentUpdate();

                //サーチビューなら、更新せよの意味深な合図
            }else if(message.obj.getClass().equals(EditText.class)){
                searchView.setQuery(searchView.getQuery(),true);
            }
        }
    };



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_selection);
		musicPlayerFragment = null;

        thisActivity = this;
//        Toast.makeText(thisActivity, "おんくり！", Toast.LENGTH_SHORT).show();

        //通知領域の初期化
        notificationText = (TextView)findViewById(R.id.notification_textView);
        TextNotificationRelay.setNotification(activityHandler);

		setVolumeControlStream(AudioManager.STREAM_MUSIC);

        //複数選択を完全リセット
        SelectedMusicList.deleteAllSelectedList();

		// ツールバーの設定
		toolbar = (Toolbar) findViewById(R.id.toolbar);
        setDefaultOptionMenu();

		// コンテナ(fragment)の設定
		SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.container);
		viewPager.setAdapter(sectionsPagerAdapter);

        //ページが変わったとかどうこうしたときのリスナー
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                //ページの移動が確定したら実行
                selectedTab = position;

                switch (selectedTab){
                    case 0:
                        if (albumListFragment2 != null && albumListFragment2.getCreateAlbumListThread() != null) {
                            TextNotificationRelay.sendText("読込中");
                        }
                        break;
                    case 1:
                        if (artistListFragment2 != null && artistListFragment2.getCreateArtistListThread() != null) {
                            TextNotificationRelay.sendText("読込中");
                        }
                        break;
                    case 2:
                        if (songListFragment2 != null && songListFragment2.getCreateTrackListThread() != null) {
                            TextNotificationRelay.sendText("読込中");
                        }
                        break;
//                    case 3:
//                        if (playListFragment2.getCreateAlbumListThread() == null) {
//                            TextNotificationRelay.sendText("読込中");
//                        }
//                        break;
//                    case 4:
//                        if (playQueueFragment2.getCreateAlbumListThread() == null) {
//                            TextNotificationRelay.sendText("読込中");
//                        }
//                        break;
                }

                //検索文字列が空ならここで終了
                if(searchView.getQuery().toString().length() == 0
                        || searchView.getQuery().toString().matches("^ *$")){
                    return;
                }

                //500ms秒後に検索クエリを投げるスレッドを作る
                HandlerThread thread = new HandlerThread("search");
                thread.start();
                final Handler handler = new Handler(thread.getLooper());
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Message message = Message.obtain();
                        message.obj = "処理中";
                        activityHandler.sendMessage(message);

                        try {
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        //検索してねメッセージを意味深に送る
                         message = Message.obtain();
                        message.obj = new EditText(getApplicationContext());
                        activityHandler.sendMessage(message);
                    }
                });

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


		// タブの設定
		TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setNestedScrollingEnabled(true);
		String[] tabNames = {"ALBUM", "ARTISTS", "SONGS", "PLAYLISTS","QUEUE"};
		int[] tabIcons = {R.drawable.album, R.drawable.artist, R.drawable.music, R.drawable.playlist, R.drawable.ic_menu_slideshow};
		for (int i = 0; i < tabNames.length; i++) {
			TabLayout.Tab tab = tabLayout.getTabAt(i);
			tab.setText(tabNames[i]);
//			tab.setIcon(new BitmapDrawable(ImageCache.getImage(tabIcons[i], thisActivity)));
		}

        Log.d("MusicSelection", "データベースの更新をバックグラウンドで指示");

        updateDB();

		setDefaultKeyDownAction();

        //アプリケーションContextをクラスに入れる
        ApplicationContextHolder.setApplicationContextToHolder(getApplication());
	}

    /**
     * フラグメントが復帰したら呼ばれる
     */
	@Override
    public void onResumeFragments(){
        super.onResumeFragments();
        allFragmentUpdate();
    }

    @Override
    public void onResume(){
        super.onResume();

//        Toast.makeText(thisActivity, "ふらぐめんとりじゅーむ！", Toast.LENGTH_SHORT).show();
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.playerFragment);
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if(fragment != null){
            transaction.attach(fragment);
        }
        transaction.commit();

        MusicPlayerFragment musicPlayerFragment = (MusicPlayerFragment) fragmentManager.findFragmentById(R.id.playerFragment);
        musicPlayerFragment.setCtr(Controller.getController());
    }

    /**
     * 生きてるフラグメントに、データが更新されたことを通知するメソッドです
     */
    public void allFragmentUpdate(){
        if(albumListFragment2 != null){
            albumListFragment2.onDatabaseUpdated();
        }
        if(artistListFragment2 != null){
            artistListFragment2.onDatabaseUpdated();
        }
        if(songListFragment2 != null){
            songListFragment2.onDatabaseUpdated();
        }
        if(playListFragment2 != null){
            playListFragment2.onDatabaseUpdated();
        }
        if(musicPlayerFragment != null){
            musicPlayerFragment.onDatabaseUpdated();
        }
        if(playQueueFragment2 != null){
            playQueueFragment2.onDatabaseUpdated();
        }
    }

    /**
     * 再生キューFragmentに、再生キューが更新されたことを通知するメソッドです
     */
    public void queueFragmentUpdate(){
        if(playQueueFragment2 != null){
            playQueueFragment2.onDatabaseUpdated();
        }
    }

    /**
     * Viewが表示されたあとにする処理
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (musicPlayerFragment == null) {
            musicPlayerFragment = (MusicPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.playerFragment);
            musicPlayerFragment.onWindowFocusChanged(hasFocus);
        }
    }


	public void updateDB(){
        //スレッド作って実行するよー
        HandlerThread thread = new HandlerThread("upDB");
        thread.start();
        //コンストラクタの引数に、作ったばかりのスレッドのLooperを指定する
        final Handler handler = new Handler(thread.getLooper());

        //スレッドに実行するコードをﾎﾟｲｰ
        handler.post(new Runnable() {
            @Override
            public void run() {

                //メッセージを作成。お古の使い回しでもおｋ
                Message message = Message.obtain();
                message.obj = "データベース更新処理の準備中です";
                activityHandler.sendMessage(message);
                UpdateDB updateDB = new UpdateDB(getApplicationContext());

                message = Message.obtain();
                message.obj = "データーベースを更新しています";
                activityHandler.sendMessage(message);
                updateDB.SearchAndUpdateDB(true);
                
                //一旦ここで画面更新
                message = Message.obtain();
                message.obj = true;
                activityHandler.sendMessage(message);
                
                
                updateDB.attachToAlbumArtAndCheckSum();

                message = Message.obtain();
                message.obj = "ファイルを追跡しています";
                activityHandler.sendMessage(message);
                updateDB.TrackTheNotExistRecord();

                message = Message.obtain();
                message.obj = "データーベースの更新が終了しました";
                activityHandler.sendMessage(message);
                message = Message.obtain();
                message.obj = true;
                activityHandler.sendMessage(message);
                try {
                    sleep(2000);
                }catch (InterruptedException e){

                }
                message = Message.obtain();
                message.obj = "";
                activityHandler.sendMessage(message);
            }
        });
    }

    /**
     * 対応したアクティビティで、テキスト通知を行います。
     * テキストの内容が空文字、あるいはnullの場合、通知領域は非表示になります。
     *
     * @param message    　メッセージ内容
     */
    @Override
    public void showTextMessage(String message) {

        //nullまたは空文字なら非表示にする
        notificationText.setVisibility((message == null || message.equals("")) ? View.INVISIBLE : View.VISIBLE);
        notificationText.setText((message == null || message.equals("")) ? "" : message);
    }


	/**
	 * 以下、OptionMenuに関する処理
	 */
	private OnCreateOptionMenuListener onCreateOptionMenuListener;



    public interface OnCreateOptionMenuListener {
		void onCreateOptionMenu(Menu menu);
	}

	public void setDefaultOptionMenu() {
		setMethod_onCreateOptionMenu(new OnCreateOptionMenuListener() {
			@Override
			public void onCreateOptionMenu(Menu menu) {
				toolbar.setTitle(R.string.app_name);
				toolbar.inflateMenu(R.menu.activity_music_selection);
				toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						switch (item.getItemId()) {
							// 設定ボタンのクリック
							case R.id.action_settings:
								// 設定画面を呼び出す
								Intent intent = new Intent(MusicSelectionActivity.this, SettingsActivity.class);
								startActivity(intent);
						}
						return false;
					}
				});

				// 検索欄の設定
				MenuItem menuItem = menu.findItem(R.id.action_search);
				searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
				searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

					/**
					 * 検索文字列が変更されるたびに呼ばれる
					 */
					@Override
					public boolean onQueryTextChange(String newText) {

                        ArrayList<AudioFileInformation> audioFileInformations = null;
                        if(newText != null && !newText.equals("")) {
                            VagueSearch vagueSearch = new VagueSearch();
                            audioFileInformations = vagueSearch.VagueSearch(newText, selectedTab, thisActivity);
                        }

                        //nullを入れるとデフォルトの検索結果表示
                        if(albumListFragment2 != null) {
                            albumListFragment2.onReceiveSearchResult(selectedTab == 0 ? audioFileInformations : null);
                        }
                        if(artistListFragment2 != null) {
                            artistListFragment2.onReceiveSearchResult(selectedTab == 1 ? audioFileInformations : null);
                        }
                        if(songListFragment2 != null) {
                            songListFragment2.onReceiveSearchResult(selectedTab == 2 ? audioFileInformations : null);
                        }

                        //Log.d("test",String.valueOf(audioFileInformations.size()));
						return false;
					}

					/**
					 * 検索文字列がEnterで確定された時に呼ばれる
					 */
					@Override
					public boolean onQueryTextSubmit(String query) {
                        ArrayList<AudioFileInformation> audioFileInformations = null;
                        if(query != null && !query.equals("")) {
                            VagueSearch vagueSearch = new VagueSearch();
                            audioFileInformations = vagueSearch.VagueSearch(query, selectedTab, thisActivity);
                        }

                        //nullを入れるとデフォルトの検索結果表示

                        //nullを入れるとデフォルトの検索結果表示
                        if(albumListFragment2 != null) {
                            albumListFragment2.onReceiveSearchResult(selectedTab == 0 ? audioFileInformations : null);
                        }
                        if(artistListFragment2 != null) {
                            artistListFragment2.onReceiveSearchResult(selectedTab == 1 ? audioFileInformations : null);
                        }
                        if(songListFragment2 != null) {
                            songListFragment2.onReceiveSearchResult(selectedTab == 2 ? audioFileInformations : null);
                        }

                        //カスタム通知領域があったら消しておく
                        showTextMessage("");
                        //Log.d("test",String.valueOf(audioFileInformations.size()));
						return false;
					}
				});
			}
		});
	}

	public void setMethod_onCreateOptionMenu(OnCreateOptionMenuListener listener) {
		this.onCreateOptionMenuListener = listener;
		setSupportActionBar(toolbar);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		onCreateOptionMenuListener.onCreateOptionMenu(menu);
		return true;
	}

    //改造

    /**
     * 選択された楽曲に変化があったかもしれないときに呼び出されてほしいメソッド。
     */
    public void notifyToSelectedItemChanged(){
        ArrayList<String> lists = SelectedMusicList.getListName();
        int listCount = lists.size();

        //もし、選択リストに何かあったら処理に入る
        if(listCount != 0){
            //すべての登録されている曲リストを取得
            selectingList = new AudioFileQueueList();
            for (int i = 0; i < listCount; i++) {
                //1リストの楽曲をすべて吸い取る
                AudioFileQueueList temp = SelectedMusicList.getSelectedList(lists.get(i));
                if(temp != null) {
                    for (int j = 0; j < temp.Count(); j++) {
                        selectingList.Add(temp.AudioList.get(j).FilePath);
                    }
                }
            }
            //もし、リストはあったけど何も入っていなかったらtrue
            if(selectingList.Count() == 0){
                //元の画面に戻す
                setDefaultOptionMenu();
                return;
            }
            //個数カウントとアレを表示する

            setSelectingOptionMenu();

            //ここに来たら、キューに入ってる楽曲の総数が分かるので、画面の表示を更新する
        }else{
            //ここに来たら、元の画面に戻す
            setDefaultOptionMenu();
        }
    }

    public void setSelectingOptionMenu() {
        //すでに選択中であれば、選択肢を追加しない
        if(!toolbar.getTitle().toString().contains("選択中")) {
            toolbar.inflateMenu(R.menu.activity_music_selection_selecting);
        }

        toolbar.setTitle("選択中: " + selectingList.Count());
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    // ツールバーの[プレイリストに追加]をクリックした時
                    case R.id.action_addToPlaylist:
                        // プレイリスト一覧を表示する
                        AlertDialog.Builder builder = new AlertDialog.Builder(thisActivity);
                        // TODO: String型配列(playlist)にプレイリスト名を代入@完了
                        //指令に従うため、まずはプレイリスト名を取得してくる

                        //m3u8ファイルの検索してリストに持ってくる
                        String[] extensions = {"m3u8"};
                        ArrayList<String> arrayPlayList = SearchSubFiles.Search(thisActivity.getFilesDir().getPath(), extensions);

                        final String[] playlist = new String[arrayPlayList.size()];
                        //ArrayList<String>からString[]に変換
                        for (int i = 0; i < playlist.length; i++) {
                            playlist[i] = arrayPlayList.get(i);
                            //ディレクトリを排除
                            playlist[i] = playlist[i].replace(thisActivity.getFilesDir().getPath() + "/", "");
                            //拡張子を排除
                            playlist[i] = playlist[i].replace(".m3u8", "");

                        }


                        builder.setTitle("プレイリスト").setItems(playlist, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // ダイアログを選択した時の処理.
                                // TODO: プレイリストに曲を追加する処理。 playlist[which] がクリックされたプレイリスト名@不安だけど一応完了

                                //選択されたプレイリストに保存されている曲を全部とってきます
                                m3u8SaveRead saveRead = new m3u8SaveRead(thisActivity);
                                ArrayList<String> edittingList = saveRead.openList(playlist[which]);

                                //選択されている項目を追加します

                                for (int i = 0; i < selectingList.Count(); i++) {
                                    edittingList.add(selectingList.GetRecord(i).FilePath);
                                }

                                //更新されたリストを保存する
                                saveRead.saveList(edittingList, playlist[which]);

                                // リストの選択を削除する
                                SelectedMusicList.deleteAllSelectedList();

                                //TODO:各フラグメントの複数選択の解除と、toolbarの復元をしないといけない
                                refreshAllPageFragments();
                                setDefaultOptionMenu();
                            }
                        });
                        builder.show();
                        break;
                    //プレイリストを新規作成するとき
                    case R.id.action_createNewPlaylist:
                        final EditText text = new EditText(thisActivity);
                        text.setInputType(InputType.TYPE_CLASS_TEXT);
                        new AlertDialog.Builder(thisActivity)
                                .setTitle("新しいプレイリスト名")
                                .setView(text)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //OKのときの処理

                                        final ArrayList<String> edittingList = new ArrayList<String>();

                                        //選択されている項目を追加します

                                        for (int i = 0; i < selectingList.Count(); i++) {
                                            edittingList.add(selectingList.GetRecord(i).FilePath);
                                        }

                                        final m3u8SaveRead saveRead = new m3u8SaveRead(thisActivity);
                                        switch (saveRead.saveList(edittingList, text.getText().toString(), false)) {
                                            //不正な名前だった場合
                                            case m3u8SaveRead.INVALID_NAME:
                                                TextView textView = new TextView(thisActivity);
                                                textView.setText("ファイル名に無効な文字列が含まれています");
                                                new AlertDialog.Builder(thisActivity)
                                                        .setTitle("保存に失敗しました")
                                                        .setView(textView)
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //OKが押されたときの処理。今のところ何もしない
                                                            }
                                                        })
                                                        .show();
                                                break;
                                            //既に同名のリストがあった場合
                                            case m3u8SaveRead.ALREADY_EXISTS:
                                                TextView textView1 = new TextView(thisActivity);
                                                textView1.setText("同じプレイリスト名が存在します。\n書き換えますか？");
                                                new AlertDialog.Builder(thisActivity)
                                                        .setTitle("確認")
                                                        .setView(textView1)
                                                        .setPositiveButton("保存", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //OKが押されたときの処理。保存する
                                                                saveRead.saveList(edittingList, text.getText().toString(), true);

                                                                refreshAllPageFragments();
                                                                setDefaultOptionMenu();

                                                            }
                                                        })
                                                        .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                //キャンセルのときの処理。何もしない
                                                            }
                                                        })
                                                        .show();
                                                break;
                                            default:
                                                //TODO:成功しているときはこっち。各フラグメントの複数選択の解除と、toolbarの復元をしないといけない
                                                refreshAllPageFragments();
                                                setDefaultOptionMenu();

                                        }
                                    }
                                })
                                .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //キャンセルのときの処理。なにもしない
                                    }
                                })
                                .show();
                        // リストの選択を解除する
                        SelectedMusicList.deleteAllSelectedList();

                }
                return false;
            }
        });
    }
    //ここまで
    public void refreshAllPageFragments(){
        if(artistListFragment2 != null) {
            artistListFragment2.refresh();
        }
        if(albumListFragment2 != null) {
            albumListFragment2.refresh();
        }
        if(songListFragment2 != null) {
            songListFragment2.refresh();
        }
        if(playListFragment2 != null) {
            playListFragment2.refresh();
            playListFragment2.onDatabaseUpdated();
        }
        if(playQueueFragment2 != null){
            playQueueFragment2.refresh();
        }

    }


	/**
	 * 以下、KeyDownに関する処理
	 */
	private OnKeyDownListener onKeyDownListener;

	public interface OnKeyDownListener {
		boolean onKeyDown(int keyCode, KeyEvent event);
	}

	public void setDefaultKeyDownAction() {
		setMethod_onKeyDown(new OnKeyDownListener() {
			@Override
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				switch (keyCode) {
					case KeyEvent.KEYCODE_BACK:
						// Androidの戻るボタンが押された時
						MusicPlayerMotionController motionController = musicPlayerFragment.getMotionController();
						switch (motionController.getScreenMode()) {
							case SETTINGS:
								motionController.changeScreenMode(MusicPlayerMotionController.ScreenMode.FULL);
								return false;
							case FULL:
								motionController.changeScreenMode(MusicPlayerMotionController.ScreenMode.MINI);
								return false;
						}
				}
				return true;
			}
		});
	}

	public void setMethod_onKeyDown(OnKeyDownListener listener) {
		this.onKeyDownListener = listener;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!onKeyDownListener.onKeyDown(keyCode, event)) return false;
		return super.onKeyDown(keyCode, event);
	}


	/**
	 * タブ用のアダプター
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		private static final int TOTAL_NUMBER_OF_PAGES = 5;

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			switch (position) {
				case 0:
					// アルバム一覧画面
					albumListFragment2 = new AlbumListFragment2();
                    return albumListFragment2;
				case 1:
					// アーティスト一覧画面
					artistListFragment2 = new ArtistListFragment2();
                    return artistListFragment2;
				case 2:
					// 曲一覧画面
					songListFragment2 = new SongListFragment2();
                    return songListFragment2;
				case 3:
					// プレイリスト一覧画面
					playListFragment2 = new PlayListFragment2();
                    return playListFragment2;
                case 4:
                    //再生キュー
                    playQueueFragment2 = new PlayQueueFragment2();
                    return playQueueFragment2;
			}
			return null;
		}

		@Override
		public int getCount() {
			return TOTAL_NUMBER_OF_PAGES;
		}
    }
}
