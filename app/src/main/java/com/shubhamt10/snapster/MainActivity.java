package com.shubhamt10.snapster;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
//    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db;

    private EditText emailText;
    private EditText passwordText;
    private TextView signUpText;
    private Button loginButton;

    private Intent wallIntent;
    private Intent signupIntent;

    private boolean loginModeIsActive;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loginModeIsActive = true;

        wallIntent = new Intent(MainActivity.this,HomeActivity.class);

        sharedPreferences = getSharedPreferences("mainPreferences", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        if (sharedPreferences.getBoolean("isLoggedIn",false)){
            startActivity(wallIntent);
            finish();
        }

        signupIntent = new Intent(MainActivity.this,SignupActivity.class);
        emailText = findViewById(R.id.email);
        passwordText = findViewById(R.id.password);
        signUpText = findViewById(R.id.signupText);
        loginButton = findViewById(R.id.loginButton);

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginModeIsActive) {
                    loginModeIsActive = false;
                    signUpText.setText(R.string.OrLogin);
                    loginButton.setText(R.string.Signup);
                }else {
                    loginModeIsActive = true;
                    signUpText.setText(R.string.OrSignup);
                    loginButton.setText(R.string.Login);
                }
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    public void loginOrSignup(View view){
        String email = emailText.getText().toString();
        String password = passwordText.getText().toString();
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(this,"Email or password is empty",Toast.LENGTH_SHORT).show();
        }else {
            if (loginModeIsActive) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        editor.putBoolean("isLoggedIn",true);
                        editor.commit();
                        startActivity(wallIntent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Login unsuccessful: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }else {
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        Toast.makeText(MainActivity.this, "Signup successful", Toast.LENGTH_LONG).show();
                        editor.putBoolean("isLoggedIn",true);
                        editor.commit();
                        startActivity(signupIntent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Signup unsuccessful: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        }

    }


}
