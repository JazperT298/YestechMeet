package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;

import es.dmoral.toasty.Toasty;

public class ProfileActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private ImageView iv_Image, iv_userImage, iv_Back,iv_Images, iv_userImages,iv_UserDetailsCamera;
    private TextInputEditText et_username;
    private AppCompatButton appCompatButton;
    private TextView tv_username, tv_email;
    private NestedScrollView nested_content;
    private ConstraintLayout nested_content2;

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private static final int STORAGE_REQUEST_CODE = 400;
    private static final int IMAGE_PICK_GALLERY_CODE = 1000;

    private static final int VIDEO_PERMISSION_CODE = 2000;
    private static final int VIDEO_REQUEST_CODE = 3000;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int DOCUMENT_PERMISSION_CODE = 103;
    private static final int DOCUMENT_REQUEST_CODE = 104;

    private static final int IMAGE_REQUEST = 1;
    private Uri imageUri;
    private StorageTask uploadTask;
    private String storagePermission[];
    private String cameraPermission[];
    private Uri selectedFile;
    private String selectedFilePath = "";
    private String displayName = "";
    private File myFile;

    private BottomSheetDialog bottomSheetDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        context = this;

        initializeUI();
    }

    private void initializeUI(){
        iv_Image = findViewById(R.id.iv_Image);
        iv_userImage = findViewById(R.id.iv_userImage);
        iv_Back = findViewById(R.id.iv_Back);
        iv_Images = findViewById(R.id.iv_Images);
        iv_userImages = findViewById(R.id.iv_userImages);
        iv_UserDetailsCamera = findViewById(R.id.iv_UserDetailsCamera);
        et_username = findViewById(R.id.et_username);

        appCompatButton = findViewById(R.id.appCompatButton);
        tv_username = findViewById(R.id.tv_username);
        tv_email = findViewById(R.id.tv_email);
        nested_content = findViewById(R.id.nested_content);
        nested_content2 = findViewById(R.id.nested_content2);

        storageReference = FirebaseStorage.getInstance().getReference("uploads");
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                tv_username.setText(user.getUsername());
                tv_email.setText(user.getEmail());
                et_username.setHint(user.getUsername());
                et_username.setText(user.getUsername());
                if (user.getProfilePhoto().equals("default")) {
                    //iv_ProfileImage.setImageResource(R.drawable.ai);
                    Glide.with(getApplicationContext()).load(R.drawable.ic_account).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_account)
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                    Glide.with(getApplicationContext()).load(R.drawable.ic_account).into(iv_Images);
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_account)
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImages);
                } else {
                    //change this
                    Glide.with(getApplicationContext()).load(user.getProfilePhoto()).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                    Glide.with(getApplicationContext()).load(user.getProfilePhoto()).into(iv_Images);
                    Glide.with(getApplicationContext())
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImages);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        iv_Back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        appCompatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(appCompatButton.getText().equals("Edit")){
                    nested_content.setVisibility(View.GONE);
                    nested_content2.setVisibility(View.VISIBLE);
                    appCompatButton.setText("SAVE");
                }else{
                    if (uploadTask != null && uploadTask.isInProgress()){
                        Toast.makeText(context, "Upload in progress", Toast.LENGTH_SHORT).show();
                    } else {
                        uploadImage();
                    }
                }

            }
        });
        iv_UserDetailsCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openBottomSheetDialog();
            }
        });

    }

    private void openBottomSheetDialog(){

        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_chose_photo, null);

        ConstraintLayout constraint1 = view.findViewById(R.id.constraint1);
        ConstraintLayout constraint2 = view.findViewById(R.id.constraint2);
        constraint1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
                bottomSheetDialog.dismiss();
            }
        });
        constraint2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkStoragePermission()) {
                    requestStoragePermission();
                } else {
                    selectedFilePath = "";
                    pickImageGallery();
                }
                bottomSheetDialog.dismiss();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(view);

        bottomSheetDialog.show();
    }

    private void askCameraPermissions(){
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }else  {
            pickCamera();
        }
    }

    private void requestStoragePermission() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE);
    }

    private boolean checkStoragePermission() {
        boolean result = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == (PackageManager.PERMISSION_GRANTED);
        return result;
    }

    private void pickImageGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_GALLERY_CODE);
    }

    private void pickCamera(){
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture,  CAMERA_REQUEST_CODE);//
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_REQUEST_CODE:
                if (grantResults.length > 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickImageGallery();
                    } else {
                        Toasty.error(context, "Permission denied ", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case CAMERA_PERMISSION_CODE:
                if (grantResults.length < 0) {
                    boolean writeStorageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if (writeStorageAccepted) {
                        pickCamera();
                    } else {
                        Toasty.error(context, "Permission denied ", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IMAGE_PICK_GALLERY_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null){
            imageUri = data.getData();

            iv_Images.setImageURI(imageUri);
            Glide.with(getApplicationContext())
                    .load(imageUri)
                    .apply(GlideOptions.getOptions())
                    .into(iv_userImages);

        }else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null){

            Bitmap image = (Bitmap) data.getExtras().get("data");

            Random r = new Random();
            int randomNumber = r.nextInt(10000);
            selectedFilePath = String.valueOf(randomNumber);
            File filesDir = getApplicationContext().getFilesDir();
            myFile = new File(filesDir, selectedFilePath + ".jpg");

            //iv_UserProfileImage.setImageBitmap(image);
            iv_Image.setImageBitmap(image);
            Glide.with(context)
                    .load(selectedFile)
                    .apply(GlideOptions.getOptions())
                    .into(iv_userImages);

            OutputStream os;
            try {
                os = new FileOutputStream(myFile);
                image.compress(Bitmap.CompressFormat.JPEG, 100, os);
                os.flush();
                os.close();
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), "Error writing bitmap", e);
            }
        }
    }

    private void openImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private String getFileExtension(Uri uri){
        ContentResolver contentResolver = context.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }

    private void uploadImage(){
        final ProgressDialog pd = new ProgressDialog(context);
        pd.setMessage("Saving");
        pd.show();

        if (imageUri != null){
            final StorageReference fileReference = storageReference.child(System.currentTimeMillis()
                    +"."+getFileExtension(imageUri));

            uploadTask = fileReference.putFile(imageUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                        throw  task.getException();
                    }

                    return  fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloadUri = task.getResult();
                        String mUri = downloadUri.toString();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
                        HashMap<String, Object> map = new HashMap<>();

                        map.put("name", et_username.getText().toString());
                        map.put("profilePhoto", ""+mUri);
                        map.put("search", et_username.getText().toString().toLowerCase());
                        map.put("username", et_username.getText().toString());


                        reference.updateChildren(map);

                        pd.dismiss();
                        Toasty.success(context, "Success!").show();
                        finish();
                    } else {
                        Toasty.warning(context, "Failed!").show();
                        pd.dismiss();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toasty.warning(context, e.getMessage()).show();
                    pd.dismiss();
                }
            });
        } else {
            Toasty.warning(context, "No image selected").show();
        }
    }

    private void status(String status){
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }

    @Override
    protected void onPause() {
        super.onPause();
        status("offline");
    }

}