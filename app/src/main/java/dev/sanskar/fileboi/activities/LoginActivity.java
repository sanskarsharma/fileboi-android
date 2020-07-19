package dev.sanskar.fileboi.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

import dev.sanskar.fileboi.MainActivity;
import dev.sanskar.fileboi.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();

    Button btnSignIn;

    int RC_SIGN_IN = 1111 ;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();


    public void checkUser(){
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }

    private void startFiebaseUISignInFlow() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build()
        );

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false, false)
                        .build(),
                RC_SIGN_IN);
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        checkUser();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnSignIn = (Button)findViewById(R.id.buttonSignIn);
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFiebaseUISignInFlow();
            }
        });

    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("BLAH-BLAH", String.valueOf(requestCode));
        Log.e("BLAH-BLAH", String.valueOf(resultCode));

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();           // ...
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Log.e(TAG, "Sign-in error: ");
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.e(TAG, "Sign-in error: ", response.getError());
                    return;
                }

                Log.e(TAG, "Sign-in error: ", response.getError());

            }
        }
    }



}