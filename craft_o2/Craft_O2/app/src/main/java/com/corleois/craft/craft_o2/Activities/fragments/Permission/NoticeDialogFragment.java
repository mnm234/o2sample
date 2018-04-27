package com.corleois.craft.craft_o2.Activities.fragments.Permission;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Created by corleois on 2017/07/05.
 */

public class NoticeDialogFragment extends DialogFragment {

    public interface NoticeDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    NoticeDialogListener mListener;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (NoticeDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Build the dialog and set up the button click handlers
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("ファイル読み取り許可のお願い")
                .setMessage("O2ではストレージのファイルを読み込む必要があります\n次の権限を許可して下さい")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    //ダイアログOKクリック時
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //リクエストするやで！！
                        mListener.onDialogPositiveClick(NoticeDialogFragment.this);
                    }
                }).create();
        return builder.create();
    }
}
