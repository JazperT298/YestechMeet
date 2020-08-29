package com.theyestech.yestechmeet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.theyestech.yestechmeet.activities.ProfileActivity;
import com.theyestech.yestechmeet.activities.SearchContactActivity;
import com.theyestech.yestechmeet.activities.StartActivity;
import com.theyestech.yestechmeet.models.Users;
import com.theyestech.yestechmeet.utils.GlideOptions;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

public class MainActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private ImageView iv_Notification, iv_ProfileImage, iv_More, iv_Search, img_on, img_off;
    private String calledBy="";

    private FirebaseUser firebaseUser;
    private DatabaseReference reference,usersRef;
    private String currentUserId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTheme(R.style.AppTheme);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_chats, R.id.navigation_meeting, R.id.navigation_contacts)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        context = this;

        initializeUI();
    }

    private void initializeUI() {
        iv_Notification = findViewById(R.id.iv_Notification);
        iv_ProfileImage = findViewById(R.id.iv_ProfileImage);
        iv_More = findViewById(R.id.iv_More);
        iv_Search = findViewById(R.id.iv_Search);
        img_on = findViewById(R.id.img_on);
        img_off = findViewById(R.id.img_off);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Users user = dataSnapshot.getValue(Users.class);
                //username.setText(user.getUsername());
                assert user != null;
                if (user.getProfilePhoto().equals("default")) {
                    //iv_ProfileImage.setImageResource(R.drawable.ai);
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ai)
                            .apply(GlideOptions.getOptions())
                            .into(iv_ProfileImage);
                } else {
                    //change this
                    Glide.with(getApplicationContext())
                            .load(user.getProfilePhoto())
                            .apply(GlideOptions.getOptions())
                            .into(iv_ProfileImage);
                }
                img_on.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        iv_Notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(context, NotificationActivity.class);
                //startActivity(intent);
            }
        });
        iv_More.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(context, v);
            popup.getMenuInflater().inflate(R.menu.menu, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case  R.id.logout:
                        openLogoutDialog();
                        break;
                    case R.id.profile:
                        Intent intent = new Intent(context, ProfileActivity.class);
                        startActivity(intent);
                        break;
                }
                return true;
            });
            popup.show();
        });
        iv_Search.setOnClickListener(v -> {
            Intent intent = new Intent(context, SearchContactActivity.class);
            startActivity(intent);
        });
    }

    private void openLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("Logout")
                .setIcon(R.drawable.ic_logout_colored)
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("YES", (dialog1, which) -> {
                    FirebaseAuth.getInstance().signOut();
                    // change this code beacuse your app will crash
                    startActivity(new Intent(context, StartActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                })
                .setNegativeButton("NO", null)
                .create();
        dialog.show();
    }


}