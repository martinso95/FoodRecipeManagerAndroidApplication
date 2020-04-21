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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ChangeDisplayNameActivity extends AppCompatActivity {

    private FirebaseUser currentUser;

    private TextInputEditText newDisplayName;
    private Button saveChanges;
    private ProgressBar progressBar;
    private TextView nameChangedLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_display_name);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        newDisplayName = findViewById(R.id.textInputLayoutEditNewDisplayNameChangeDisplayName);
        newDisplayName.setText(currentUser.getDisplayName());

        saveChanges = findViewById(R.id.buttonSaveChangeDisplayName);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showProgressBar();
                if (TextUtils.isEmpty(newDisplayName.getText().toString()) || (newDisplayName.length() >= 16)) {
                    showSaveButton();
                    newDisplayName.setError("Name too long");
                } else {
                    if (currentUser != null) {
                        UserProfileChangeRequest profileDisplayNameUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(newDisplayName.getText().toString())
                                .build();
                        currentUser.updateProfile(profileDisplayNameUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Log.d("Test", "User profile display name updated.");
                                            showChangedLabel();
                                        } else {
                                            Log.d("Test", "User profile display name update failed.");
                                            showSaveButton();
                                            InformationDialog informationDialog = new InformationDialog();
                                            informationDialog.showDialog(ChangeDisplayNameActivity.this, null, false, getString(R.string.profile_change_display_name_failed_dialog_description));
                                        }
                                    }
                                });
                    }
                }
            }
        });
        progressBar = findViewById(R.id.progressBarSaveChangeDisplayName);
        nameChangedLabel = findViewById(R.id.textViewChangedLabelChangeDisplayName);
    }

    private void showSaveButton() {
        saveChanges.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        nameChangedLabel.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        nameChangedLabel.setVisibility(View.GONE);
    }

    private void showChangedLabel() {
        saveChanges.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        nameChangedLabel.setVisibility(View.VISIBLE);
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
