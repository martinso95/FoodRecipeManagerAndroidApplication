package martin.so.foodrecipemanager.model;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import martin.so.foodrecipemanager.R;

/**
 * Class for showing a dynamic information dialog, based on input.
 * The dialog shows information and the user can only accept.
 * It is possible to direct the user to a new activity.
 */
public class InformationDialog {

    /**
     * Shows a dialog in the activity in order to inform the user.
     * With possibility to redirect the user to a new activity.
     *
     * @param currentActivity     The current activity where the dialog should be shown.
     * @param reDirectionActivity The new activity class where the user should be redirected to.
     * @param shouldReDirect      boolean for determining whether there will be a redirection.
     * @param message             The information showed to the user.
     */
    public void showDialog(Activity currentActivity, Class reDirectionActivity, boolean shouldReDirect, String message) {
        final Dialog dialog = new Dialog(currentActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.dialog_information);

        TextView text = dialog.findViewById(R.id.textViewDescriptionInformationDialog);
        text.setText(message);

        Button dialogButton = dialog.findViewById(R.id.buttonOkInformationDialog);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (shouldReDirect) {
                    Intent returnActivity = new Intent(currentActivity, reDirectionActivity);
                    dialog.dismiss();
                    currentActivity.startActivity(returnActivity);
                    currentActivity.finish();
                } else {
                    dialog.dismiss();
                }
            }
        });
        dialog.show();
    }
}