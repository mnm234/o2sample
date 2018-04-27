package com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * Created by StarLink on 2017/08/06.
 */

/**
 * RecyclerViewのアニメーション担当クラス
 */
public class SongListAnimation extends ItemTouchHelper.SimpleCallback {

    public SongListAnimation(int dragDirs, int swipeDirs){
        super(dragDirs, swipeDirs);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

    }


}
