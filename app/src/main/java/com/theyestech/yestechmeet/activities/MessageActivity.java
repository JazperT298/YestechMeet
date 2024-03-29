package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.MessageAdapter;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.listeners.UsersListener;
import com.theyestech.yestechmeet.models.Chat;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.notifications.Data;
import com.theyestech.yestechmeet.notifications.MyResponse;
import com.theyestech.yestechmeet.notifications.Sender;
import com.theyestech.yestechmeet.notifications.Token;
import com.theyestech.yestechmeet.services.ApiService;
import com.theyestech.yestechmeet.services.ApiClient;
import com.theyestech.yestechmeet.services.Client;
import com.theyestech.yestechmeet.utils.Constants;
import com.theyestech.yestechmeet.utils.Debugger;
import com.theyestech.yestechmeet.utils.GlideOptions;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity implements UsersListener {
    private Context context;
    //Widgets
    private ImageView profile_image;
    private TextView username;
    private ImageView btn_send, iv_Back, iv_More, iv_Video, iv_File, iv_Audio;
    private EditText text_send;
    private RecyclerView recyclerView;
    private String role;

    //Array, Adapter, Variables
    private MessageAdapter messageAdapter;
    private ArrayList<Chat> chatArrayList;
    private String userid;
    private Date currentDate;
    private Chat selectedChat;

    private Intent intent;

    //Services
    private ApiService apiService;
    private ValueEventListener seenListener;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser fuser;
    private DatabaseReference reference;

    boolean notify = false;
    private UsersListener usersListener;
    private Users users;
    private String token;
    private Chat chat;

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
        setContentView(R.layout.activity_message);
        context = this;

        initializeUI();
        setUserHeader();
    }

    private void initializeUI() {
        //Firebase Database
        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        profile_image = findViewById(R.id.iv_ProfileEducatorImage);
        username = findViewById(R.id.username);
        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);
        iv_Back = findViewById(R.id.iv_Back);
        iv_More = findViewById(R.id.iv_More);
        iv_Video = findViewById(R.id.iv_Video);
        iv_Audio = findViewById(R.id.iv_Audio);
        iv_File = findViewById(R.id.iv_File);

        intent = getIntent();
        userid = intent.getStringExtra("userid");
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        firebaseAuth = FirebaseAuth.getInstance();
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        currentDate = Calendar.getInstance().getTime();

        iv_Back.setOnClickListener(v -> finish());
        profile_image.setOnClickListener(v -> openUsersProfile(userid));
        iv_More.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.delete_conversation, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.delete_conversation:
                        openDeleteChatDialog();
                        break;

                    case R.id.archive_conversation:
                        openArchiveDialog();
                        break;
                }
                return true;
            });
            popup.show();
        });
        iv_Video.setOnClickListener(v -> {
            if (users.getToken() == null || users.getToken().trim().isEmpty()) {
                Toast.makeText(context, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(context, OutgoingInvitationActivity.class);
                intent.putExtra("users", users);
                intent.putExtra("type", "video");
                startActivity(intent);
            }
        });
        iv_Audio.setOnClickListener(v -> {
            if (users.getToken() == null || users.getToken().trim().isEmpty()) {
                Toast.makeText(context, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
            } else {
                Intent intent = new Intent(context, OutgoingInvitationActivity.class);
                intent.putExtra("users", users);
                intent.putExtra("type", "audio");
                startActivity(intent);
            }
        });
        iv_File.setOnClickListener(v -> {
            openBottomSheetDialog();
        });


        btn_send.setOnClickListener(view -> {
            notify = true;
            String msg = text_send.getText().toString();
            if (!msg.equals("")) {
                sendUserMessage(fuser.getUid(), userid, msg, currentDate);
            } else {
                Toasty.warning(context, "You can't send empty message").show();
            }
            text_send.setText("");
        });

    }

    private void setUserHeader() {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                users = dataSnapshot.getValue(Users.class);
                username.setText(users.getName());
                if (users.getProfilePhoto().equals("default")) {
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_account)
                            .apply(GlideOptions.getOptions())
                            .into(profile_image);
                } else {
                    Glide.with(getApplicationContext())
                            .load(users.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(profile_image);
                }
                token = users.getToken();
                readUserMessages(fuser.getUid(), userid, users.getProfilePhoto());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        seenUserMessage(userid);
    }

    private void seenUserMessage(final String userid) {
        reference = FirebaseDatabase.getInstance().getReference("Chats");
        seenListener = reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiverId().equals(fuser.getUid()) && chat.getSenderId().equals(userid)) {
                        HashMap<String, Object> hashMap = new HashMap<>();
                        hashMap.put("isseen", true);
                        snapshot.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserMessage(String senderId, final String receiverId, String message, Date currentDate) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("senderId", senderId);
        hashMap.put("receiverId", receiverId);
        hashMap.put("message", message);
        hashMap.put("messageDateCreated", currentDate);
        hashMap.put("isseen", false);

        reference.child("Chats").push().setValue(hashMap);

        // add user to chat fragment
        final DatabaseReference chatRef = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(fuser.getUid())
                .child(userid);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(userid)
                .child(fuser.getUid());
        chatRefReceiver.child("id").setValue(fuser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (notify) {
                    //if (users.getName() == null){
                    sendUserNotification(receiverId, users.getName(), msg);
//                    }else{
//                        sendUserNotification(receiverId, users.getFullName(), msg);
//                    }
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void readUserMessages(final String myid, final String userid, final String imageurl) {
        chatArrayList = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatArrayList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiverId().equals(myid) && chat.getSenderId().equals(userid) ||
                            chat.getReceiverId().equals(userid) && chat.getSenderId().equals(myid)) {
                        chatArrayList.add(chat);
                    }

                    messageAdapter = new MessageAdapter(context, chatArrayList, imageurl);
                    messageAdapter.setClickListener(new OnClickRecyclerView() {
                        @Override
                        public void onItemClick(View view, int position, int fromButton) {
                            selectedChat = chatArrayList.get(position);
                            Debugger.logD("ID " + selectedChat.getMessage());
                            Snackbar.make(view, "Remove message?", Snackbar.LENGTH_LONG)
                                    .setActionTextColor(Color.GREEN)
                                    .setAction("Confirm", v -> {
                                        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
                                        Query messageQuery = ref.child("Chats").orderByChild("message").equalTo(selectedChat.getMessage());

                                        messageQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot1) {
                                                for (DataSnapshot chatSnapshot : dataSnapshot1.getChildren()) {
                                                    chatSnapshot.getRef().removeValue();
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {
                                            }
                                        });
                                    }).show();

                        }
                    });
                    recyclerView.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendUserNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.drawable.yessessionsicon, username + ": " + message, "New Message", currentDate,
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(@NotNull Call<MyResponse> call, @NotNull Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toasty.error(context, "Failed!").show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NotNull Call<MyResponse> call, @NotNull Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void openUsersProfile(final String userid) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Light_NoTitleBar);
        dialog.setContentView(R.layout.user_friend_profile);
        final ImageView iv_Image, iv_userImage, iv_Back;
        final TextView tv_username, tv_email;

        iv_Image = dialog.findViewById(R.id.iv_Image);
        iv_userImage = dialog.findViewById(R.id.iv_userImage);
        iv_Back = dialog.findViewById(R.id.iv_Back);
        tv_username = dialog.findViewById(R.id.tv_username);
        tv_email = dialog.findViewById(R.id.tv_email);

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                tv_username.setText(user.getUsername());
                tv_email.setText(user.getEmail());
                if (user.getProfilePhoto().equals("default")) {
                    //iv_ProfileImage.setImageResource(R.drawable.ai);
                    Glide.with(getApplicationContext()).load(R.drawable.ai).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ai)
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                } else {
                    //change this
                    Glide.with(getApplicationContext()).load(user.getProfilePhoto()).into(iv_Image);
                    Glide.with(getApplicationContext())
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_userImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        iv_Back.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void openDeleteChatDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Delete Conversation")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Are you sure you want to delete conversation?")
                .setPositiveButton("YES", (dialog1, which) -> {
                    // change this code beacuse your app will crash
                    DatabaseReference chatlist = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(fuser.getUid()).child(userid);
                    Query chatemessage = FirebaseDatabase.getInstance().getReference().child("Chats").orderByChild("receiverId").equalTo(userid);
                    chatemessage.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                                chatSnapshot.getRef().removeValue();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    chatlist.removeValue();
                    finish();
//                    startActivity(new Intent(context, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                })
                .setNegativeButton("NO", null)
                .create();
        dialog.show();
    }

    private void openArchiveDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Archive Conversation")
                .setIcon(R.drawable.ic_baseline_warning_24)
                .setMessage("Move conversation to archive?")
                .setPositiveButton("YES", (dialog1, which) -> {
                    // change this code beacuse your app will crash
                    DatabaseReference data = FirebaseDatabase.getInstance().getReference().child("Chatlist").child(fuser.getUid()).child(userid);
                    data.removeValue();
                    finish();
                })
                .setNegativeButton("NO", null)
                .create();
        dialog.show();
    }

    private void currentUser(String userid) {
        SharedPreferences.Editor editor = getSharedPreferences("PREFS", MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }

    private void status(String status) {
        reference = FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("status", status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
        setUserHeader();
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        status("offline");
        currentUser("none");
        setUserHeader();
    }

    @Override
    public void initiateVideoMeeting(Users users) {
        if (users.getToken() == null || users.getToken().trim().isEmpty()) {
            Toast.makeText(this, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "video");
            startActivity(intent);
        }
    }

    @Override
    public void initiateAudioMeeting(Users users) {
        if (users.getToken() == null || users.getToken().trim().isEmpty()) {
            Toast.makeText(this, users.getUsername() + " " + "is not available for meeting", Toast.LENGTH_LONG).show();
        } else {
            Intent intent = new Intent(context, OutgoingInvitationActivity.class);
            intent.putExtra("users", users);
            intent.putExtra("type", "audio");
            startActivity(intent);
        }
    }

    @Override
    public void onMultipleUsersAction(Boolean isMultipleUsersSelected) {
//        if (isMultipleUsersSelected) {
//            iv_Conference.setVisibility(View.VISIBLE);
//            iv_Conference.setOnClickListener(v -> {
//                Intent intent = new Intent(context, OutgoingInvitationActivity.class);
//                intent.putExtra("selectedUsers", new Gson().toJson(usersAdapter.getSelectedUsers()));
//                intent.putExtra("type", "video");
//                intent.putExtra("isMultiple", true);
//                startActivity(intent);
//            });
//        } else {
//            iv_Conference.setVisibility(View.GONE);
//        }
    }

    private void openBottomSheetDialog() {

        View view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_chose_photo, null);

        ConstraintLayout constraint1 = view.findViewById(R.id.constraint1);
        ConstraintLayout constraint2 = view.findViewById(R.id.constraint2);
        ImageView iv_Close = view.findViewById(R.id.iv_Close);
        constraint1.setOnClickListener(v -> {
            askCameraPermissions();
            bottomSheetDialog.dismiss();
        });
        constraint2.setOnClickListener(v -> {
            if (!checkStoragePermission()) {
                requestStoragePermission();
            } else {
                selectedFilePath = "";
                pickImageGallery();
            }
            bottomSheetDialog.dismiss();
        });
        iv_Close.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(view);

        bottomSheetDialog.show();
    }

    private void askCameraPermissions() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
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

    private void pickCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, CAMERA_REQUEST_CODE);//
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
                && data != null && data.getData() != null) {
            imageUri = data.getData();

//            iv_Images.setImageURI(imageUri);
//            Glide.with(getApplicationContext())
//                    .load(imageUri)
//                    .apply(GlideOptions.getOptions())
//                    .into(iv_userImages);

        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK
                && data != null && data.getData() != null) {

            Bitmap image = (Bitmap) data.getExtras().get("data");

            Random r = new Random();
            int randomNumber = r.nextInt(10000);
            selectedFilePath = String.valueOf(randomNumber);
            File filesDir = getApplicationContext().getFilesDir();
            myFile = new File(filesDir, selectedFilePath + ".jpg");

            //iv_UserProfileImage.setImageBitmap(image);
//            iv_Image.setImageBitmap(image);
//            Glide.with(context)
//                    .load(selectedFile)
//                    .apply(GlideOptions.getOptions())
//                    .into(iv_userImages);

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


}
