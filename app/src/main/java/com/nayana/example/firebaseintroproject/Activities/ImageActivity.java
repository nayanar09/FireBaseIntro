package com.nayana.example.firebaseintroproject.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;

public class ImageActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private List<Upload> uploadList;
    private DatabaseReference databaseReference;
    private FirebaseStorage firebaseStorage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

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

                myAdapter = new MyAdapter( ImageActivity.this , uploadList);
                recyclerView.setAdapter(myAdapter);

                myAdapter.setOnItemClickListener(new MyAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        String text = uploadList.get(position).getImageName();
                        Toast.makeText( ImageActivity.this , text+" is selected "+position ,Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onDoAnyTask(int position) {
                        Toast.makeText( ImageActivity.this , "onDoAnyTask is selected " ,Toast.LENGTH_LONG).show();
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
}
