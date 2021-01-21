package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        actionBar= getSupportActionBar();
        actionBar.setTitle("Thông tin");


        firebaseAuth=FirebaseAuth.getInstance();

        BottomNavigationView navigationView=findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        //xử lý nút home(default, on star)
        actionBar.setTitle("Home");
        HomeFragment fragment1 = new HomeFragment();
        FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
        ft1.replace(R.id.content, fragment1,"");
        ft1.commit();
    }

    private  BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    //xử lý click
                    switch (menuItem.getItemId()){
                        case R.id.nav_home:
                            //xử lý nút home
                            actionBar.setTitle("Home");
                            HomeFragment fragment1 = new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.content, fragment1,"");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //xử lý nút profile

                            actionBar.setTitle("Profile");
                            ProfileFragment fragment2 = new ProfileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.content, fragment2,"");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //xử lý nút users
                            actionBar.setTitle("Users");
                            UsersFragment fragment3 = new UsersFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.content, fragment3,"");
                            ft3.commit();
                            return true;

                     /*   case R.id.nav_chat:
                            //xử lý nút users
                            actionBar.setTitle("Chats");
                            ChatListFragment fragment4 = new ChatListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.content, fragment4,"");
                            ft4.commit();
                            return true;*/
                    }


                    return false;
                }
            };

    private  void checkUserStatus(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user đăng nhập thì ở đây
            //hiện tên đăng nhập
           // mProfileTv.setText(user.getEmail());
        }
        else {
            //user 0 đăng nhập, sẽ chuyển sang Main Activity
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onStart() {
        //chạy kiểm tra trên app
        checkUserStatus();
        super.onStart();
    }


}