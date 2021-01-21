package com.example.firebase;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.firebase.adapters.AdapterChat;
import com.example.firebase.models.ModelChat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;


public class ChatActivity extends AppCompatActivity {
    //view from xml
    Toolbar toolbar;
    RecyclerView recyclerView;
    ImageView profileIv;
    TextView nameTv, userStatusTv;
    EditText messageEt;
    ImageButton sendBtn;

    //firebase auth
    FirebaseAuth firebaseAuth;
    DatabaseReference usersDbRef;

    FirebaseDatabase firebaseDatabase;

    DatabaseReference userRefForSeen;
    //for checking if use has seen message or not
    ValueEventListener seenListener;


    List<ModelChat> chatList;
    AdapterChat adapterChat;

    String hisUid;
    String myUid;
    String hisImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Toolbar toolbar =findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        recyclerView= findViewById(R.id.chat_recyclerView);
        profileIv =findViewById(R.id.profileIv);
        nameTv = findViewById(R.id.nameTv);
        userStatusTv=findViewById(R.id.userStatusTv);
        messageEt=findViewById(R.id.messageEt);
        sendBtn=findViewById(R.id.sendBtn);

        //Layout (LinearLayout) for RecyclerView
        LinearLayoutManager linearLayoutManager= new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //Recyclerview properties
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        Intent intent=getIntent();
        hisUid=intent.getStringExtra("hisUid");

        //firebase auth
        firebaseAuth=FirebaseAuth.getInstance();

        firebaseDatabase=FirebaseDatabase.getInstance();
        usersDbRef=firebaseDatabase.getReference("User");

        //search user to get that user's info
        Query userQuery =usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get user picture and name
        userQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //check unil required info is recevied
                for(DataSnapshot ds: snapshot.getChildren()){
                    //get data
                    String name=""+ds.child("name").getValue();
                    hisImage=""+ds.child("image").getValue();
                    String typingStatus=""+ds.child("typingTo").getValue();

                    //check typing status
                    if(typingStatus.equals(myUid)){
                        userStatusTv.setText("typing...");

                    }
                    else {
                        //get value of online status
                        String onlineStatus =""+ds.child("onlineStatus").getValue();

                        if(onlineStatus.equals("online")){
                            userStatusTv.setText(onlineStatus);
                        }

                        else {
                            //convert timstamp to proper time date
                            //convert timstamp to dd/mm/YYYY hh:mm am/pm
                            Calendar cal=Calendar.getInstance(Locale.ENGLISH);
                            cal.setTimeInMillis(Long.parseLong(onlineStatus));
                            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
                            userStatusTv.setText("Last seen at:"+ dateTime);
                        }
                    }



                    //set data
                    nameTv.setText(name);

                    try{
                        //image received set it to imageview in toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_default_img).into(profileIv);
                    }
                    catch (Exception e){
                        //there is exception getting picture, set default picture
                        Picasso.get().load(R.drawable.ic_default_img_white).into(profileIv);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        //click button to send message
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get text from edit text
                String message= messageEt.getText().toString().trim();
                //check if text is empty or not
                if(TextUtils.isEmpty(message)){
                    //text empty
                    Toast.makeText(ChatActivity.this,"Can't send the empty message...",Toast.LENGTH_SHORT).show();
                }
                else {
                    //text not empty
                    sendMessage(message);
                }
            }
        });


        messageEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            if(s.toString().trim().length()==0){
                checkTypingStatus("noOne");
            }
            else {
                checkTypingStatus(hisUid);//uid of receiver
            }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        readMessages();

        seenMessage();

    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)){
                        HashMap<String,Object> hashSeenMashMap= new HashMap<>();
                        hashSeenMashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hashSeenMashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readMessages() {
        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for (DataSnapshot ds: snapshot.getChildren()){
                    ModelChat chat =ds.getValue(ModelChat.class);
                    if(chat.getReceiver().equals(myUid)&&chat.getSender().equals(hisUid)||
                            chat.getReceiver().equals(hisUid)&&chat.getSender().equals(myUid)){
                        chatList.add(chat);
                    }
                    //adapter
                    adapterChat = new AdapterChat(ChatActivity.this,chatList,hisImage);
                    adapterChat.notifyDataSetChanged();

                    recyclerView.setAdapter(adapterChat);

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String message) {
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        String timestamp = String.valueOf(System.currentTimeMillis());

        HashMap<String, Object> hashMap= new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("receiver",hisUid);
        hashMap.put("message",message);
        hashMap.put("timestamp",timestamp);
        hashMap.put("isSeen",false);
        databaseReference.child("Chats").push().setValue(hashMap);

        //reset edittext after sending message
        messageEt.setText("");
    }


    private  void checkUserStatus(){

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            //user đăng nhập thì ở đây
            //hiện tên đăng nhập
            // mProfileTv.setText(user.getEmail());
            myUid=user.getUid();//currently signed in user uid
        }
        else {
            //user 0 đăng nhập, sẽ chuyển sang Main Activity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }


    private  void checkOnlineStatus(String status){
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference("User").child(myUid);
        HashMap<String,Object>hashMap= new HashMap<>();
        hashMap.put("onlineStatus",status);
        //update value of onlineStatus of user
        dbRef.updateChildren(hashMap);
    }
    private  void checkTypingStatus(String typing){
        DatabaseReference dbRef =FirebaseDatabase.getInstance().getReference("User").child(myUid);
        HashMap<String,Object>hashMap= new HashMap<>();
        hashMap.put("typingTo",typing);
        //update value of onlineStatus of user
        dbRef.updateChildren(hashMap);
    }

    @Override
    protected void onStart() {
        checkUserStatus();
        //check online
        checkOnlineStatus("online");
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        //get timestamp
        String timestamp =String.valueOf(System.currentTimeMillis());

        //set ofline with last seen time stamp
        checkOnlineStatus(timestamp);
        checkTypingStatus("noOne");
        userRefForSeen.removeEventListener(seenListener);
    }

    @Override
    protected void onResume() {

        //set online
        checkOnlineStatus("online");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        //hide searchview, as we dont need it here
        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

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