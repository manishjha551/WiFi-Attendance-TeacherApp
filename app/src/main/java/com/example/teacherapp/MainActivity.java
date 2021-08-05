package com.example.teacherapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    TextView txt_display;
    EditText edt_code;
    Button btn_submit, btn_random;
    String selectedLecture;
    Spinner lectureSpinner;
    String ssid = "", bssid = "";
    String uid;
    SharedPreferences sharedPreferences;

    private static final int LOCATION = 1;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_display = findViewById(R.id.txt_display);
        edt_code = findViewById(R.id.edt_code);
        btn_submit = findViewById(R.id.btn_submit);
        btn_random = findViewById(R.id.btn_generatePin);

        sharedPreferences = getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);

        TeacherDetails teacherDetails = (TeacherDetails) getApplicationContext();
        uid = teacherDetails.getTeacherID();

        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("Select Lecture");
        collectionReference = db.collection("teacher/" + uid + "/subject");
        collectionReference.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot documentSnapshot : Objects.requireNonNull(task.getResult())) {
                            arrayList.add(documentSnapshot.getId());
//                            Toast.makeText(teacherDetails, documentSnapshot.getId(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        lectureSpinner = findViewById(R.id.spinner);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, arrayList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        lectureSpinner.setAdapter(arrayAdapter);
        lectureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedLecture = parent.getItemAtPosition(position).toString();
                if (!selectedLecture.equals("Select Lecture")) {
                    if (selectedLecture.equals("CI-1"))
                        selectedLecture = "CI";
//                    Toast.makeText(parent.getContext(), "Selected: " + selectedLecture, Toast.LENGTH_SHORT).show();
                    edt_code.setVisibility(View.VISIBLE);
                    btn_submit.setVisibility(View.VISIBLE);
                    btn_random.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        collectionReference = db.collection("attendance");
        tryToReadSSID();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == LOCATION)
            tryToReadSSID();
    }

    @SuppressLint("SetTextI18n")
    private void tryToReadSSID() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION);
        } else {
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                String str = "";
                ssid = wifiInfo.getSSID();
                bssid = wifiInfo.getBSSID();
            }
        }
    }

    public void setCode(View view) {
        if (edt_code.getText().length() < 4)
            Toast.makeText(this, "Enter a 4 digit code.", Toast.LENGTH_SHORT).show();
        else {
        lectureSpinner.setEnabled(false);
        edt_code.setEnabled(false);
        btn_submit.setVisibility(View.INVISIBLE);
        btn_random.setVisibility(View.INVISIBLE);

            LocalDate date = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            DocumentReference documentReference = collectionReference.document(date.format(formatter));

            Map<String, Object> code = new HashMap<>();
            code.put("code", edt_code.getText().toString());
            code.put("status", "0");
            if (!selectedLecture.equals("Select Lecture")) {
                code.put("lecture", selectedLecture);
                code.put("allowAttendance", "1");

                Toast.makeText(this, selectedLecture, Toast.LENGTH_SHORT).show();

                documentReference.set(code).
                        addOnSuccessListener(aVoid -> {

                            int uidCounter = 2019450001;
                            while (uidCounter <= 2019450060) {
                                HashMap<String, Object> field = new HashMap<>();
                                field.put("Attend", "0");

                                DocumentReference docRef = db.document("attendance/" +
                                        date.format(formatter) + "/subjects/" + selectedLecture
                                        + "/students/" + uidCounter++);

                                docRef.set(field);
                            }

                            Toast.makeText(MainActivity.this, "Code set as " + edt_code.getText().toString(), Toast.LENGTH_SHORT).show();
                            int time_in_seconds = 20;
                            new CountDownTimer(time_in_seconds * 1000, 1000) {
                                @SuppressLint("SetTextI18n")
                                public void onTick(long millisUntilFinished) {
                                    txt_display.setText("seconds remaining: " + millisUntilFinished / 1000);
                                }

                                @SuppressLint("SetTextI18n")
                                public void onFinish() {
                                    txt_display.setText("Select Lecture from spinner");
                                    Map<String, Object> code1 = new HashMap<>();
                                    code1.put("code", "00000");
                                    code1.put("allowAttendance", "0");
                                    documentReference.update(code1);
                                edt_code.setEnabled(true);
                                edt_code.setVisibility(View.INVISIBLE);
                                lectureSpinner.setEnabled(true);
                                }
                            }.start();
                        }).
                        addOnFailureListener(e -> {

                        });
            }
        }
    }

    public void generatePIN(View view) {
        edt_code.setText(String.valueOf((int) (Math.random() * 9000) + 1000));
    }

    public void setDefaultRouter(View view) {
        DocumentReference documentReference = collectionReference.document("routerDetails");
        Map<String, Object> wifiDetails = new HashMap<>();
        wifiDetails.put("ssid", ssid);
        wifiDetails.put("bssid", bssid);

        documentReference.update(wifiDetails)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(MainActivity.this, "Wi-Fi set as default WIFI.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void logout(View view) {
        TeacherDetails teacherDetails = (TeacherDetails) getApplicationContext();
        teacherDetails.setTeacherID(null);
        Toast.makeText(teacherDetails, "Logged Out!", Toast.LENGTH_SHORT).show();
        Intent login = new Intent(MainActivity.this, Login.class);
        finish();
        startActivity(login);
    }
}