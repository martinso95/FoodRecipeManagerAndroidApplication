package martin.so.foodrecipemanager.ui.profile;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Utils;
import martin.so.foodrecipemanager.model.user.SignInActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
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

public class DeleteAccountActivity extends AppCompatActivity {

    private FirebaseUser currentUser;

    private TextInputEditText currentEmail;
    private TextInputEditText currentPassword;
    private TextView incorrectPasswordLabel;
    private Button deleteAccountButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_account);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        currentEmail = findViewById(R.id.textInputLayoutEditCurrentEmailDeleteAccount);
        String currentEmailValue = currentUser.getEmail() == null ? "" : currentUser.getEmail();
        currentEmail.setText(currentEmailValue);
        currentEmail.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        currentPassword = findViewById(R.id.textInputLayoutEditCurrentPasswordDeleteAccount);
        currentPassword.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        incorrectPasswordLabel = findViewById(R.id.textViewIncorrectPasswordDeleteAccount);

        deleteAccountButton = findViewById(R.id.buttonDeleteAccount);
        deleteAccountButton.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            showProgressBar();
            if (!fieldsValid()) {
                hideProgressBar();
                incorrectPasswordLabel.setVisibility(View.GONE);
            } else {
                if (currentUser != null) {
                    AuthCredential credential = EmailAuthProvider.getCredential(currentEmail.getText().toString(), currentPassword.getText().toString());
                    currentUser.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Test", "User is authenticated");
                                        currentUser.delete()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d("Test", "User account deleted.");
                                                            hideProgressBar();
                                                            InformationDialog informationDialog = new InformationDialog();
                                                            informationDialog.showDialog(DeleteAccountActivity.this, SignInActivity.class, true, getString(R.string.profile_delete_account_dialog_description));
                                                        } else {
                                                            Log.d("Test", "User account could not be deleted.");
                                                            InformationDialog informationDialog = new InformationDialog();
                                                            informationDialog.showDialog(DeleteAccountActivity.this, null, false, getString(R.string.profile_delete_account_failed_dialog_description));
                                                            hideProgressBar();
                                                        }
                                                    }
                                                });

                                    } else {
                                        Log.d("Test", "Credentials could not be authenticated.");
                                        hideProgressBar();
                                        incorrectPasswordLabel.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                }
            }
        });
        progressBar = findViewById(R.id.progressBarDeleteAccount);
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
        return fieldsValid;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        deleteAccountButton.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        deleteAccountButton.setVisibility(View.VISIBLE);
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
