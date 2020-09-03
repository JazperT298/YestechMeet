package com.theyestech.yestechmeet.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.theyestech.yestechmeet.R;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private TextView sign_in;
    private TextInputEditText et_username, et_email, et_password;
    private Button btn_register;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.footerBackground));
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        initializeUI();
    }

    private void initializeUI() {

        auth = FirebaseAuth.getInstance();

        sign_in = findViewById(R.id.sign_in);
        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_register = findViewById(R.id.btn_register);
        progressDialog = new ProgressDialog(context);

        sign_in.setOnClickListener(v -> {
            Intent intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
        });

        btn_register.setOnClickListener(v -> {
            String txt_username = et_username.getText().toString();
            String txt_email = et_email.getText().toString();
            String txt_password = et_password.getText().toString();

            if (TextUtils.isEmpty(txt_username) || TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show();
            } else if (txt_password.length() < 6) {
                Toast.makeText(context, "password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(txt_username, txt_email, txt_password);
            }
        });

    }

    private void registerUser(final String username, final String email, String password) {
        progressDialog.setTitle("User Registration");
        progressDialog.setMessage("Please wait, while we are verifying your account");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        assert firebaseUser != null;
                        String userid = firebaseUser.getUid();

                        reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put("id", userid);
                        hashMap.put("username", username);
                        hashMap.put("name", username);
                        hashMap.put("email", email);
                        hashMap.put("profilePhoto", "default");
                        hashMap.put("state", "");
                        hashMap.put("status", "offline");
                        hashMap.put("search", username.toLowerCase());

                        reference.setValue(hashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(context, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            }
                        });
                    } else {
                        progressDialog.dismiss();
                        Toast.makeText(context, "You can't register with this email or password", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}