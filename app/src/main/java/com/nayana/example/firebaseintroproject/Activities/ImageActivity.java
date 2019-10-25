package com.nayana.example.firebaseintroproject.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nayana.example.firebaseintroproject.Adapter.MyAdapter;
import com.nayana.example.firebaseintroproject.Model.Upload;
import com.nayana.example.firebaseintroproject.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<Upload> uploadList;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private static final String TAG = "ImageActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager( new LinearLayoutManager( this));

        progressBar = findViewById(R.id.recyclerProgressBar);

        uploadList = new ArrayList<>();

        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        firebaseStorage = FirebaseStorage.getInstance();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                uploadList.clear();

                for ( DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()){

                    Upload upload = dataSnapshot1.getValue(Upload.class);
                    upload.setKey(dataSnapshot1.getKey());
                    uploadList.add(upload);
                }

                Collections.reverse(uploadList);

                myAdapter = new MyAdapter( ImageActivity.this , uploadList);
                recyclerView.setAdapter(myAdapter);

                myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String text = uploadList.get(position).getImageName();
                        Toast.makeText( ImageActivity.this , text+" is selected at position " + (position+1) ,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDoAnyTask(int position) {
                        Toast.makeText( ImageActivity.this , "onDoAnyTask is selected at position "+ (position+1) ,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDelete(int position) {
                        //Toast.makeText( ImageActivity.this , "onDelete is selected " ,Toast.LENGTH_LONG).show();
                        Upload selectedItemToDelete = uploadList.get(position);
                        final String key = selectedItemToDelete.getKey();

                        StorageReference storageReference = firebaseStorage.getReferenceFromUrl(selectedItemToDelete.getImageUrl());
                        storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                               databaseReference.child(key).removeValue();
                                Toast.makeText( ImageActivity.this , "Item is deleted" ,Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText( ImageActivity.this , "problem deleting item" ,Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });

                progressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                Toast.makeText( ImageActivity.this , "Error : "+databaseError.getMessage() , Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu , menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch ( item.getItemId()){

            case R.id.action_add :
                if ( mAuth != null && mUser != null ) {

                    startActivity( new Intent( ImageActivity.this , Main2Activity.class));
                    finish();
                }
                break;

            case R.id.action_signout :
                if ( mAuth != null && mUser != null){

                    mAuth.signOut();
                    Toast.makeText( ImageActivity.this , "User Signed Out" , Toast.LENGTH_LONG).show();
                    Log.d( TAG , mUser.getEmail());
                    startActivity( new Intent( ImageActivity.this , MainActivity.class));
                    finish();
                }
        }

        return super.onOptionsItemSelected(item);
    }
}
