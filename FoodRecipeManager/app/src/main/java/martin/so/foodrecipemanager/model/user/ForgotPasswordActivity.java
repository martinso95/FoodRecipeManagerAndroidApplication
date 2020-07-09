package martin.so.foodrecipemanager.model.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Utils;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;


/**
 * Activity for handling forgotten user passwords.
 * Users can request a password reset email so that they can reset their password via the email sent.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText email;
    private Button send;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        email = findViewById(R.id.textInputLayoutEditEmailForgotPassword);

        send = findViewById(R.id.buttonSendForgotPassword);
        send.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            showProgressBar();
            String emailValue = email.getText().toString();
            if (TextUtils.isEmpty(emailValue) || !emailValue.contains("@")) {
                hideProgressBar();
                email.setError("Enter a valid email");
            } else {
                FirebaseAuth.getInstance().sendPasswordResetEmail(emailValue)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Log.d("Test", "Password reset email sent.");
                                    hideProgressBar();
                                    InformationDialog informationDialog = new InformationDialog();
                                    informationDialog.showDialog(ForgotPasswordActivity.this, SignInActivity.class, true, getString(R.string.forgot_password_email_sent_dialog_description));
                                } else {
                                    hideProgressBar();
                                    InformationDialog informationDialog = new InformationDialog();
                                    informationDialog.showDialog(ForgotPasswordActivity.this, null, false, getString(R.string.forgot_password_email_sent_dialog_failed_description));
                                    Log.d("Test", "Password reset email could not be sent.");
                                    Toast.makeText(ForgotPasswordActivity.this, "Password reset email could not be sent.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        progressBar = findViewById(R.id.progressBarSendForgotPassword);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        send.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        send.setVisibility(View.VISIBLE);
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
