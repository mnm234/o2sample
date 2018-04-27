package com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.corleois.craft.craft_o2.Activities.MusicSelectionActivity;
import com.corleois.craft.craft_o2.CraftLibrary.ImageCache;
import com.corleois.craft.craft_o2.Activities.Interfaces.AudioGroupRecyclerViewEventListener;
import com.corleois.craft.craft_o2.MetaData.AudioFileInformationAnalyzer;
import com.corleois.craft.craft_o2.MetaData.AudioGroupInformation;
import com.corleois.craft.craft_o2.MusicDB.MusicDBColumns;
import com.corleois.craft.craft_o2.PlaybackQueue.PlaybackQueue;
import com.corleois.craft.craft_o2.R;

import java.util.ArrayList;

/**
 * Created by StarLink on 2017/08/07.
 */

public class GroupListRecyclerViewAdapter extends RecyclerView.Adapter<GroupListRecyclerViewAdapter.ViewHolder>{


    private ArrayList<AudioGroupInformation> audioGroupInformations;
    private String identifier;
    private MusicSelectionActivity musicSelectionActivity;

    //呼び出し元でコードを走らせるためのリスナー
    private AudioGroupRecyclerViewEventListener listener;

    /**
     * 楽曲グループリストを受け取り、リサイクラービューが使えるようにします。
     * @param list 表示したい楽曲グループのリスト
     * @param identifier 楽曲グループリストの識別子。
     * @param musicSelectionActivity
     */
    public GroupListRecyclerViewAdapter(ArrayList<AudioGroupInformation> list,
                                        @NonNull String identifier,
                                        AudioGroupRecyclerViewEventListener listener,
                                        @NonNull MusicSelectionActivity musicSelectionActivity){
        this.audioGroupInformations = list;
        this.identifier = identifier;
        this.musicSelectionActivity = musicSelectionActivity;

        this.listener = listener;
    }

    /**
     * 一件あたりのレイアウトを作成します
     * @param parent
     * @param viewType
     * @return
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_music_recycler_selection, parent, false);

        return new ViewHolder(view);
    }

    /**
     * 一件あたりのレイアウトに、データを関連付けます
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        //グループ情報をもたせる
        holder.audioGroupInformation = audioGroupInformations.get(position);

        {//アルバムアートを設定する。デフォルトは人の画像
            ArrayList<String> imageList;

            //グループのリストが持ってる画像を解析するぜ！
            AudioFileInformationAnalyzer analyzer = new AudioFileInformationAnalyzer();
            analyzer.setAnalyzeData(holder.audioGroupInformation.getChildGroupAFI(true));
            imageList = analyzer.getAllIncludedDataFromColumnWithoutDuplicate(MusicDBColumns.ArtWorkPath);

            //最初に発見した利用可能な画像をアートワーク画像として利用する
            //もし、リストがnullまたはサイズ0ならFalse
            if(imageList != null && imageList.size() > 0){
                Bitmap bitmap = null;
                //有効な最初の画像が出るまで、後ろから探す。該当する、最新の画像が手に入るかもしれない
                for (int i = imageList.size() - 1; 0 <= i; i--) {

                    //画像パスがnullもしくは空文字ではなかったら
                    if(imageList.get(i) != null && !imageList.get(i).equals("")) {
                        bitmap = ImageCache.getImage(imageList.get(i));
                        //有効な画像を取ってこれたらtrue
                        if (bitmap != null) {
                            break;
                        }
                    }
                }
                //全件捜索終了
                //有効な画像があったらtrue
                if(bitmap != null){
//                    holder.albumArt.setImageBitmap(Bitmap.createScaledBitmap(bitmap, 512 ,512 ,true));
                    holder.albumArt.setImageBitmap(bitmap);
                }else {
                    //結局有効な画像がなかったとき
                    holder.albumArt.setImageBitmap(null);
                    holder.albumArt.setImageBitmap(ImageCache.getImage(R.drawable.disc, musicSelectionActivity));
                }
            }else{
                //こっちは有効な画像がなかったとき
                holder.albumArt.setImageBitmap(null);
                holder.albumArt.setImageBitmap(ImageCache.getImage(R.drawable.disc, musicSelectionActivity));
            }

        }

        //グループ名を設定する
        holder.groupTitleTextView.setText(holder.audioGroupInformation.getGroupName());

        //トータルの楽曲数を設定する
        holder.totalCountTextView.setText((String.valueOf( holder.audioGroupInformation.getChildGroupAFI(true).size()) + "曲"));

        //リスナー設定

        //クリックされたら
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(musicSelectionActivity, v.getParent().toString(), Toast.LENGTH_SHORT).show();
                if(listener != null){
                    //親のフラグメントのメソッドが呼ばれます
                    listener.GroupClick(holder.audioGroupInformation, position);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(listener != null){
                    //親フラグメントの長押しメソッドが呼ばれる
                    return listener.GroupLongClick(holder.audioGroupInformation, position);
                }
                return false;
            }
        });

        //カラーバー処理。SoundCloudに関しては判別方法未策定なので、左側は黒一色固定
        holder.imageView1.setImageResource(R.color.colorNormal);
        holder.imageView2.setImageResource(R.color.colorNormal);

        //再生中のファイルパスがあるかどうか探して、あれば青バー表示
        int size = holder.audioGroupInformation.getSongList().size();
        for (int i = 0; i < size; i++) {
            if(PlaybackQueue.GetNowPlaying() != null && PlaybackQueue.GetNowPlaying().FilePath.equals(holder.audioGroupInformation.getSongList().get(i).getFilePath())){
                holder.imageView2.setImageResource(R.color.colorNowPlaying);
                break;
            }
        }


        //バインドされた後のオリジナルイベントを呼ぶ
        if(listener != null){
            listener.GroupViewBinded(holder, holder.audioGroupInformation);
        }

    }

    @Override
    public int getItemCount() {
        return audioGroupInformations.size();
    }
    /**
     * ビューがリサイクルされるときに呼ばれる。
     * 大きな画像はここで解放しておくと良いぞと、オーバーライド元メソッドに書いてあった
     * @param holder
     */
    @Override
    public void onViewRecycled(GroupListRecyclerViewAdapter.ViewHolder holder){
        holder.imageView1.setImageDrawable(null);
        holder.imageView2.setImageDrawable(null);
        holder.albumArt.setImageDrawable(null);
    }
    public class ViewHolder extends RecyclerView.ViewHolder{

        public AudioGroupInformation audioGroupInformation; //このビューホルダーと対応したグループ情報

        public ImageView imageView1;    //ステータスカラー1
        public ImageView imageView2;    //ステータスカラー2
        public ImageView albumArt;      //アルバムアート

        public TextView groupTitleTextView; //グループ名
        public TextView totalCountTextView; //トータルの楽曲数
        public boolean selected;


        public ViewHolder(View itemView) {
            super(itemView);

            //ステータスカラーのビューを取得する
            imageView1 = (ImageView) itemView.findViewById(R.id.statusColor1);
            imageView2 = (ImageView) itemView.findViewById(R.id.statusColor2);
            //アルバムアートのビュー取得
            albumArt = (ImageView) itemView.findViewById(R.id.albumArt1);
            //テキストビューのビュー取得
            groupTitleTextView = (TextView) itemView.findViewById(R.id.group_title);
            totalCountTextView = (TextView) itemView.findViewById(R.id.track_count);
        }
    }
}
