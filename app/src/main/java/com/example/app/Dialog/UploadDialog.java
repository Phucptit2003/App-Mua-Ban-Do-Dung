package com.example.app.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Window;

import com.example.app.R;

public class UploadDialog {
    private Dialog dialog;

    public UploadDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_upload);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public void show() {
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public void setContentView(int animation) {
                dialog.setContentView(animation);
        }
}
