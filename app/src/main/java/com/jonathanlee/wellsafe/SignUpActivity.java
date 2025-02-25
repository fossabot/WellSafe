package com.jonathanlee.wellsafe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jonathanlee.wellsafe.authentication.Users;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.hbb20.CountryCodePicker;

public class SignUpActivity extends AppCompatActivity {

    protected EditText emailEditText;
    protected EditText passwordEditText;
    protected EditText phoneEditText;
    protected EditText fullNameEditText;
    protected TextView loginTextView;
    protected Button signupButton;
    protected CountryCodePicker countryCode;
    private FirebaseAuth authFirebase;
    DatabaseReference databaseReference;
    FirebaseDatabase firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();  // Hide Action Bar
        }
        catch (NullPointerException e){}
        setContentView(R.layout.activity_sign_up);

        //Initialize Firebase
        authFirebase = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        loginTextView = (TextView) findViewById(R.id.loginText);
        passwordEditText = (EditText) findViewById(R.id.passwordField);
        emailEditText = (EditText) findViewById(R.id.emailField);
        phoneEditText = (EditText) findViewById(R.id.phoneNumberField);
        fullNameEditText = (EditText) findViewById(R.id.fullNameField);
        signupButton = (Button) findViewById(R.id.signupButton);
        countryCode = (CountryCodePicker) findViewById(R.id.countryCode);

        loginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUpActivity.this, LogInActivity.class);
                startActivity(intent);
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String fullName = fullNameEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                final String password = passwordEditText.getText().toString();
                final String phone = phoneEditText.getText().toString();
                final String phoneNumber;

                fullName.trim();
                password.trim();
                email.trim();
                phone.trim();

                countryCode.registerCarrierNumberEditText(phoneEditText);
                phoneNumber = countryCode.getFullNumberWithPlus();

                if (email.isEmpty() || password.isEmpty() || fullName.isEmpty() || phone.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                    builder.setMessage(R.string.signup_error_message)
                            .setTitle(R.string.signup_error_title)
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } else {
                    authFirebase.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>(){
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task){
                                    if(task.isSuccessful()){
                                        Users userData = new Users(fullName, email, phoneNumber);
                                        FirebaseDatabase.getInstance().getReference("Users")
                                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                                .child("User Information")
                                                .setValue(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(SignUpActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                                Intent intent = new Intent(SignUpActivity.this, HomeActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        });
                                    } else {
                                        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                                        builder.setMessage(task.getException().getMessage())
                                                .setTitle(R.string.signup_error_title)
                                                .setPositiveButton(android.R.string.ok, null);
                                        AlertDialog dialog = builder.create();
                                        dialog.show();
                                    }
                                }
                            });

                }
            }
        });
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Are you sure you want to quit?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user pressed "yes", then he is allowed to exit from application
                finish();
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //if user select "No", just cancel this dialog and continue with app
                dialog.cancel();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}