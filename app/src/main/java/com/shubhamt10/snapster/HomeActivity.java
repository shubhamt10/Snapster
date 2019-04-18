package com.shubhamt10.snapster;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference usersReference;
    private CollectionReference postsReference;
    private User currentUser;
    private ArrayList<Post> posts = new ArrayList<>();
    private Intent mainIntent;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            Fragment fragment = null;

            switch (item.getItemId()) {
                case R.id.navigation_wall:
                    fragment = new WallFragment();
                    ((WallFragment) fragment).setPosts(posts);
                    getSupportActionBar().setTitle("Snapster");
                    break;
                case R.id.navigation_search:
                    updatePosts();
                    fragment = new SearchFragment();
                    getSupportActionBar().setTitle("Search");
                    break;
                case R.id.navigation_addImage:
                    updatePosts();
                    fragment = new AddImageFragment();
                    getSupportActionBar().setTitle("Post");
                    break;
                case R.id.navigation_profile:
                    updatePosts();
                    fragment = new ProfileFragment();
                    ((ProfileFragment) fragment).setUser(currentUser);
                    getSupportActionBar().setTitle(currentUser.getUsername());
                    break;
            }

            return loadFragment(fragment);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Client client = new Client("2V160HLXTY", "80f5e050be3d544f1ae19151a4c4945c");
        final Index index = client.getIndex("users");

        mainIntent = new Intent(HomeActivity.this, MainActivity.class);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        getSupportActionBar().setTitle("Snapster");

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersReference = firestore.collection("users");
        postsReference = firestore.collection("posts");

        loadFragment(new LoadingFragment());

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user!=null) {
            updateUser();
            usersReference.whereEqualTo("uid", user.getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    currentUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                    posts.clear();
                    postsReference.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            DocumentSnapshot documentSnapshot;
                            for (int i=0; i<queryDocumentSnapshots.size(); i++){
                                documentSnapshot = queryDocumentSnapshots.getDocuments().get(i);
                                try {
                                    for (String foll: currentUser.getFollowing()) {
                                        if (documentSnapshot.get("uploaderUid").equals(foll)) {
                                            posts.add(documentSnapshot.toObject(Post.class));
                                        }
                                    }
                                }catch (Exception e){
                                    System.out.println(e.getMessage());
                                }
                            }

                            WallFragment wallFragment = new WallFragment();
                            wallFragment.setPosts(posts);
                            loadFragment(wallFragment);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            System.out.println(e.getMessage());
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
            Toast.makeText(this,"Current user is null",Toast.LENGTH_LONG).show();
        }

        usersReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                List<JSONObject> array = new ArrayList<>();
                User thisUser;
                for (int i=0; i<queryDocumentSnapshots.size(); i++){
                    thisUser = queryDocumentSnapshots.getDocuments().get(i).toObject(User.class);
                    try {
                        array.add(new JSONObject().put("objectID", thisUser.getUid())
                                .put("name", thisUser.getName())
                                .put("dpUrl", thisUser.getDisplayPictureUrl())
                                .put("username", thisUser.getUsername()));
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }
                index.addObjectsAsync(new JSONArray(array), new CompletionHandler() {
                    @Override
                    public void requestCompleted(@Nullable JSONObject jsonObject, @Nullable AlgoliaException e) {
                        if (e == null) {
                            System.out.println("Index successfully updated");
                        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.optionsmenu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                FirebaseAuth.getInstance().signOut();
                getSharedPreferences("mainPreferences", Context.MODE_PRIVATE).edit().putBoolean("isLoggedIn",false).apply();
                currentUser = null;
                startActivity(mainIntent);
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean loadFragment(Fragment fragment){

        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragments_container, fragment)
                    .commit();

            return true;
        }

        return false;
    }

    public void updateUser(){
        usersReference.whereEqualTo("uid", firebaseAuth.getCurrentUser().getUid())
                .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                currentUser = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

    public void updatePosts(){
        posts.clear();
        updateUser();
        postsReference.orderBy("timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                DocumentSnapshot documentSnapshot;
                for (int i=0; i<queryDocumentSnapshots.size(); i++){
                    documentSnapshot = queryDocumentSnapshots.getDocuments().get(i);
                    try {
                        for (String foll: currentUser.getFollowing()) {
                            if (documentSnapshot.get("uploaderUid").equals(foll)) {
                                posts.add(documentSnapshot.toObject(Post.class));
                            }
                        }
                    }catch (Exception e){
                        System.out.println(e.getMessage());
                    }
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                System.out.println(e.getMessage());
            }
        });
    }

}
