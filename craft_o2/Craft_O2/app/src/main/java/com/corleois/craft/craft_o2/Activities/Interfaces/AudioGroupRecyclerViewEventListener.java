package com.corleois.craft.craft_o2.Activities.Interfaces;

import com.corleois.craft.craft_o2.MetaData.AudioGroupInformation;
import com.corleois.craft.craft_o2.Activities.fragments.RecyclerViewClass.GroupListRecyclerViewAdapter;

/**
 * Created by StarLink on 2017/08/08.
 */

public interface AudioGroupRecyclerViewEventListener {

    void GroupClick(AudioGroupInformation groupInformation,int index);
    boolean GroupLongClick(AudioGroupInformation groupInformation,int index);
    void GroupViewBinded(GroupListRecyclerViewAdapter.ViewHolder viewHolder, AudioGroupInformation groupInformation);
}
