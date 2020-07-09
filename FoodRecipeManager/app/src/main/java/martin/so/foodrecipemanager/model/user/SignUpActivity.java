package martin.so.foodrecipemanager.model.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.InformationDialog;
import martin.so.foodrecipemanager.model.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Activity for users to sign up.
 * Users have to enter a name, email, and password.
 */
public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button signUpButton;
    private TextView alreadyRegistered;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        firebaseAuth = FirebaseAuth.getInstance();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        nameInput = findViewById(R.id.textInputLayoutEditNameEditSignUp);
        emailInput = findViewById(R.id.textInputLayoutEditEmailEditSignUp);
        passwordInput = findViewById(R.id.textInputLayoutEditPasswordEditSignUp);

        nameInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        emailInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        passwordInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean handled = false;
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                handled = true;
            }
            return handled;
        });

        signUpButton = findViewById(R.id.buttonSignUp);
        signUpButton.setOnClickListener(view -> {
            Utils.hideKeyboard(this);
            Log.d("Test", "Start signUp");
            if (!fieldsValid()) {
                Toast.makeText(SignUpActivity.this, "Enter correct values",
                        Toast.LENGTH_SHORT).show();
            } else {
                showProgressBar();
                String name = nameInput.getText().toString();
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                signUp(name, email, password);
            }
        });

        progressBar = findViewById(R.id.progressBarSignUpProgressSignUp);

        alreadyRegistered = findViewById(R.id.textViewAlreadySignedUpSignUp);
        alreadyRegistered.setOnClickListener(view -> {
            Intent loginActivity = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(loginActivity);
            finish();
            overridePendingTransition(R.anim.slide_in_left_activity, R.anim.slide_out_right_activity);
        });
    }

    private void signUp(String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                user.sendEmailVerification()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Log.d("Test", "Email verification sent.");
                                                }
                                            }
                                        });
                                UserProfileChangeRequest profileDisplayNameUpdate = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(name)
                                        .build();
                                user.updateProfile(profileDisplayNameUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                firebaseAuth.signOut();
                                                if (task.isSuccessful()) {
                                                    hideProgressBar();
                                                    InformationDialog informationDialog = new InformationDialog();
                                                    informationDialog.showDialog(SignUpActivity.this, SignInActivity.class, true, getString(R.string.user_sign_up_success_dialog_description));
                                                } else {
                                                    hideProgressBar();
                                                    InformationDialog informationDialog = new InformationDialog();
                                                    informationDialog.showDialog(SignUpActivity.this, SignInActivity.class, true, getString(R.string.user_sign_up_success_name_set_failed_dialog_description));
                                                }
                                            }
                                        });
                            }
                        } else {
                            hideProgressBar();
                            InformationDialog informationDialog = new InformationDialog();
                            informationDialog.showDialog(SignUpActivity.this, null, false, getString(R.string.user_sign_up_failed_dialog_description));
                        }
                    }
                });
    }

    private boolean fieldsValid() {
        boolean fieldsValid = true;
        String name = nameInput.getText().toString();
        if (TextUtils.isEmpty(name) || (name.length() >= 16)) {
            nameInput.setError("Enter a shorter name");
            fieldsValid = false;
        } else {
            nameInput.setError(null);
        }
        String email = emailInput.getText().toString();
        if (TextUtils.isEmpty(email) || !email.contains("@")) {
            emailInput.setError("Enter a valid email");
            fieldsValid = false;
        } else {
            emailInput.setError(null);
        }
        String password = passwordInput.getText().toString();
        if (TextUtils.isEmpty(password) || (password.length() < 6)) {
            passwordInput.setError("Enter a password longer than 6 characters");
            fieldsValid = false;
        } else {
            passwordInput.setError(null);
        }
        return fieldsValid;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        signUpButton.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        signUpButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }
}
