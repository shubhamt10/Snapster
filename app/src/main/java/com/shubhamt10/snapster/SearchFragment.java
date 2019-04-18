package com.shubhamt10.snapster;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.algolia.search.saas.AlgoliaException;
import com.algolia.search.saas.Client;
import com.algolia.search.saas.CompletionHandler;
import com.algolia.search.saas.Index;
import com.algolia.search.saas.Query;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    private EditText searchText;
    private RecyclerView searchRecyclerView;
    private TextView searchTextView;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;
    private CollectionReference usersReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersReference = firestore.collection("users");

        searchText = view.findViewById(R.id.searchEditText);
        searchRecyclerView = view.findViewById(R.id.searchRecyclerView);
        searchTextView = view.findViewById(R.id.searchForUsers);

        Client client = new Client("2V160HLXTY", "80f5e050be3d544f1ae19151a4c4945c");
        final Index index = client.getIndex("users");

        searchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchTextView.setVisibility(View.GONE);
                Query query = new Query(editable.toString())
                        .setAttributesToRetrieve("name", "username")
                        .setHitsPerPage(20);
                index.searchAsync(query, new CompletionHandler() {
                    @Override
                    public void requestCompleted(JSONObject content, AlgoliaException error) {
                        System.out.println(content);
                        try {
                            JSONArray hits = content.getJSONArray("hits");
                            final ArrayList<ResultUser> list = new ArrayList<>();
                            for (int i=0; i<hits.length(); i++){
                                JSONObject object = hits.getJSONObject(i);
                                String name = object.getString("name");
                                String username = object.getString("username");
                                String highlited = object.getString("_highlightResult");
                                JSONObject hlt = new JSONObject(highlited);
                                JSONObject dp = hlt.getJSONObject("dpUrl");
                                String url = dp.getString("value");
                                String uid = object.getString("objectID");
                                ResultUser user = new ResultUser(name, username, url, uid);
                                if (!user.getUid().equals(firebaseAuth.getCurrentUser().getUid())) {
                                    list.add(user);
                                }
                            }

                            SearchRecycleViewAdapter.RecyclerViewClickListener listener = new SearchRecycleViewAdapter.RecyclerViewClickListener() {
                                @Override
                                public void onClick(View view, int position) {
                                    String uid = list.get(position).getUid();
                                    System.out.println(uid);
                                    final UserFragment userFragment = new UserFragment();
                                    usersReference.whereEqualTo("uid",uid).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            User user = queryDocumentSnapshots.getDocuments().get(0).toObject(User.class);
                                            userFragment.setUser(user);
                                            System.out.println(user);
                                            try {
                                                getFragmentManager().beginTransaction().replace(R.id.fragments_container,userFragment).commit();
                                            }catch (Exception e){
                                                System.out.println(e.getMessage());
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            System.out.println(e.getMessage());
                                        }
                                    });
                                }
                            };
                            SearchRecycleViewAdapter adapter = new SearchRecycleViewAdapter(getContext(),list,listener);
                            searchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            searchRecyclerView.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        return view;
    }

}
