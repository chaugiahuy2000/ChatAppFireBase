package com.example.firebase;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.security.cert.PolicyNode;
import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static com.google.firebase.FirebaseApp.getInstance;


public class ProfileFragment extends Fragment {


    //fire base
    FirebaseAuth firebaseAuth;
    FirebaseUser user;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    //storage
    StorageReference storageReference;
    // path where images of user profile and cover will be stored
    String storagePath ="Users_Profile_Cover_Imgs/";

    // view from xml
    ImageView avaterIv, coverIv;
    TextView nameTv, emailTv, phoneTv;
    FloatingActionButton fab;

    //progress dialog
    ProgressDialog pd;



    //permissions constants
    private  static  final  int CAMERA_REQUEST_CODE=100;
    private  static  final  int STORAGE_REQUEST_CODE=200;
    private  static  final  int IMAGE_PICK_GALLERY_CODE=300;
    private  static  final  int IMAGE_PICK_CAMERA_CODE=400;
    //arrays of permissions to be requested
    String cameraPermissions[];
    String storagePermissions[];

    //uri of picked image
    Uri image_uri;

    //for checking profile or cover photo
    String  profileOrCoverPhoto;

    public ProfileFragment() {
        // Required empty public constructor
    }






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view =inflater.inflate(R.layout.fragment_profile, container, false);
        firebaseAuth =FirebaseAuth.getInstance() ;
        user=firebaseAuth.getCurrentUser();
        firebaseDatabase=FirebaseDatabase.getInstance();
        databaseReference=firebaseDatabase.getReference("User");
        storageReference=  FirebaseStorage.getInstance().getReference();


        //init arrays of permissions
        cameraPermissions=new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions=new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //init views
        avaterIv = view.findViewById(R.id.avatarIv);
        coverIv = view.findViewById(R.id.coverIv);
        nameTv = view.findViewById(R.id.nameTv);
        emailTv = view.findViewById(R.id.emailTv);
        phoneTv = view.findViewById(R.id.phoneTv);
        fab = view.findViewById(R.id.fab);


        //khai báo progress dialog
        pd = new ProgressDialog(getActivity());




        Query query = databaseReference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {

                for(DataSnapshot ds : datasnapshot.getChildren()){
                // get data
                    String name =""+ds.child("name").getValue();
                    String email =""+ds.child("email").getValue();
                    String phone =""+ds.child("phone").getValue();
                    String image =""+ds.child("image").getValue();
                    String cover =""+ds.child("cover").getValue();

                    //set data
                    nameTv.setText(name);
                    emailTv.setText(email);
                    phoneTv.setText(phone);

                    try{

                        Picasso.get().load(image).into(avaterIv);
                    }
                    catch (Exception e){
                        Picasso.get().load(R.drawable.ic_default_img_white).into(avaterIv);
                    }

                    try{

                        Picasso.get().load(cover).into(coverIv);
                    }
                    catch (Exception e){
                    }


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // fab button click
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });


        return view;
    }

    private  boolean checkStoragePermission(){
        // check if storage permission is enabled or not
        // return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == (PackageManager.PERMISSION_GRANTED);
        return  result;

    }

    private  void  requestStoragePermision(){
        //repuest runtime storage permission
        requestPermissions(storagePermissions,STORAGE_REQUEST_CODE);
    }


    private  boolean checkCameraPermission(){
        // check if camera permission is enabled or not
        // return true if enabled
        // return false if not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                == (PackageManager.PERMISSION_GRANTED);

        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == (PackageManager.PERMISSION_GRANTED);
        return  result && result1;

    }

    private  void  requestCameraPermision(){
        //repuest runtime storage permission
        requestPermissions(cameraPermissions,CAMERA_REQUEST_CODE);
    }



    private void showEditProfileDialog() {
        /*
        show dialog để các chức năng
        1 chỉnh sửa font ảnh
        2 chỉnh hình ảnh cá nhân
        3 sửa tên
        4 sửa số điện thoại
         */
        String options[]={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone"};
        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
         builder.setTitle("Choose Action");
         //set các chức năng dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            // sử lý các item được chọn trong dialog
                if(which ==0){
                    //chỉnh profile clicked
                    pd.setMessage("Updating Profile Picture");
                    // changing cover profile picture,  make sure to assign same value
                    profileOrCoverPhoto="image";
                    showImagePicDialog();
                }
                else  if (which ==1){
                    //edit cover clicked
                    pd.setMessage("Updating Cover Photo");
                    // changing cover Photo,  make sure to assign same value
                    profileOrCoverPhoto="cover";
                    showImagePicDialog();
                }
                else  if (which ==2){
                    //edit Name clicked
                    pd.setMessage("Updating Name");
                    showNamePhoneUpdateDialog("name");
                }
                else  if (which ==3){
                    //edit Phone clicked
                    pd.setMessage("Updating Phone");
                    showNamePhoneUpdateDialog("phone");
                }
            }
        });
        //tạo và hiển thị dialog
        builder.create().show();
    }

    private void showNamePhoneUpdateDialog(final String key) {
        //custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Update"+key);//update name or update phone
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit text
        final EditText editText = new EditText(getActivity());
        editText.setHint("Enter"+key);// Edit name or edit phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button in dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result = new HashMap<>();
                    result.put(key,value);

                    databaseReference.child(user.getUid()).updateChildren(result)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                //update, dismiss progress
                                    pd.dismiss();
                                    Toast.makeText(getActivity(),"Update...",Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    //failed, dismiss progress,
                                     Toast.makeText(getActivity(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            });


                }
                else {
                    Toast.makeText(getActivity(),"Please Enter"+key,Toast.LENGTH_SHORT).show();
                }

            }
        });

        //add button in dialog to cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void showImagePicDialog() {
        //show dialog kết nói với camera  và thư mục chứa ảnh

        String options[]={"Camera","Gallery"};
        //alert dialog
        AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
        //set title
        builder.setTitle("Pick Image From");
        //set các chức năng dialog
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // sử lý các item được chọn trong dialog
                if(which ==0){
                    //Camera clicked
                if(!checkCameraPermission()){
                    requestCameraPermision();
                }
                else {
                    pickFromCamera();
                }
                }
                else  if (which ==1){
                    //Gallery clicked
                    if(!checkStoragePermission()){
                        requestStoragePermision();
                    }
                    else {
                        pickFromGallery();
                    }

                }

            }
        });
        //tạo và hiển thị dialog
        builder.create().show();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        /* this method called when user press allow or deny from permission request dialog
        here will handle permission cases (allowed & denied)
         */
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if(grantResults.length>0){
                    boolean camerAccepted = grantResults[0]==PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if(camerAccepted && writeStorageAccepted){
                        //permission enabled
                        pickFromCamera();
                    }
                    else {
                        //permission denied
                        Toast.makeText(getActivity(),"Please enable camera & storage",Toast.LENGTH_SHORT).show();
                    }
                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                    //picking from galler, first check if storage permission allowed or not

                if(grantResults.length>0){
                    boolean writeStorageAccepted = grantResults[1]==PackageManager.PERMISSION_GRANTED;
                    if( writeStorageAccepted){
                        //permission enabled
                        pickFromGallery();
                    }
                    else {
                        //permission denied
                        Toast.makeText(getActivity(),"Please enable storage",Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;

        }


    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        // this method will be called after picking image from Camera and Gallery
        if(resultCode == RESULT_OK){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is picked from gallery, get url of image
                image_uri=data.getData();

                uploadProfileCoverPhoto(image_uri);

            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is picked from camera, get uri of image

                uploadProfileCoverPhoto(image_uri);

            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {
        pd.show();
        //path and name of image to be stored in firebase storage
        String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+user.getUid();

        StorageReference storageReference2nd = storageReference.child(filePathAndName);
        storageReference2nd.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // image is upload to storage, now   get  it is uri and store in user's database
                        Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        Uri downloadUri = uriTask.getResult();

                        //check if image is upload or not and uri is received
                        if(uriTask.isSuccessful()){
                            //image uploaded
                            //add/update url in user's database
                            HashMap<String, Object> result = new HashMap<>();

                            result.put(profileOrCoverPhoto,downloadUri.toString());


                            databaseReference.child(user.getUid()).updateChildren(result)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // uri in database of user is added successfully
                                            //dimiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Image Uploaded...",Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            //error adding url in database of user
                                            //dismiss progress bar
                                            pd.dismiss();
                                            Toast.makeText(getActivity(),"Erroe Updating Image...",Toast.LENGTH_SHORT).show();


                                        }
                                    });
                        }
                        else {
                            //error
                            pd.dismiss();
                            Toast.makeText(getActivity(),"Some error occured",Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // there were some error(s), get and show error  message, dismiss progress dialog
                        pd.dismiss();
                        Toast.makeText(getActivity(),e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });

        }

    private void pickFromCamera() {
        //Intent of picking iamge from device camera
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE," Temp Pic");
        values.put(MediaStore.Images.Media.DESCRIPTION," Temp Description");
        //put image uri
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //intent to star camera
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,image_uri);
        startActivityForResult(cameraIntent,IMAGE_PICK_CAMERA_CODE);

    }


    private void pickFromGallery() {


        //pick from gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);


    }

}