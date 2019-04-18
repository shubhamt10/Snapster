package com.shubhamt10.snapster;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class AddImageFragment extends Fragment {

    private ImageView uploadImageView;
    private EditText captionEditText;
    private String imageUrl;
    private ProgressBar uploadProgressBar;
    private TextView uploadingTextView;
    private Button postButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference postsReference;
    private byte[] byteArray;
    private String path;

    private User user;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_addimage, container, false);

        uploadImageView = view.findViewById(R.id.uploadImageView);
        captionEditText = view.findViewById(R.id.captionEditText);
        uploadProgressBar = view.findViewById(R.id.uploadProgressBar);
        uploadingTextView = view.findViewById(R.id.uploadingTextView);

        uploadImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 1);
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        postsReference = storage.getReference().child("posts");

        firestore.collection("users").whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });

        postButton = view.findViewById(R.id.uploadButton);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!path.isEmpty()){
                    uploadProgressBar.setVisibility(View.VISIBLE);
                    uploadingTextView.setVisibility(View.VISIBLE);
                    final StorageReference storageReference = postsReference.child(path);
                    storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    System.out.println("ImageUrl : " + imageUrl);
                                    Post post = new Post(imageUrl,firebaseAuth.getCurrentUser().getUid(),captionEditText.getText().toString(),user.getUsername());
                                    firestore.collection("posts").add(post).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            System.out.println("Post added with document id: " + documentReference.getId());
                                            firestore.collection("users").whereEqualTo("uid",firebaseAuth.getCurrentUser().getUid())
                                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                @Override
                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                    DocumentSnapshot document =  queryDocumentSnapshots.getDocuments().get(0);
                                                    firestore.collection("users").document(document.getId()).update("postUrls",FieldValue.arrayUnion(imageUrl))
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            System.out.println("User updated");
                                                            Toast.makeText(getContext(),"Upload Complete",Toast.LENGTH_LONG).show();
                                                            uploadImageView.setImageResource(R.drawable.uploadplaceholder);
                                                            captionEditText.setText("");
                                                            uploadProgressBar.setVisibility(View.INVISIBLE);
                                                            uploadingTextView.setVisibility(View.INVISIBLE);
                                                            try {
                                                                ((HomeActivity) getActivity()).updateUser();
                                                            }catch (Exception e){
                                                                System.out.println(e.getMessage());
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            System.out.println("Unable to update user : " + e.getMessage());
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Error uploading profile image. Please try again", Toast.LENGTH_LONG).show();
                        }
                    });

                }
            }
        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            Uri selectedImage = data.getData();
            try {
                String paths[] = selectedImage.getLastPathSegment().split("/");
                if (paths.length > 0) {
                    path = paths[paths.length - 1];
                } else {
                    path = selectedImage.getLastPathSegment();
                }
            }catch (Exception e){
                System.out.println(e.getMessage());
            }

        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), selectedImage);
            System.out.println("Photo received");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream);
            byteArray = stream.toByteArray();
            uploadImageView.setImageBitmap(bitmap);
        } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}