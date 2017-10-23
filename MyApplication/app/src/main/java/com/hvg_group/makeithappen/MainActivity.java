package com.hvg_group.makeithappen;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.transition.Transition;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, View.OnClickListener
{


    String mailid, passwd, userId,usrnme;
    EditText mail, pass;
    TextInputLayout inputLayout;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser user;
    GoogleApiClient googleApiClient;
    DatabaseReference dbref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mail = (EditText) findViewById(R.id.edtxt_mail_login);
        pass = (EditText) findViewById(R.id.edtxt_pass_login);

        inputLayout  = (TextInputLayout) findViewById(R.id.textInputLayout3);
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    reset();
                } else {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        };

        dbref = FirebaseDatabase.getInstance().getReference();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestIdToken(getString(R.string.default_web_client_id)).requestEmail().build();
        googleApiClient = new GoogleApiClient.Builder(MainActivity.this).enableAutoManage(this,this).addApi(Auth.GOOGLE_SIGN_IN_API,googleSignInOptions).build();

        firebaseAuth.addAuthStateListener(authStateListener);
        SignInButton signInButton = (SignInButton) findViewById(R.id.btn_gsignin);
        signInButton.setOnClickListener(this);
        signInButton.setSize(SignInButton.SIZE_WIDE);
        for (int i = 0; i < signInButton.getChildCount(); i++)
        {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView)
            {
                TextView tv = (TextView) v;
                tv.setText("Google");
                return;
            }
        }

    }


    public void Signin(View view) {
        mailid = mail.getText().toString();
        passwd = pass.getText().toString();
        if(mailid.equals(""))
        {
            mail.setError("Enter a valid E-Mail ID");
        }
        else if(passwd.equals(""))
        {
            inputLayout.setError("Password is Required");
        }
        else
        {
            inputLayout.setError(null);
            firebaseAuth.signInWithEmailAndPassword(mailid, passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful())
                    {
                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Email ID or Password is Invalid!", Toast.LENGTH_SHORT).show();
                        reset();
                    }
                }
            });
        }
    }

    public void signup(View view) {
        Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
        startActivity(intent);
    }

    public void reset() {
        mail.setText("");
        pass.setText("");
    }

    public void forgotPass(View view)
    {
        mailid = mail.getText().toString();
        if (mailid.equals("")) {
            mail.setError("Enter a valid E-Mail ID");
        } else {
            firebaseAuth.sendPasswordResetEmail(mailid).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Reset Mail sent to " + mailid, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "E-Mail ID is incorrect\n\r             OR\n\rUser does not Exist", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    public void Home(View view)
    {
        Intent intent = new Intent(MainActivity.this,HomeActivity.class);
        intent.putExtra("Status","SKIP_LOGIN");
        startActivity(intent);
    }

    public void onClick(View view)
    {
        Log.i("App","Hi, there");
        Intent gsignInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(gsignInIntent,9001);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        Toast.makeText(MainActivity.this,"Google Play Services error", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == 9001)
        {
            GoogleSignInResult signInResult = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if(signInResult.isSuccess())
            {
                GoogleSignInAccount account = signInResult.getSignInAccount();
                firebaseAuthwithGoogle(account);
            }
            else
            {
                Toast.makeText(MainActivity.this,"Google Sign In Failed", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    }

    public void firebaseAuthwithGoogle(final GoogleSignInAccount account)
    {
        final AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(),null);
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    usrnme = account.getDisplayName();
                    mailid = user.getEmail();
                    Log.i("App",userId +" " + usrnme + " " + mailid);
                    DataEntry(usrnme,mailid);
                } else {
                    Toast.makeText(MainActivity.this, "Authentication Failed!", Toast.LENGTH_SHORT).show();
                    reset();
                }
            }
        });
    }

    public void DataEntry(String name, String email)
    {
        User user = new User(name, email);
        dbref.child("users").child(userId).setValue(user);
    }


}

