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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.app.Activity.RESULT_OK;

public class EditFragment extends Fragment {

    private User user;
    private String imageUrl;
    private Boolean isNewDisplayPicture;

    private ImageView profileImage;
    private EditText nameText;
    private EditText usernameText;
    private EditText bioText;
    private Button updateButton;

    private FirebaseStorage storage;
    private StorageReference dpStorageReference;
    private FirebaseFirestore firestore;
    private CollectionReference usersReference;
    private DocumentReference currentUserDoc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile,container,false);

        isNewDisplayPicture = false;

        profileImage = view.findViewById(R.id.dp);
        nameText = view.findViewById(R.id.nameText);
        usernameText = view.findViewById(R.id.usernameText);
        bioText = view.findViewById(R.id.bioText);
        updateButton = view.findViewById(R.id.updateProfileButton);

        firestore = FirebaseFirestore.getInstance();
        usersReference = firestore.collection("users");
        storage = FirebaseStorage.getInstance();
        dpStorageReference = storage.getReference().child("profileImages");

        Glide.with(profileImage.getContext())
                .asBitmap()
                .load(user.getDisplayPictureUrl())
                .into(profileImage);

        nameText.setText(user.getName());
        usernameText.setText(user.getUsername());
        bioText.setText(user.getBio());

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,1);
            }
        });

        final String oldUrl = user.getDisplayPictureUrl();

        usersReference.whereEqualTo("uid",user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                currentUserDoc = usersReference.document(document.getId());
            }
        });

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = nameText.getText().toString();
                String username = usernameText.getText().toString();
                String bio = bioText.getText().toString();
                if (isNewDisplayPicture) {
                    currentUserDoc.update("name",name,"username",username,"bio",bio,"imageUrl",imageUrl)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(),"User updated successfully",Toast.LENGTH_SHORT).show();
                                    usersReference.whereEqualTo("uid",user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                            ProfileFragment fragment = new ProfileFragment();
                                            fragment.setUser(user);
                                            getFragmentManager().beginTransaction().replace(R.id.fragments_container,fragment).commit();
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    System.out.println(e.getMessage());
                                }
                            });
                }else {
                    currentUserDoc.update("name",name,"username",username,"bio",bio)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(),"User updated successfully",Toast.LENGTH_SHORT).show();
                                usersReference.whereEqualTo("uid",user.getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                        storage.getReferenceFromUrl(oldUrl).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                System.out.println("Old image successfully deleted");
                                            }
                                        });
                                        ProfileFragment fragment = new ProfileFragment();
                                        fragment.setUser(user);
                                        getFragmentManager().beginTransaction().replace(R.id.fragments_container,fragment).commit();
                                    }
                                });

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                System.out.println(e.getMessage());
                            }
                        });
                }
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
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
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),selectedImage);
                System.out.println("Photo received");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,10,stream);
                byte[] byteArray = stream.toByteArray();
                profileImage.setImageBitmap(bitmap);

                final StorageReference storageReference = dpStorageReference.child(path);
                storageReference.putBytes(byteArray).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                isNewDisplayPicture = true;
                                imageUrl = uri.toString();
                                System.out.println("ImageUrl : " + imageUrl);
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),"Error uploading profile image. Please try again",Toast.LENGTH_LONG).show();
                    }
                });
            }catch (IOException e){
                e.printStackTrace();
            }

        }
    }

    public void setUser(User user){
        this.user = user;
    }
}
