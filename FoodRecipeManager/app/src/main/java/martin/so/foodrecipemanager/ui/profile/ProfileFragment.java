package martin.so.foodrecipemanager.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import martin.so.foodrecipemanager.R;
import martin.so.foodrecipemanager.model.user.SignInActivity;

public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;

    private View view = null;
    private TextView displayName;
    private TextView email;
    private TextView emailNotVerified;
    private TextView password;
    private TextView deleteAccount;
    private RelativeLayout reSendEmailVerificationRelativeLayout;
    private Button reSendEmailVerificationButton;
    private TextView emailVerificationSentLabel;
    private Button signOutButton;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        if (view == null) {
            firebaseAuth = FirebaseAuth.getInstance();
            currentUser = firebaseAuth.getCurrentUser();
            view = inflater.inflate(R.layout.fragment_profile, container, false);

            displayName = view.findViewById(R.id.textViewDisplayNameProfile);
            displayName.setText(currentUser.getDisplayName());
            displayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent changeDisplayNameActivity = new Intent(getActivity(), ChangeDisplayNameActivity.class);
                    startActivity(changeDisplayNameActivity);
                }
            });

            email = view.findViewById(R.id.textViewEmailProfile);
            email.setText(currentUser.getEmail());
            email.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent changeEmailActivity = new Intent(getActivity(), ChangeEmailActivity.class);
                    startActivity(changeEmailActivity);
                }
            });

            password = view.findViewById(R.id.textViewPasswordProfile);
            password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent changePasswordActivity = new Intent(getActivity(), ChangePasswordActivity.class);
                    startActivity(changePasswordActivity);
                }
            });

            deleteAccount = view.findViewById(R.id.textViewDeleteAccountProfile);
            deleteAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent deleteAccountActivity = new Intent(getActivity(), DeleteAccountActivity.class);
                    startActivity(deleteAccountActivity);
                }
            });

            emailNotVerified = view.findViewById(R.id.textViewEmailNotVerifiedLabelProfile);
            reSendEmailVerificationRelativeLayout = view.findViewById(R.id.relativeLayoutReSendEmailVerificationProfile);
            reSendEmailVerificationButton = view.findViewById(R.id.buttonReSendEmailVerificationProfile);
            emailVerificationSentLabel = view.findViewById(R.id.textViewReSendEmailVerificationProfile);
            reSendEmailVerificationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sendEmailVerification();
                    reSendEmailVerificationButton.setVisibility(View.GONE);
                    emailVerificationSentLabel.setVisibility(View.VISIBLE);
                    Log.d("Test", "Email verification sent.");
                }
            });

            if (!currentUser.isEmailVerified()) {
                emailNotVerified.setVisibility(View.VISIBLE);
                reSendEmailVerificationRelativeLayout.setVisibility(View.VISIBLE);
            }

            signOutButton = view.findViewById(R.id.buttonSignOutProfile);
            signOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    firebaseAuth.signOut();
                    Intent signInActivity = new Intent(getActivity(), SignInActivity.class);
                    startActivity(signInActivity);
                    getActivity().finish();
                }
            });
        }
        return view;
    }

    /**
     * Change the profile photo of the user.
     *
     * @param photoUri the new profile photo uri of the user.
     */
    private void updateUserProfilePhoto(String photoUri) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null && !photoUri.isEmpty()) {

            UserProfileChangeRequest profilePhotoUpdate = new UserProfileChangeRequest.Builder()
                    .setDisplayName(photoUri)
                    .build();

            user.updateProfile(profilePhotoUpdate)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("Test", "User profile photo updated.");
                            }
                        }
                    });
        }
    }

    /**
     * Sends email to the user to verify their email used.
     */
    private void sendEmailVerification() {
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
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Test", "Profile fragment RESUMED");
        displayName.setText(currentUser.getDisplayName());
        email.setText(currentUser.getEmail());

        if (!currentUser.isEmailVerified()) {
            emailNotVerified.setVisibility(View.VISIBLE);
            reSendEmailVerificationRelativeLayout.setVisibility(View.VISIBLE);
            reSendEmailVerificationButton.setVisibility(View.VISIBLE);
            emailVerificationSentLabel.setVisibility(View.GONE);
        } else {
            emailNotVerified.setVisibility(View.GONE);
            reSendEmailVerificationRelativeLayout.setVisibility(View.GONE);
            reSendEmailVerificationButton.setVisibility(View.GONE);
            emailVerificationSentLabel.setVisibility(View.GONE);
        }

    }
}