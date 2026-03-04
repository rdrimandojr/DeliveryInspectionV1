package ph.gov.philrice.rcepdeliveryinspection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import ph.gov.philrice.rcepdeliveryinspection.databinding.CustomDialogClassBinding;

public class MyCustomDialog {

    private final Context context;
    private AlertDialog dialog;

    public MyCustomDialog(Context context1) {
        this.context = context1;
    }

    public void showDialog(String title, String message,
                           String positiveButtonTitle, String negativeButtonTitle, String neutralButtonTitle,
                           boolean showPositive, boolean showNegative, boolean showNeutral, MyCustomDialogCallback callback) {
        // Inflate the layout using ViewBinding
        CustomDialogClassBinding binding = CustomDialogClassBinding.inflate(LayoutInflater.from(context));
        //title
        if (!title.isEmpty()) {
            binding.tvTitle.setText(title);
        } else {
            binding.tvTitle.setVisibility(View.GONE);
        }
        //message
        if (!message.isEmpty()) {
            binding.tvMessage.setText(message);
        } else {
            binding.tvMessage.setVisibility(View.GONE);
        }
        // Build the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(binding.getRoot()); // Set the root view from binding

        // Create the dialog
        dialog = builder.create();
        dialog.setCancelable(false);

        // Set up the Positive Button
        if (showPositive) {
            dialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonTitle, (DialogInterface.OnClickListener) null);
        }
        // Set up the Negative Button
        if (showNegative) {
            dialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonTitle, (DialogInterface.OnClickListener) null);
        }
        // Set up the Neutral Button
        if (showNeutral) {
            dialog.setButton(AlertDialog.BUTTON_NEUTRAL, neutralButtonTitle, (DialogInterface.OnClickListener) null);
        }

        // Show the dialog
        dialog.show();

        // Override the Positive Button click listener
        if (showPositive) {
            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setTextColor(Color.parseColor("#065D39")); // Example: Green color
            positiveButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onPositiveClick();
                }
                // Do not call dialog.dismiss() here to prevent automatic dismissal
            });
        }

        // Override the Negative Button click listener
        if (showNegative) {
            Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setTextColor(Color.parseColor("#B86618")); // Example: Orange color
            negativeButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onNegativeClick();
                }
                // Do not call dialog.dismiss() here to prevent automatic dismissal
            });
        }

        // Override the Neutral Button click listener
        if (showNeutral) {
            Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            neutralButton.setTextColor(Color.parseColor("#808080")); // Example: Gray color
            neutralButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onNeutralClick();
                }
                // Do not call dialog.dismiss() here to prevent automatic dismissal
            });
        }
    }

    // Method to dismiss the dialog manually
    public void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    // Callback interface for dialog button actions
    public interface MyCustomDialogCallback {
        void onPositiveClick();

        void onNegativeClick();

        void onNeutralClick();
    }

    /*HOW TO USE*/

    /*
    MyCustomDialog myCustomDialog;
    myCustomDialog = new MyCustomDialog(this);
        myCustomDialog.showDialog("title", "message", "positive", "negative", "neutral", true, true, true, new MyCustomDialog.MyCustomDialogCallback() {
        @Override
        public void onPositiveClick(*//*AlertDialog dialog*//*) {
            Toast.makeText(HomeActivity.this, "Positive", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onNegativeClick(*//*AlertDialog dialog*//*) {
            Toast.makeText(HomeActivity.this, "Negative", Toast.LENGTH_SHORT).show();
            myCustomDialog.dismissDialog();
        }

        @Override
        public void onNeutralClick(*//*AlertDialog dialog*//*) {
            Toast.makeText(HomeActivity.this, "Neutral", Toast.LENGTH_SHORT).show();
        }
    });
    */

}
