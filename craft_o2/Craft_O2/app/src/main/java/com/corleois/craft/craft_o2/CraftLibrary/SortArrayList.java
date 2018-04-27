package com.corleois.craft.craft_o2.CraftLibrary;

import java.util.ArrayList;

/**
 * Created by Mato on 2017/08/02.
 */

public class SortArrayList {
    /**
     *
     * @param arrayList ソートしたいArrayList<String>
     * @param sort 昇順or降順(ASC:DESC)
     * @return ソートしたArrayList
     */
    public static ArrayList<String> Sort(ArrayList<String> arrayList, SortType sort){
        boolean changed = true;
        if(sort == SortType.ASC) {
            while (changed) {
                changed = false;
                for (int j = 0; j < arrayList.size() - 1; j++) {
                    if (CodeCompare.StringA_minus_B_Special(arrayList.get(j),arrayList.get(j + 1)) > 0) {
                        String temp;
                        temp = arrayList.get(j);
                        arrayList.set(j, arrayList.get(j + 1));
                        arrayList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }else{//降順ソート
            while (changed) {
                changed = false;
                for (int j = 0; j < arrayList.size() - 1; j++) {
                    if (CodeCompare.StringA_minus_B_Special(arrayList.get(j),arrayList.get(j + 1)) < 0) {
                        String temp;
                        temp = arrayList.get(j);
                        arrayList.set(j, arrayList.get(j + 1));
                        arrayList.set(j + 1, temp);
                        changed = true;
                    }
                }
            }
        }
        return arrayList;
    }
}