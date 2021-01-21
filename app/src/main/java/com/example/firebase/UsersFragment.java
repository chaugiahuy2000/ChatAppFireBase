package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import com.example.firebase.adapters.AdapterUsers;
import com.example.firebase.models.ModelUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class UsersFragment extends Fragment {

RecyclerView recyclerView;
AdapterUsers adapterUsers;
List<ModelUsers> usersList;

//firebase auth
    FirebaseAuth firebaseAuth;

    public UsersFragment() {
        // Required empty public constructor
    }




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_users, container, false);


        //init
        firebaseAuth=FirebaseAuth.getInstance();

        //init recylerview
        recyclerView= view.findViewById(R.id.users_recyclerView);
        //set it's properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //init user list
        usersList=new ArrayList<>();

        //get all users
        getAllUsers();

        return view;
    }

    private void getAllUsers() {
        // get current user
      final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database name"Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                      //get all users except currently signed in users
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        usersList.add(modelUsers);
                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    //set adatpter to reycler view
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void searchUsers(final String s ) {

        final FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        //get path of database name"Users" containing users info
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("User");
        //get all data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                usersList.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUsers modelUsers = ds.getValue(ModelUsers.class);

                    //get all searched users except currently signed in users
                    if(!modelUsers.getUid().equals(fUser.getUid())){
                        // xử lý tìm name và email
                        if (modelUsers.getName().toLowerCase().contains(s.toLowerCase()) ||
                        modelUsers.getEmail().toLowerCase().contains(s.toLowerCase())){
                            usersList.add(modelUsers);
                        }


                    }

                    //adapter
                    adapterUsers = new AdapterUsers(getActivity(),usersList);
                    //refresh adapter
                    adapterUsers.notifyDataSetChanged();
                    //set adatpter to reycler view
                    recyclerView.setAdapter(adapterUsers);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private  void checkUserStatus(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user đăng nhập thì ở đây
            //hiện tên đăng nhập
            // mProfileTv.setText(user.getEmail());
        }
        else {
            //user 0 đăng nhập, sẽ chuyển sang Main Activity
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);//show menu option in fragment
        super.onCreate(savedInstanceState);
    }

    // các chức năng menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);

        //Search view
        MenuItem item =menu.findItem(R.id.action_search);
        SearchView searchView =(SearchView) MenuItemCompat.getActionView(item);

        //search listener
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                //called when user press search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text, search it
                    searchUsers(s);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                //called whenever user press any single letter
                //if search query is not empty then search
                if(!TextUtils.isEmpty(s.trim())){
                    //search text contains text, search it
                    searchUsers(s);
                }
                else {
                    //search text empty, get all users
                    getAllUsers();
                }
                return false;
            }
        });


        super.onCreateOptionsMenu(menu,inflater);
    }



    //xử lý các chức năng click tren
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id =item.getItemId();
        if(id == R.id.action_logout){
            firebaseAuth.signOut();
            checkUserStatus();
        }
        return super.onOptionsItemSelected(item);
    }

}