package com.example.filemanager.Todo_Tabs;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.filemanager.MainActivity;
import com.example.filemanager.R;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTask extends AppCompatActivity {

    final Calendar calendar = Calendar.getInstance();

    private static final String SHARED_PREF_NAME = "session";
    private static final String SESSION_TOKEN = "user_token";

    ImageView todoBack;
    EditText titleInput, descriptionInput;
    TextInputEditText startDateInput, endDateInput;
    Button addTask;
    RequestQueue requestQueue;

    String url = "https://skcalamba.scarlet2.io/android_api/todo/add_task_api.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        titleInput = findViewById(R.id.todoTitleInput);
        descriptionInput = findViewById(R.id.todoDescriptionInput);
        startDateInput = findViewById(R.id.startDate);
        endDateInput = findViewById(R.id.endDate);
        addTask = findViewById(R.id.addTask);
        todoBack = findViewById(R.id.todoBack);

        todoBack.setOnClickListener(v -> {
            Intent intent = new Intent(NewTask.this, MainActivity.class);
            startActivity(intent);
        });

        requestQueue = Volley.newRequestQueue(this);

        startDateInput.setOnClickListener(v -> {
            new DatePickerDialog(NewTask.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateFormat = "MM/dd/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                    startDateInput.setText(sdf.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        endDateInput.setOnClickListener(v -> {
            new DatePickerDialog(NewTask.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateFormat = "MM/dd/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
                    endDateInput.setText(sdf.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        addTask.setOnClickListener(v -> {addTask();});
    }

    private void addTask(){
        String titleInput = this.titleInput.getText().toString().trim();
        String descriptionInput = this.descriptionInput.getText().toString().trim();
        String startDateInput = this.startDateInput.getText().toString().trim();
        String endDateInput = this.endDateInput.getText().toString().trim();

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String token = sharedPreferences.getString(SESSION_TOKEN, "");

        if (titleInput.isEmpty() || descriptionInput.isEmpty()){
            Toast.makeText(this, "Please enter a title and description", Toast.LENGTH_SHORT).show();
            return;
        }
        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("title", titleInput);
            jsonBody.put("description", descriptionInput);
            jsonBody.put("startDate", startDateInput);
            jsonBody.put("endDate", endDateInput);
            jsonBody.put("type", "shared");
            jsonBody.put("created_by", token);
        } catch (JSONException e) {
            Log.e("SharedTask", "JSON error: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try{
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("success")){
                            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
//                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new SharedTask()).commit();
                            startActivity(new Intent(this, MainActivity.class));
                        } else {
                            String errorMessage = jsonResponse.optString("message", "Adding task failed");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){
                        Log.e("SharedTask", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Creating task failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("SharedTask", "Volley error: " + error.getMessage(), error);
                    Toast.makeText(this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }){
            @Override
            public byte[] getBody() {
                return jsonBody.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };
        requestQueue.add(stringRequest);
    }
}