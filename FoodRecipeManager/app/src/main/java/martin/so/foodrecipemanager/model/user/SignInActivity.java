package martin.so.foodrecipemanager.model.user;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import martin.so.foodrecipemanager.MainActivity;
import martin.so.foodrecipemanager.R;
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

/**
 * Sign in portal for users.
 * Users have the possibility to: sign in, sign up, and reset password.
 */
public class SignInActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private Button signInButton;
    private TextView incorrectPasswordLabel;
    private TextView signUpTextButton;
    private TextView forgotPasswordTextButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme_NoActionBar);
        firebaseAuth = FirebaseAuth.getInstance();

        checkIfSignedIn();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailInput = findViewById(R.id.textInputLayoutEditEmailSignIn);
        passwordInput = findViewById(R.id.textInputLayoutEditPasswordSignIn);
        incorrectPasswordLabel = findViewById(R.id.textViewIncorrectPasswordSignIn);

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

        signInButton = findViewById(R.id.buttonSignInSignIn);
        signInButton.setOnClickListener(v -> {
            Utils.hideKeyboard(this);
            Log.d("Test", "Start signIp");
            if (!fieldsValid()) {
                Toast.makeText(SignInActivity.this, "Incorrect user or password",
                        Toast.LENGTH_SHORT).show();
            } else {
                showProgressBar();
                incorrectPasswordLabel.setVisibility(View.GONE);
                String email = emailInput.getText().toString();
                String password = passwordInput.getText().toString();
                signIn(email, password);
            }
        });

        progressBar = findViewById(R.id.progressBarSignInProgressSignIn);

        signUpTextButton = findViewById(R.id.textViewSignUpSignIn);
        signUpTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUpActivity = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(signUpActivity);
                overridePendingTransition(R.anim.slide_in_right_activity, R.anim.slide_out_left_activity);
            }
        });

        forgotPasswordTextButton = findViewById(R.id.textViewForgotPasswordSignIn);
        forgotPasswordTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forgotPasswordActivity = new Intent(SignInActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPasswordActivity);
            }
        });
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent mainActivity = new Intent(SignInActivity.this, MainActivity.class);
                            startActivity(mainActivity);
                            finish();
                        } else {
                            hideProgressBar();
                            incorrectPasswordLabel.setVisibility(View.VISIBLE);
                            Toast.makeText(SignInActivity.this, "Sign In Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkIfSignedIn() {
        if (firebaseAuth.getCurrentUser() != null) {
            Intent mainActivity = new Intent(SignInActivity.this, MainActivity.class);
            startActivity(mainActivity);
        }
    }

    private boolean fieldsValid() {
        boolean fieldsValid = true;
        if (TextUtils.isEmpty(emailInput.getText().toString())) {
            emailInput.setError("Required");
            fieldsValid = false;
        } else {
            emailInput.setError(null);
        }
        if (TextUtils.isEmpty(passwordInput.getText().toString())) {
            passwordInput.setError("Required");
            fieldsValid = false;
        } else {
            passwordInput.setError(null);
        }
        return fieldsValid;
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
        signInButton.setVisibility(View.GONE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
        signInButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

}
