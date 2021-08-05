package com.example.teacherapp;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;
import java.util.Objects;

public class Login extends AppCompatActivity {
    Button btn_login;
    EditText edt_uid, edt_pwd;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_login = findViewById(R.id.btn_login);
        edt_uid = findViewById(R.id.txt_uid);
        edt_pwd = findViewById(R.id.txt_pwd);

        collectionReference = db.collection("teacher");
        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        String uid = sharedPreferences.getString("uid", "");
        edt_uid.setText(uid);

        if (edt_uid.equals(null)) {
            edt_uid.requestFocus();
            if (edt_uid.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        } else {
            edt_pwd.requestFocus();
            if (edt_pwd.requestFocus()) {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
        }
    }

    public void checkCredentials(View view) {
        if (edt_uid.getText().toString().isEmpty() && edt_pwd.getText().toString().isEmpty())
            Toast.makeText(this, "Enter Teacher ID and Password.", Toast.LENGTH_SHORT).show();
        else if (edt_uid.getText().toString().isEmpty())
            Toast.makeText(this, "Enter Teacher ID.", Toast.LENGTH_SHORT).show();
        else if (edt_pwd.getText().toString().isEmpty())
            Toast.makeText(this, "Enter Password.", Toast.LENGTH_SHORT).show();
        else {
            collectionReference.get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            boolean flag = false;
                            for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                                if (Objects.equals(documentSnapshot.getString("teacherID"), edt_uid.getText().toString())
                                        && Objects.equals(documentSnapshot.getString("pass"), edt_pwd.getText().toString())) {
                                    TeacherDetails teacherDetails = (TeacherDetails) getApplicationContext();
                                    Toast.makeText(teacherDetails, "Welcome " +
                                            documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                                    teacherDetails.setTeacherID(documentSnapshot.getId());

                                    SharedPreferences.Editor editor = sharedPreferences.edit();

                                    editor.putString("uid", edt_uid.getText().toString());//txt_uid.getText().toString());
                                    editor.apply();
                                    flag=true;

                                    Intent mainActivity = new Intent(Login.this, MainActivity.class);
                                    startActivity(mainActivity);
                                }
                            }
                            if(!flag) {
                                Toast.makeText(Login.this, "Incorrect Credentials. Try again!", Toast.LENGTH_SHORT).show();
                                edt_pwd.setText("");
                                edt_pwd.requestFocus();
                                if (edt_pwd.requestFocus()) {
                                    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                                }
                            }
                        }
                    });
        }
    }
}