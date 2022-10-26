package com.example.test_app;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity2 extends AppCompatActivity {

    ImageView google_img;
    GoogleSignInOptions gso;
    GoogleSignInClient gsc;

    String name,surname,email;
    TextView name_txt, surname_txt, email_txt;
    Button back,logout;
    int sign=0;

    FirebaseAuth mAuth;

    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //do something
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        name_txt = findViewById(R.id.google_name);
        surname_txt = findViewById(R.id.google_surname);
        email_txt = findViewById(R.id.google_email);
        back = findViewById(R.id.back);
        logout = findViewById(R.id.logout);
        google_img = findViewById(R.id.google);

        mAuth = FirebaseAuth.getInstance();

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
        gsc = GoogleSignIn.getClient(this,gso);

        google_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignIn();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!name.equals("none")) openActivity1();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SignOut();
                displayAccount();
            }
        });

        loadData();

    }

    public void SignIn(){
        Intent intent = gsc.getSignInIntent();
        startActivityForResult(intent,100);
    }

    public void SignOut(){
        gsc.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                name="none";
                surname="none";
                email="none";
                saveData();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==100){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try{
                task.getResult(ApiException.class);
                displayAccount();
                String password = name;
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("myapp:", "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w("myapp:", "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(MainActivity2.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }catch (ApiException e){
                Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();;
            }
        }
    }

    public void openActivity1(){
        finish();
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void displayAccount(){
        GoogleSignInAccount googleSignInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(googleSignInAccount!=null){
            name_txt.setText(googleSignInAccount.getDisplayName());
            surname_txt.setText(googleSignInAccount.getFamilyName());
            email_txt.setText(googleSignInAccount.getEmail());
            sign=1;
            name=googleSignInAccount.getDisplayName();
            surname=googleSignInAccount.getFamilyName();
            email=googleSignInAccount.getEmail();
            saveData();
        }
        else{
            name_txt.setText("NAME");
            surname_txt.setText("SURNAME");
            email_txt.setText("EMAIL");
            name="none";
            surname="none";
            email="none";
            saveData();
        }
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences("ALL_ACTIVITY", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("NAME", name);
        editor.putString("SURNAME", surname);
        editor.putString("EMAIL", email);
        editor.apply();
    }

    public void loadData(){
        SharedPreferences sharedPreferences = getSharedPreferences("ALL_ACTIVITY", MODE_PRIVATE);
        name = sharedPreferences.getString("NAME","none");
        surname = sharedPreferences.getString("SURNAME","none");
        email = sharedPreferences.getString("EMAIL","none");
    }
}