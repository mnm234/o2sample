package com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.Activities.TagSettingActivity;
import com.corleois.craft.craft_o2.Activities.fragments.MusicPlayerFragment;
import com.corleois.craft.craft_o2.Control.Controller;
import com.corleois.craft.craft_o2.CraftLibrary.ImageCache;
import com.corleois.craft.craft_o2.CraftLibrary.StringArrayListConverter;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformation;
import com.corleois.craft.craft_o2.PlaybackQueue.AudioFileQueueList;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;
import com.corleois.craft.craft_o2.R;
import com.corleois.craft.craft_o2.SelectedMusic.SelectedMusicList;

import java.util.ArrayList;

/**
 * Created by StarLink on 2017/08/02.
 */

public class SongListRecyclerViewAdapter extends RecyclerView.Adapter<SongListRecyclerViewAdapter.ViewHolder>{

    private ArrayList<AudioFileInformation> songList;
    private String identifier;
    private MusicSelectionActivity musicSelectionActivity;
    private ArrayList<Boolean> selected;
    private boolean canTrackMove;

    //このリサイクラービューを呼び出したフラグメント
    private Fragment userFlagment;

    /**
     * 楽曲リストを受け取り、リサイクラービューが使えるようにします。
     * @param list　表示したい楽曲リスト
     * @param identifier 楽曲リストの識別子。ファイルの選択時のリストの保持に必要です。
     * @param activity 大元のMusicSelectionActivityのオブジェクトです
     */
    public SongListRecyclerViewAdapter(ArrayList<AudioFileInformation> list,
                                       @NonNull String identifier,
                                       boolean canTrackMove,
                                       @NonNull MusicSelectionActivity activity,
                                       @NonNull Fragment yourFragment){
        this.identifier = identifier;
        this.musicSelectionActivity = activity;
        this.songList = list;
        this.canTrackMove = canTrackMove;
        this.selected = new ArrayList<>();
        this.userFlagment = yourFragment;
        for (int i = 0; i < songList.size(); i++) {
            selected.add(SelectedMusicList.contains(identifier, songList.get(i).getFilePath()));
        }
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //一件あたりのレイアウトを生成する
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_music_recycler_selection, parent, false);

        return new ViewHolder(view);
    }

    /**
     * 画面に表示するデータ一件に対して、データを設定するメソッド。
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //ここで、一件あたりのデータをどのデータと関連付けるかを設定する
        AudioFileInformation record = songList.get(position);
        holder.position = position;

        //リスナーをここに記述する


        /**
         * -----------------------------------
         */

        //状態を示すインジケータを実装。今のところSoundcloudに関しては記述していない。
        //その代わり、再生中のヤツは青系に
        holder.status1.setImageResource((record.getNotExistFlag() == 0) ? R.color.colorNormal : R.color.colorNormalDeadItem);

        //再生中のファイルパスが一致したら
        if(PlaybackQueue.GetNowPlaying() != null && PlaybackQueue.GetNowPlaying().FilePath.equals(record.getFilePath())) {
            holder.status1.setImageResource(R.color.colorNowPlaying);
        }

        //アルバムアートを設定する。デフォルトは人の画像。
        // 画像が設定されていれば最初の画像。ただし、アルバムアートの画像は表示する大きさにトリミングしてメモリを節約する
        {
            String imgPath;

            //アートワークパスが空でないならtrue
            if(!record.getArtWorkPath().isEmpty() && !record.getArtWorkPath().get(0).equals("")){
                imgPath = record.getArtWorkPath().get(0);
                Bitmap bitmap = ImageCache.getImage(imgPath);
//            bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);
                holder.albumArt.setImageBitmap(bitmap);
            }else {
                holder.albumArt.setImageBitmap(null);
                holder.albumArt.setImageBitmap(ImageCache.getImage(R.drawable.artist_dark, musicSelectionActivity));
            }

        }

        //タイトルを設定する
        {
            String title;
            try {
                title = StringArrayListConverter.EncodeToString(record.getTitle(), " / ");
            } catch (Exception e) {
                title = record.getFilePath();
            }
            if(title.length() == 0){
                title = record.getFilePath();
            }
            holder.title.setText(title);
        }

        //アーティストを表示する
        {
            String artist;
            try{
                artist = StringArrayListConverter.EncodeToString(record.getArtist(), " / ");
            }catch (Exception e){
                artist = "";
            }
            holder.artist.setText(artist);
        }

        //他に表示する項目があったらここで設定する
        holder.fileInformation = record;
        selected.set(holder.position, SelectedMusicList.contains(identifier, holder.fileInformation.getFilePath()));


        //色は、選択されていなかったら白、そうでなかったら、それなりの色
        holder.thisView.setBackgroundColor(!selected.get(holder.position) ? Color.WHITE : Color.GREEN);

        //楽曲の移動が要求されていなかったら、≡の代わりにタグを表示
        if(!canTrackMove){
            holder.upDown.setImageBitmap(ImageCache.getImage(R.drawable.tag, musicSelectionActivity));
        }else{
            holder.upDown.setImageBitmap(ImageCache.getImage(R.drawable.hamburger, musicSelectionActivity));
        }


        /**
         * ちょっとすごいかもしれないけど、ここでOnLongClickListenerを定義する。
         */
        class OurOnLongClickListener implements View.OnLongClickListener{

            @Override
            public boolean onLongClick(View v) {
                if(v == holder.thisView.findViewById(R.id.updown)){

                    return false;
                }
                //Toast.makeText(musicSelectionActivity, v.toString(),Toast.LENGTH_SHORT).show();

                //選択リストに楽曲を登録するしないをトグルして、それぞれのパターンの処理を行う
                selected.set(holder.position, !selected.get(holder.position));

                //もし、登録処理が必要ならtrue
                if(selected.get(holder.position)){
                    holder.thisView.setBackgroundColor(Color.GREEN);   //←とりあえず緑にしてるけど変更可能

                    //選択リスト。なかったら作るしあったらそのまま使いまわす
                    SelectedMusicList.createNewSelectedList(identifier);
                    SelectedMusicList.addSelectedfFile(identifier, songList.get(holder.position).getFilePath());

                }else{//削除処理が必要ならfalse
                    holder.thisView.setBackgroundColor(Color.WHITE);
                    SelectedMusicList.removeSelectedFile(identifier, songList.get(holder.position).getFilePath());
                }

                //オリジナルのアレを呼ぶ
                musicSelectionActivity.notifyToSelectedItemChanged();

                return true;
            }
        }
        /**
         * 定義終了
         */

        //タップ＆ホールドされたら
        holder.itemView.setOnLongClickListener(new OurOnLongClickListener());
        holder.upDown.setOnLongClickListener(new OurOnLongClickListener());


        //アイテムがクリックされたとき
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //複数選択中でないかチェック
                //もし、複数選択中ならば再生ではなく選択処理を行う
                {
                    AudioFileQueueList list = SelectedMusicList.getSelectedList(identifier);
                    if (list != null && list.Count() != 0){
                        //長押しした時のイベントを呼んじゃうもんね
                        v.performLongClick();
                        return;
                    }
                }
                //存在しない楽曲でないかチェック
                //存在しなかったらtrue
                if(holder.fileInformation.getNotExistFlag() == 1){
                    AlertDialog.Builder dialog = new AlertDialog.Builder(musicSelectionActivity);
                    dialog.setTitle("再生できません");
                    TextView view = new TextView(musicSelectionActivity);
                    view.setText("この音声ファイルを監視ディレクトリ内に見つけることができませんでした。\n 最後に確認された場所は\n" + holder.fileInformation.getFilePath() +"\nです。");
                    view.setPadding(4,4,4,4);
                    dialog.setView(view);
                    dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //何もしない
                        }
                    }).show();
                    return;
                }


                //キューをクリア
                PlaybackQueue.Clear();

                //全曲をキューに追加
                ArrayList<String> list = new ArrayList<>();
                int size = songList.size();
                for (int i = 0; i < size; i++) {
                    list.add(songList.get(i).getFilePath());
                }
                //まるごとポイー
                PlaybackQueue.AddList(list);

                MusicPlayerFragment musicPlayerFragment = (MusicPlayerFragment) musicSelectionActivity.getSupportFragmentManager().findFragmentById(R.id.playerFragment);
                if (songList.get(position).getFilePath() == null) {
                    Toast.makeText(musicSelectionActivity, "songList.get(position).getFilePath()はnull", Toast.LENGTH_SHORT).show();
                }
                musicPlayerFragment.skipToFilePath = songList.get(position).getFilePath();

                ImageButton buttonNext = (ImageButton) musicSelectionActivity.findViewById(R.id.nextButton);
                buttonNext.callOnClick();

                //最後に、再生キューの画面側リストを更新
                musicSelectionActivity.queueFragmentUpdate();

            }
        });

        //左側の画像がクリックされたき
        holder.upDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //表示されている画像がTagだったらtrue
                if(((BitmapDrawable) holder.upDown.getDrawable()).getBitmap().equals(ImageCache.getImage(R.drawable.tag, musicSelectionActivity))){

                    //タグ編集画面へ飛ぶよ
                    Intent intent = new Intent(musicSelectionActivity, TagSettingActivity.class);
                    intent.putExtra("filePath", holder.fileInformation.getFilePath());
                    musicSelectionActivity.startActivity(intent);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    /**
     * ビューがリサイクルされるときに呼ばれる。
     * 大きな画像はここで解放しておくと良いぞと、オーバーライド元メソッドに書いてあった
     * @param holder
     */
    @Override
    public void onViewRecycled(SongListRecyclerViewAdapter.ViewHolder holder){
        holder.upDown.setImageDrawable(null);
        holder.albumArt.setImageDrawable(null);
        holder.status1.setImageDrawable(null);

    }


    /**
     * 一件あたりのデータを保持するクラス
     */
    public static class ViewHolder extends RecyclerView.ViewHolder{


        public ImageView status1;
        public ImageView albumArt;
        public TextView title;
        public TextView artist;
        public View thisView;
        public ImageView upDown;

        //リスナーとか色々処理する上でpositionを処理できないので、view側で保持しておく
        public int position;


        public AudioFileInformation fileInformation;

        public ViewHolder(final View itemView) {
            super(itemView);

            status1 = (ImageView) itemView.findViewById(R.id.statusColor1);
            albumArt = (ImageView) itemView.findViewById(R.id.albumArt);
            title = (TextView) itemView.findViewById(R.id.title);
            artist = (TextView) itemView.findViewById(R.id.artist);
            thisView = itemView.findViewById(R.id.item_music_selection);
            upDown = (ImageView) itemView.findViewById(R.id.updown);

        }

    }

}
