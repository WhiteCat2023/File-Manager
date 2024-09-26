package com.example.filemanager;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Objects;

public class NewTask extends AppCompatActivity {

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

        requestQueue = Volley.newRequestQueue(this);

        startDateInput.setOnClickListener(v -> {pickDate();});
        endDateInput.setOnClickListener(v -> {pickDate();});

        addTask.setOnClickListener(v -> {addTask();});
    }
    private void pickDate(){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                startDateInput.setText((month + 1) + "/" + dayOfMonth + "/" + year);
            }
        }, year, month, day);

        datePickerDialog.show();

    }
    private void addTask(){
        String titleInput = this.titleInput.getText().toString().trim();
        String descriptionInput = this.descriptionInput.getText().toString().trim();
        String startDateInput = Objects.requireNonNull(this.startDateInput.getText()).toString().trim();
        String endDateInput = Objects.requireNonNull(this.endDateInput.getText()).toString().trim();

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
        } catch (JSONException e) {
            Log.e("Login", "JSON error: " + e.getMessage());
            Toast.makeText(this, "Error creating JSON", Toast.LENGTH_SHORT).show();
        }

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    try{
                        JSONObject jsonResponse = new JSONObject(response);
                        String status = jsonResponse.getString("status");
                        if (status.equals("success")){
                            Toast.makeText(this, "Task added", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorMessage = jsonResponse.optString("message", "Authentication failed");
                            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }catch (JSONException e){
                        Log.e("Login", "JSON parsing error: " + e.getMessage());
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("Todo", "Volley error: " + error.getMessage(), error);
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