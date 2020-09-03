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

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.theyestech.yestechmeet.MainActivity;
import com.theyestech.yestechmeet.R;

public class LoginActivity extends AppCompatActivity {
    private View view;
    private Context context;
    private TextView sign_up, forgot_password;
    private TextInputEditText et_email, et_password;
    private Button btn_login;
    private ProgressDialog progressDialog;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
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
        progressDialog = new ProgressDialog(context);

        forgot_password = findViewById(R.id.forgot_password);
        sign_up = findViewById(R.id.sign_up);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        btn_login = findViewById(R.id.btn_login);
        btn_login.setOnClickListener(v -> {
            String txt_email = et_email.getText().toString();
            String txt_password = et_password.getText().toString();

            if (TextUtils.isEmpty(txt_email) || TextUtils.isEmpty(txt_password)) {
                Toast.makeText(LoginActivity.this, "All fields are required", Toast.LENGTH_SHORT).show();
            } else {
                progressDialog.setTitle("User Confirmation");
                progressDialog.setMessage("Please wait, while we are confirming your account");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                auth.signInWithEmailAndPassword(txt_email, txt_password)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                Intent intent = new Intent(context, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Authentication failed!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
        forgot_password.setOnClickListener(v -> {
            Intent intent = new Intent(context, ResetPasswordActivity.class);
            startActivity(intent);
        });
        sign_up.setOnClickListener(v -> {
            Intent intent = new Intent(context, RegisterActivity.class);
            startActivity(intent);
        });

    }
}