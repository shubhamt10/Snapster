package com.shubhamt10.snapster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SignupActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Intent wallIntent;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage firebaseStorage;
    private StorageReference profileImagesReference;

    private EditText nameEditText;
    private EditText usernameEditText;
    private EditText bioEditText;
    private ImageView profileImage;
    private Spinner spinner;
    private String imageUrl;
    private String gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        nameEditText = findViewById(R.id.nameEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        bioEditText = findViewById(R.id.bioEditText);
        profileImage = findViewById(R.id.profileImage);

        spinner = findViewById(R.id.genderSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.genders,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(this);

        imageUrl = "";

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        firebaseStorage = FirebaseStorage.getInstance();
        profileImagesReference = firebaseStorage.getReference().child("profileImages");

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        });

    }

    public void completeSignup(View view){

        String uid = firebaseAuth.getCurrentUser().getUid();
        String email = firebaseAuth.getCurrentUser().getEmail();
        String name = nameEditText.getText().toString();
        String username = usernameEditText.getText().toString();
        String bio = bioEditText.getText().toString();

        if (uid.isEmpty() || name.isEmpty() || username.isEmpty()){
            Toast.makeText(this,"Please enter values for all the fields",Toast.LENGTH_SHORT).show();
        }else {

            User user = new User(uid,name,username,email,gender,imageUrl,bio);
            firestore.collection("users").add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    System.out.println("User added with document id: " + documentReference.getId());
                    wallIntent = new Intent(SignupActivity.this,HomeActivity.class);
                    startActivity(wallIntent);
                    finish();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignupActivity.this,"Error adding user " + e.getMessage(),Toast.LENGTH_LONG).show();
                }
            });
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data!= null){

            Uri selectedImage = data.getData();
            String paths[] = selectedImage.getLastPathSegment().split("/");
            String path;
            if (paths.length>0) {
                path = paths[paths.length - 1];
            }else {
                path = selectedImage.getLastPathSegment();
            }

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImage);
                System.out.println("Photo received");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,10,stream);
                byte[] byteArray = stream.toByteArray();
                profileImage.setImageBitmap(bitmap);

                final StorageReference storageReference = profileImagesReference.child(path);
                storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                imageUrl = uri.toString();
                                System.out.println("ImageUrl : " + imageUrl);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SignupActivity.this,"Error uploading profile image. Please try again",Toast.LENGTH_LONG).show();
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        gender = adapterView.getItemAtPosition(i).toString();
        System.out.println(gender);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        //
    }
}
