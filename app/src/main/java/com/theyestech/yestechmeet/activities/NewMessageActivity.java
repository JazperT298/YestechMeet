package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageTask;
import com.theyestech.yestechmeet.R;
import com.theyestech.yestechmeet.adapters.MessageAdapter;
import com.theyestech.yestechmeet.adapters.UserContactsAdapter;
import com.theyestech.yestechmeet.adapters.UserDropDownAdapter;
import com.theyestech.yestechmeet.interfaces.OnClickRecyclerView;
import com.theyestech.yestechmeet.models.Chat;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.notifications.Data;
import com.theyestech.yestechmeet.notifications.MyResponse;
import com.theyestech.yestechmeet.notifications.Sender;
import com.theyestech.yestechmeet.notifications.Token;
import com.theyestech.yestechmeet.services.ApiService;
import com.theyestech.yestechmeet.services.ApiClient;
import com.theyestech.yestechmeet.services.Client;
import com.theyestech.yestechmeet.utils.GlideOptions;
import com.theyestech.yestechmeet.utils.KeyboardHandler;

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

public class NewMessageActivity extends AppCompatActivity {
    private Context context;

    private EditText et_message;
    private TextView suggested;
    private ImageView iv_Back, btn_sends, iv_File, iv_Image, iv_Close;
    private AutoCompleteTextView et_SearchUser;

    private String role;
    private boolean doneSelecting;
    private String message;

    //Firebase
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private DatabaseReference threadRef;
    private DatabaseReference conversationRef;

    private String receiverId;
    private String senderId;
    private String threadId;
    private Date currentDate;
    private String conversationId;
    private ArrayList<Users> usersArrayList = new ArrayList<>();
    private UserDropDownAdapter userDropDownAdapter;
    private Users selectedUsers = new Users();
    boolean notify = false;

    private ApiService apiService;

    private ArrayList<Chat> chatArrayList;
    private DatabaseReference reference;
    private MessageAdapter messageAdapter;
    private RecyclerView rv_user;
    private RecyclerView rv_chat;
    private String userName = "", profileImage = "", calledBy = "", userid = "";

    private DatabaseReference usersRef, friendRequestRef, contactsRef;
    private String currentUserId;
    private UserContactsAdapter userContactsAdapter;
    private Users users;

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
        setContentView(R.layout.activity_new_message);
        context = this;
        usersArrayList = getIntent().getParcelableArrayListExtra("USERARRAYLIST");

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        doneSelecting = false;

        senderId = firebaseUser.getUid();

        apiService = Client.getClient("https://fcm.googleapis.com/").create(ApiService.class);

        initializeUI();
        readAllUsers();
    }

    private void initializeUI() {
        iv_Back = findViewById(R.id.iv_Back);
        et_SearchUser = findViewById(R.id.et_SearchUser);
        rv_user = findViewById(R.id.rv_user);
        rv_chat = findViewById(R.id.rv_chat);
        iv_File = findViewById(R.id.iv_File);
        et_message = findViewById(R.id.text_send);
        btn_sends = findViewById(R.id.btn_send);
        iv_Image = findViewById(R.id.iv_Image);
        iv_Close = findViewById(R.id.iv_Close);
        suggested = findViewById(R.id.suggested);
        et_SearchUser.requestFocus();

        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();
        friendRequestRef = FirebaseDatabase.getInstance().getReference().child("Friend Request");
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        currentDate = Calendar.getInstance().getTime();

        rv_user.setHasFixedSize(true);
        rv_user.setLayoutManager(new LinearLayoutManager(context));
        rv_chat.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        rv_chat.setLayoutManager(linearLayoutManager);

        userDropDownAdapter = new UserDropDownAdapter(context, R.layout.listrow_chat_user_dropdown, usersArrayList);
        et_SearchUser.setAdapter(userDropDownAdapter);

        iv_Back.setOnClickListener(v -> finish());
        btn_sends.setOnClickListener(v -> {
            if (!doneSelecting) {
                Toasty.warning(context, "Please select one of your contacts.").show();
            } else {
                notify = true;
                String msg = et_message.getText().toString();
                if (!msg.equals("")) {
                    sendUserMessage(firebaseUser.getUid(), receiverId, msg, currentDate);
                } else {
                    Toasty.warning(context, "You can't send empty message").show();
                }
                et_message.setText("");
            }
        });

        iv_Close.setOnClickListener(v -> {
            rv_chat.setVisibility(View.GONE);
            readAllUsers();
            et_SearchUser.setText("");
            iv_Image.setImageResource(R.drawable.ic_account);
        });

        et_SearchUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                readAllUsers();
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                userDropDownAdapter.getFilter().filter(charSequence);
                users = userDropDownAdapter.getItem(i);

                //searchUsers(charSequence.toString().toLowerCase());
            }

            @Override
            public void afterTextChanged(Editable editable) {
                readAllUsers();
            }
        });

        et_SearchUser.setOnItemClickListener((parent, view, position, id) -> {
            Object item = parent.getItemAtPosition(position);
            if (item instanceof Users) {
                selectedUsers = (Users) item;
                et_SearchUser.setText(selectedUsers.getName());
                receiverId = selectedUsers.getId();
                doneSelecting = true;
                rv_chat.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(selectedUsers.getProfilePhoto())
                        .apply(GlideOptions.getOptions())
                        .into(iv_Image);
                readUserMessages(firebaseUser.getUid(), receiverId, selectedUsers.getProfilePhoto());
            }

            KeyboardHandler.closeKeyboard(et_SearchUser, context);
        });
        iv_File.setOnClickListener(v -> openBottomSheetDialog());
    }

    private void readAllUsers() {
        rv_user.setVisibility(View.VISIBLE);
        FirebaseRecyclerOptions<Users> options = new FirebaseRecyclerOptions.Builder<Users>()
                .setQuery(contactsRef.child(currentUserId), Users.class).build();

        FirebaseRecyclerAdapter<Users, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull Users model) {
                final String listUserId = getRef(position).getKey();

                usersRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            userid = snapshot.child("id").getValue().toString();
                            userName = snapshot.child("name").getValue().toString();
                            profileImage = snapshot.child("profilePhoto").getValue().toString();

                            holder.username.setText(userName);
                            Glide.with(getApplicationContext())
                                    .load(profileImage)
                                    .apply(GlideOptions.getOptions())
                                    .into(holder.profile_image);
                            //holder.date.setText();
                            holder.constraint.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    //openUsersProfile(listUserId);
                                    displayUser(listUserId);
                                    KeyboardHandler.closeKeyboard(et_SearchUser, context);
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggested_user_item, parent, false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };
        rv_user.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    private static class ContactsViewHolder extends RecyclerView.ViewHolder {
        private TextView username;
        private ImageView profile_image;
        private ImageView img_on;
        private ImageView img_off;
        private ConstraintLayout constraint;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            img_on = itemView.findViewById(R.id.img_on);
            img_off = itemView.findViewById(R.id.img_off);
            constraint = itemView.findViewById(R.id.constraint);

        }
    }

    private void displayUser(final String userid) {
        rv_user.setVisibility(View.GONE);
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);

                et_SearchUser.setText(user.getUsername());
//                if (user.getProfilePhoto().equals("default")) {
//                    //iv_ProfileImage.setImageResource(R.drawable.ai);
//                    Glide.with(getApplicationContext()).load(R.drawable.ai).into(iv_Image);
//                } else {
//                    //change this
//                    Glide.with(getApplicationContext())
//                            .load(user.getProfilePhoto())
//                            .apply(GlideOptions.getOptions())
//                            .into(iv_Image);
//                }
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
                .child(firebaseUser.getUid())
                .child(receiverId);

        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    chatRef.child("id").setValue(receiverId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final DatabaseReference chatRefReceiver = FirebaseDatabase.getInstance().getReference("Chatlist")
                .child(receiverId)
                .child(firebaseUser.getUid());
        chatRefReceiver.child("id").setValue(firebaseUser.getUid());

        final String msg = message;

        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users users = dataSnapshot.getValue(Users.class);
                if (notify) {
                    sendUserNotification(receiverId, users.getName(), msg);
                }
                notify = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        finish();
    }

    private void sendUserNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(firebaseUser.getUid(), R.drawable.session_logo, username + ": " + message, "New Message", currentDate,
                            receiverId);

                    Sender sender = new Sender(data, token.getToken());

                    apiService.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200) {
                                        if (response.body().success != 1) {
                                            Toast.makeText(context, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
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
                    Chat chat = snapshot.getValue(Chat.class);
                    if (chat.getReceiverId().equals(myid) && chat.getSenderId().equals(userid) ||
                            chat.getReceiverId().equals(userid) && chat.getSenderId().equals(myid)) {
                        chatArrayList.add(chat);

                    }
                    messageAdapter = new MessageAdapter(context, chatArrayList, imageurl);
                    rv_chat.setAdapter(messageAdapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void status(String status) {
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

    private void openBottomSheetDialog() {

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
//
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

//            //iv_UserProfileImage.setImageBitmap(image);
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