package css.cis3334.firebaseauthentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    // Declare global variables
    private TextView textViewStatus;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonGoogleLogin;
    private Button buttonCreateLogin;
    private Button buttonSignOut;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private GoogleApiClient mGoogleApiClient;
    private static final int GOOGLE_SIGN_IN_FLAG = 9001;

    // The method below will determine what the screen will look like on the initial start up
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // declare local variables -> setting widgets up to interface with java code
        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonGoogleLogin = (Button) findViewById(R.id.buttonGoogleLogin);
        buttonCreateLogin = (Button) findViewById(R.id.buttonCreateLogin);
        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);

        mAuth = FirebaseAuth.getInstance(); // create an instance of a authorized firebase user

        // if normalLogin button click do this
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn(editTextEmail.getText().toString(), editTextPassword.getText().toString()); // call method and send parameters
            }
        });

        // if createUser button is clicked do tis
        buttonCreateLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createAccount(editTextEmail.getText().toString(), editTextPassword.getText().toString());// call method and send parameters
            }
        });

        // if "google login" button is clicked do this
        buttonGoogleLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                googleSignIn();
            }
        });

        // if the signout button is clicked do this
        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signOut();
            }
        });

        // the method below is used for the normal login -- the normal login will send a request to the firebase authentication sdk
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); // used to get the email address, or login in info, entered by the user
            }
        };

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    // mAuth is logged in
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // removal of mAuth
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // this method is called when a new login is created
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)// call method and send parameters
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      //Display whether or not the new user was successfully created
                        if (!task.isSuccessful()) {
                            textViewStatus.setText("Status: \nFailed to create new login.");
                        }
                        else
                        {
                            textViewStatus.setText("Status: \nLogin successfully created.");
                        }
                    }
                });
    }

    // this method is called when an existing user logs in
    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)// call method and send parameters
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                      //Display whether or not authentication successful
                        if (!task.isSuccessful()) {
                            textViewStatus.setText("Status: \nAuthentication Failed.");
                        }
                        else
                        {
                            textViewStatus.setText("Status: \nAuthentication Successful.");
                        }
                    }
                });
    }

    // called when user logs out
    private void signOut () {
        textViewStatus.setText("Status:\n" + mAuth.getCurrentUser().getEmail() + " signed out.");//display status
        mAuth.signOut();//log user off
    }

    // called when logging in with google - starts a new intent that will return data
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient); // declare new intent
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_FLAG);          //start intent and wait for result
    }


    // This method is used to handle the data returned from the  signInIntent
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_FLAG) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            }
        }
    }


    // if there are any difficulties logging in with the google login then do the method below
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        textViewStatus.setText("Status: \nGoogle Account Fail"); //display result
    }

    // creates an instant of a Firebase authentication SDK that's tie to a google account
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Display is authentication successful
                        if (!task.isSuccessful()) {
                            textViewStatus.setText("Status: \nGOOGLE Authentication failed.");
                        }
                        else
                        {
                            textViewStatus.setText("Status: \nGOOGLE Authentication Successul.");
                        }
                    }
                });
    }
}
