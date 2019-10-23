package com.nayana.example.firebaseintroproject.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.nayana.example.firebaseintroproject.Model.Upload;
import com.nayana.example.firebaseintroproject.R;
import com.squareup.picasso.Picasso;

import java.io.File;

public class Main2Activity extends AppCompatActivity implements View.OnClickListener {

    private Button chooseImage , saveImage , displayImage;
    private EditText imageNameEditText;
    private ImageButton imageView;
    private ProgressBar progressBar;
    private Uri imageUri;

    private static final int IMAGE_REQUEST_CODE = 1;

    DatabaseReference databaseReference;
    StorageReference storageReference;
    StorageTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        chooseImage = findViewById(R.id.chooseButton);
        saveImage = findViewById(R.id.saveButton);
        displayImage = findViewById(R.id.dispalyButton);
        imageNameEditText = findViewById(R.id.editText);
        imageView = findViewById(R.id.imageButton);
        progressBar = findViewById(R.id.progressBar);

        databaseReference = FirebaseDatabase.getInstance().getReference("Upload");
        storageReference = FirebaseStorage.getInstance().getReference().child("Upload");

        chooseImage.setOnClickListener(this);
        saveImage.setOnClickListener(this);
        displayImage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        switch ( v.getId() ){

            case R.id.chooseButton : openFileChooser();
                break;

            case  R.id.saveButton :

                if ( uploadTask != null && uploadTask.isInProgress()){
                    Toast.makeText( Main2Activity.this , "Uploading in progress" , Toast.LENGTH_LONG).show();
                }
                    else {
                        saveData();
                    }
                break;
                
            case R.id.dispalyButton :

                Intent intent = new Intent( Main2Activity.this , ImageActivity.class);
                startActivity(intent);
                break;
        }
    }

    //getting extension of image
    public String getFileExtension ( Uri imageUri){
        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(imageUri));
    }

    private void saveData() {
        final String imageName = imageNameEditText.getText().toString().trim();

        if ( imageName.isEmpty() ){
            imageNameEditText.setError( "Please Enter image name");
            imageNameEditText.requestFocus();
            return;
        }

        StorageReference reference = storageReference.child( System.currentTimeMillis() + "."+ getFileExtension(imageUri));

        reference.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        Toast.makeText( Main2Activity.this , "Image stored successfully" , Toast.LENGTH_LONG).show();

                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();

                        while ( !uriTask.isSuccessful());

                        Uri downloadUri = uriTask.getResult();

                        Upload upload = new Upload( imageName , downloadUri.toString());

                        //Upload upload = new Upload( imageName , taskSnapshot.getStorage().getDownloadUrl().toString());

                        String uploadID = databaseReference.push().getKey();

                        databaseReference.child(uploadID).setValue(upload);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle unsuccessful uploads
                        Toast.makeText( Main2Activity.this , "Problem storing image " , Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openFileChooser() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult( intent , IMAGE_REQUEST_CODE );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.getData() != null){

            imageUri = data.getData();
            imageView.setImageURI(imageUri);
            //Picasso.with(this).load(imageUri).into(imageView);

        }
    }
}
