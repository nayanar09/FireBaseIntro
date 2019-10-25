package com.nayana.example.firebaseintroproject.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nayana.example.firebaseintroproject.Model.Customer;
import com.nayana.example.firebaseintroproject.R;

public class MainActivity extends AppCompatActivity {

    //The entry point for accessing a FireBase Database.
    //You can get an instance by calling getInstance() .
    //To access a location in the database and read or write data, use getReference()
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;

    private FirebaseAuth mAuth;
    //The entry point of the FireBase Authentication SDK.
    //First, obtain an instance of this class by calling getInstance().
    private FirebaseAuth.AuthStateListener mAuthListener;
    //Listener called when there is a change in the authentication state.
    private static final String TAG = "MainActivity"; //logt

    private EditText email;
    private EditText password;
    private Button login;
    //private Button signout;
    private Button createAccount;

    /*  {Visit https://firebase.google.com/docs/database/security to learn more about security rules.
             //allows anybody with url to write to database when set to true
             "rules": { ".read": true, ".write": true }
             //allows only authenticated users to write to database
             "rules": { ".read": "auth != null", ".write": "auth != null"}
        }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        database = FirebaseDatabase.getInstance(); //instantiate
        //databaseReference = database.getReference("message");
        //databaseReference = database.getReference("another_message");

        email = (EditText) findViewById(R.id.emailID);
        password = (EditText) findViewById(R.id.passwordID);
        login = (Button) findViewById(R.id.loginID);
        //signout = (Button) findViewById(R.id.signoutID);
        createAccount = (Button) findViewById(R.id.createAccountID);

        mAuth = FirebaseAuth.getInstance();

        //databaseReference.setValue("Hello FireBase I hope everything works fine");
        //databaseReference.setValue("Hello there! Are you liking this!!");

        /*
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String dB_values = dataSnapshot.getValue(String.class);
                Toast.makeText(MainActivity.this, dB_values , Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                //get current user logged in or not
                 FirebaseUser user = firebaseAuth.getCurrentUser();
                 
                 if ( user != null)
                     {
                         // user signed in
                         Log.d(TAG, "onAuthStateChanged: User signed in");
                         Log.d(TAG, "UserName : " + user.getEmail());
                     }
                 else
                     {
                         // user signed out
                         Log.d(TAG, "onAuthStateChanged: User signed out");
                     }
            }
        };

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String emailString = email.getText().toString();
                final String pwd = password.getText().toString();

                if ( !emailString.equals("") && !pwd.equals("")){

                    mAuth.signInWithEmailAndPassword( emailString , pwd)
                            .addOnCompleteListener( MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if ( !task.isSuccessful())
                                        {
                                            Toast.makeText(MainActivity.this , "Failed to Sign In!!" , Toast.LENGTH_LONG).show();
                                        }
                                    else
                                        {
                                            Toast.makeText(MainActivity.this, "Signed In!!", Toast.LENGTH_LONG).show();
                                            //we can write to database with credentials email & password
                                            //databaseReference = database.getReference("usingCredentials");
                                            //databaseReference.setValue("Hurray!! Writing to database using email and password!!");

                                            databaseReference = database.getReference("usingCustomerObject");
                                            //Customer customer = new Customer("raju","rajanna","raju@raju.com",59);
                                            //Customer customer = new Customer();
                                            //Customer customer = new Customer("nayana","nayana","nayana@nayana.com",25);
                                            Customer customer = new Customer("nayana", pwd , emailString ,25);
                                            databaseReference.setValue(customer);

                                            startActivity( new Intent( MainActivity.this , ImageActivity.class));
                                            finish();
                                        }
                                }
                            });
                }
                else
                    {
                        Toast.makeText(MainActivity.this,"Please enter email and password to continue" , Toast.LENGTH_LONG).show();
                    }
            }
        });

//        signout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mAuth.signOut();
//                Toast.makeText(MainActivity.this, "You Signed Out", Toast.LENGTH_LONG).show();
//            }
//        });

        createAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String emailString = email.getText().toString();
                String pwd = password.getText().toString();

                if ( !emailString.equals("") && !pwd.equals("")) {
                    mAuth.createUserWithEmailAndPassword( emailString , pwd )
                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if ( !task.isSuccessful()) {
                                        Toast.makeText( MainActivity.this , "Failed to Create Account", Toast.LENGTH_LONG).show();
                                        }
                                        else
                                            {
                                                Toast.makeText( MainActivity.this , "Account Created Successfully" , Toast.LENGTH_LONG).show();
                                                startActivity( new Intent( MainActivity.this , ImageActivity.class));
                                                finish();
                                            }
                                }
                            });
                }
                else
                {
                    Toast.makeText(MainActivity.this,"Please enter email and password to Create Account" , Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //add auth only when activity is on start
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //onStop make sure listener is null if not remove listener
        if(mAuthListener != null){
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
