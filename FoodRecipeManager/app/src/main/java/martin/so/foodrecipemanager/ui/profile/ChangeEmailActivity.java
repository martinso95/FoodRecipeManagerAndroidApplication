package martin.so.foodrecipemanager.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.InformationDialog;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ChangeEmailActivity extends AppCompatActivity {

    private FirebaseUser currentUser;

    private TextInputEditText currentEmail;
    private TextInputEditText currentPassword;
    private TextInputEditText newEmail;
    private TextView incorrectPasswordLabel;
    private Button saveChanges;
    private ProgressBar progressBar;
    private TextView emailChangedLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_email);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentEmail = findViewById(R.id.textInputLayoutEditCurrentEmailChangeEmail);
        currentEmail.setText(currentUser.getEmail());

        currentPassword = findViewById(R.id.textInputLayoutEditCurrentPasswordChangeEmail);

        newEmail = findViewById(R.id.textInputLayoutEditNewEmailChangeEmail);

        incorrectPasswordLabel = findViewById(R.id.textViewIncorrectPasswordChangeEmail);

        saveChanges = findViewById(R.id.buttonSaveChangeChangeEmail);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                incorrectPasswordLabel.setVisibility(View.GONE);
                if (!fieldsValid()) {
                    showSaveButton();
                } else {
                    if (currentUser != null) {
                        AuthCredential credential = EmailAuthProvider.getCredential(currentEmail.getText().toString(), currentPassword.getText().toString());
                        currentUser.reauthenticate(credential)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Test", "User is authenticated");
                                            currentUser.updateEmail(newEmail.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("Test", "User profile email updated.");
                                                                sendEmailVerification();
                                                                showChangedLabel();
                                                                InformationDialog informationDialog = new InformationDialog();
                                                                informationDialog.showDialog(ChangeEmailActivity.this, null, false, getString(R.string.profile_change_email_success_dialog_description));
                                                            } else {
                                                                Log.d("Test", "User profile email update failed.");
                                                                showSaveButton();
                                                                InformationDialog informationDialog = new InformationDialog();
                                                                informationDialog.showDialog(ChangeEmailActivity.this, null, false, getString(R.string.profile_change_email_failed_dialog_description));
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Log.d("Test", "Credentials could not be authenticated.");
                                            showSaveButton();
                                            incorrectPasswordLabel.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    }
                }
            }
        });
        progressBar = findViewById(R.id.progressBarSaveChangeEmail);
        emailChangedLabel = findViewById(R.id.textViewChangedLabelChangeEmail);
    }

    /**
     * Sends email to the user to verify their email used.
     */
    private void sendEmailVerification() {
        if (currentUser != null) {
            currentUser.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Test", "Email verification sent.");
                            }
                        }
                    });
        }
    }

    private boolean fieldsValid() {
        boolean fieldsValid = true;
        String currentEmailValue = currentEmail.getText().toString();
        if (!currentEmailValue.equals(currentUser.getEmail())) {
            currentEmail.setError("Your entered current email is incorrect");
            fieldsValid = false;
        } else {
            currentEmail.setError(null);
        }
        String currentPasswordValue = currentPassword.getText().toString();
        if (TextUtils.isEmpty(currentPasswordValue)) {
            currentPassword.setError("Password is empty");
            fieldsValid = false;
        } else {
            currentPassword.setError(null);
        }
        String newEmailValue = newEmail.getText().toString();
        if (TextUtils.isEmpty(newEmailValue) || !newEmailValue.contains("@")) {
            newEmail.setError("Enter a valid email");
            fieldsValid = false;
        } else {
            newEmail.setError(null);
        }
        return fieldsValid;
    }

    private void showSaveButton() {
        saveChanges.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        emailChangedLabel.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        emailChangedLabel.setVisibility(View.GONE);
    }

    private void showChangedLabel() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        emailChangedLabel.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
