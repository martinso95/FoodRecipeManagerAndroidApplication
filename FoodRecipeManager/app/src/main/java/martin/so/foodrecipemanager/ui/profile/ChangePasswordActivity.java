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

public class ChangePasswordActivity extends AppCompatActivity {

    private FirebaseUser currentUser;

    private TextInputEditText currentEmail;
    private TextInputEditText currentPassword;
    private TextInputEditText newPassword;
    private TextView incorrectPasswordLabel;
    private Button saveChanges;
    private ProgressBar progressBar;
    private TextView passwordChangedLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentEmail = findViewById(R.id.textInputLayoutEditCurrentEmailChangePassword);
        String currentEmailValue = currentUser.getEmail() == null ? "" : currentUser.getEmail();
        currentEmail.setText(currentEmailValue);

        currentPassword = findViewById(R.id.textInputLayoutEditCurrentPasswordChangePassword);

        newPassword = findViewById(R.id.textInputLayoutEditNewPasswordChangePassword);

        incorrectPasswordLabel = findViewById(R.id.textViewIncorrectPasswordChangePassword);

        saveChanges = findViewById(R.id.buttonSaveChangeChangePassword);
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
                                            currentUser.updatePassword(newPassword.getText().toString())
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Log.d("Test", "User profile password updated.");
                                                                showChangedLabel();
                                                            } else {
                                                                Log.d("Test", "User profile password update failed.");
                                                                showSaveButton();
                                                                InformationDialog informationDialog = new InformationDialog();
                                                                informationDialog.showDialog(ChangePasswordActivity.this, null, false, getString(R.string.profile_change_password_failed_dialog_description));
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
        progressBar = findViewById(R.id.progressBarSaveChangePassword);
        passwordChangedLabel = findViewById(R.id.textViewChangedLabelChangePassword);
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
        String newPasswordValue = newPassword.getText().toString();
        if (TextUtils.isEmpty(newPasswordValue) || (newPasswordValue.length() < 6)) {
            newPassword.setError("Enter a valid password");
            fieldsValid = false;
        } else {
            newPassword.setError(null);
        }
        return fieldsValid;
    }

    private void showSaveButton() {
        saveChanges.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        passwordChangedLabel.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        passwordChangedLabel.setVisibility(View.GONE);
    }

    private void showChangedLabel() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        passwordChangedLabel.setVisibility(View.VISIBLE);
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
