package com.example.eposreceiptprintsample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class ShowMsg {

    public static void show(Context context, String msg) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setMessage(msg);

        alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                return ;
            }
        });

        alertDialog.create();

        alertDialog.show();
    }

}
