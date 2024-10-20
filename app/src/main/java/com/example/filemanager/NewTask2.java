package com.example.filemanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.filemanager.Utils.ToDoListItem;
import com.example.filemanager.Utils.TodoDatabaseHelper;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class NewTask2 extends AppCompatActivity {
    final Calendar calendar = Calendar.getInstance();

    TextInputEditText editTextTaskName;
    TextInputEditText editTextDescription;
    TextInputEditText editTextStartDate;
    TextInputEditText editTextEndDate;
    Button buttonAddTask;
    ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task2);
// Inside NewTask2 Activity
        TodoDatabaseHelper dbHelper = new TodoDatabaseHelper(this);

        editTextTaskName = findViewById(R.id.newTask2TitleInput);
        editTextDescription = findViewById(R.id.newTask2DescriptionInput);
        editTextStartDate = findViewById(R.id.newTask2StartDate);
        editTextEndDate = findViewById(R.id.newTask2EndDate);
        buttonAddTask = findViewById(R.id.newTask2AddTask);
        back = findViewById(R.id.myTaskBack);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(NewTask2.this, MainActivity.class);
            startActivity(intent);
        });

        editTextStartDate.setOnClickListener(v -> {
            new DatePickerDialog(NewTask2.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateFormat = "MM/dd/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                    editTextStartDate.setText(sdf.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });

        editTextEndDate.setOnClickListener(v -> {
            new DatePickerDialog(NewTask2.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    String dateFormat = "MM/dd/yyyy";
                    SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
                    editTextEndDate.setText(sdf.format(calendar.getTime()));
                }
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
        });
        buttonAddTask.setOnClickListener(v -> {
            String taskName = editTextTaskName.getText().toString();
            String startDate = editTextStartDate.getText().toString();
            String endDate = editTextEndDate.getText().toString();
            String description = editTextDescription.getText().toString();

            // Check if all fields are filled
            if (taskName.isEmpty() || startDate.isEmpty() || endDate.isEmpty() || description.isEmpty()) {
                Toast.makeText(NewTask2.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            ToDoListItem newTask = new ToDoListItem(0, taskName, description, false, startDate, endDate);
            dbHelper.addTask(newTask);
            Toast.makeText(NewTask2.this, "Task added successfully!", Toast.LENGTH_SHORT).show();

            // Send result back to the fragment
            // Since this is an activity, not a fragment, we can't use getParentFragmentManager()
            // Instead, we can use an intent to send the result back to the previous activity
            Intent intent = new Intent();
            intent.putExtra("taskCreated", true);
            setResult(RESULT_OK, intent);

            finish();

        });
    }
}