package com.hvg_group.makeithappen;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUpActivity extends AppCompatActivity {


    String mailid, passwd, repasswd, usrnme, userId;
    EditText mail, pass, repass, name;
    TextInputLayout inputLayout,inputLayout1;
    public FirebaseAuth firebaseAuth;
    public FirebaseAuth.AuthStateListener authStateListener;
    DatabaseReference dbref;
    FirebaseUser user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        name = (EditText) findViewById(R.id.edtnme_reg);
        mail = (EditText)findViewById(R.id.edtxtmail_reg);
        pass = (EditText) findViewById(R.id.edtxtpass_reg);
        repass = (EditText) findViewById(R.id.edtxtcnfrmpass_reg);

        inputLayout = (TextInputLayout) findViewById(R.id.textInputLayout) ;
        inputLayout1 = (TextInputLayout) findViewById(R.id.textInputLayout2);

        dbref = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener()
        {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth)
            {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null)
                {
                    reset();
                    mail.setText("");
                    name.setText("");
                } else
                {
                    Intent intent = new Intent(SignUpActivity.this,HomeActivity.class);
                    startActivity(intent);
                }
            }
        };
        firebaseAuth.addAuthStateListener(authStateListener);

    }
    public void Reg(View view)
    {
        usrnme = name.getText().toString();
        mailid = mail.getText().toString();
        passwd = pass.getText().toString();
        repasswd = repass.getText().toString();

        inputLayout.setError(null);
        inputLayout1.setError(null);
        if(usrnme.equals(""))
        {

            name.setError("Required");
        }

        if(mailid.equals(""))
        {
            mail.setError("Required");
        }

        if(passwd.equals(""))
        {
            inputLayout.setError("Required");
            return;
        }

        if(repasswd.equals(""))
        {
            inputLayout1.setError("Required");
            return;
        }


        if(passwd.length()<6)
        {
            inputLayout.setError("Length of the Password must be atleast 6 characters");
            reset();
            return;
        }

        if(!(passwd.equals(repasswd)))
        {
            inputLayout1.setError("Enter Same Passwords");
            reset();
            return;
        }



        firebaseAuth.createUserWithEmailAndPassword(mailid,passwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful())
                {
                    Toast.makeText(SignUpActivity.this,"New User Account Created",Toast.LENGTH_SHORT).show();
                    Toast.makeText(SignUpActivity.this,"Logged in Successfully",Toast.LENGTH_SHORT).show();
                    user = FirebaseAuth.getInstance().getCurrentUser();
                    userId = user.getUid();
                    DataEntry(usrnme,mailid);
                }
                else
                {
                    Toast.makeText(SignUpActivity.this,"User ID already exists",Toast.LENGTH_SHORT).show();
                    reset();
                    mail.setText("");
                }
            }
        });
    }

    public void reset()
    {
        pass.setText("");
        repass.setText("");
    }

    public void back(View view)
    {
        finish();
    }

    public void DataEntry(String name, String email)
    {
        User user = new User(name, email);
        dbref.child("users").child(userId).setValue(user);
    }

}
