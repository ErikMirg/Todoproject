package com.example.todolite;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ToDoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> noteList;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_list);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "Ошибка получения данных пользователя", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Button btnAdd = findViewById(R.id.addNoteButton);
        btnAdd.setOnClickListener(v -> addNewNote());

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteList = new ArrayList<>();
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        fetchNotes();

    }

    private void fetchNotes() {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.URL_GET_NOTES);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "user_id=" + URLEncoder.encode(String.valueOf(userId), "UTF-8");
                try (OutputStream os = urlConnection.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                }

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    StringBuilder response = new StringBuilder();
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                    }

                    JSONObject responseObject = new JSONObject(response.toString());
                    if (responseObject.getInt("success") == 1) {
                        JSONArray notesArray = responseObject.getJSONArray("notes");
                        runOnUiThread(() -> {
                            noteList.clear();
                            for (int i = 0; i < notesArray.length(); i++) {
                                try {
                                    JSONObject noteObject = notesArray.getJSONObject(i);
                                    Note note = new Note(
                                            noteObject.getInt("id"),
                                            noteObject.getString("note"),
                                            noteObject.getInt("enabled") == 1
                                    );
                                    noteList.add(note);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            noteAdapter.notifyDataSetChanged();
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(ToDoListActivity.this, "Задач не найдено", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(ToDoListActivity.this, "Ошибка загрузки задач", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ToDoListActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private void addNewNote() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Добавить новую задачу");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("Добавить", (dialog, which) -> {
            String newNote = input.getText().toString();
            if (!newNote.isEmpty()) {
                sendAddNoteRequest(newNote);
            } else {
                Toast.makeText(ToDoListActivity.this, "Текст задачи не может быть пустым", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Отмена", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendAddNoteRequest(String noteText) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.URL_ADD_NOTE);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "user_id=" + URLEncoder.encode(String.valueOf(userId), "UTF-8") +
                        "&note=" + URLEncoder.encode(noteText, "UTF-8");
                try (OutputStream os = urlConnection.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                }

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        Toast.makeText(ToDoListActivity.this, "Задача добавлена", Toast.LENGTH_SHORT).show();
                        fetchNotes();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(ToDoListActivity.this, "Ошибка добавления задачи", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(ToDoListActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            int noteId = data.getIntExtra("noteId", -1);
            String updatedText = data.getStringExtra("updatedText");

            if (noteId != -1 && updatedText != null) {
                for (Note note : noteList) {
                    if (note.getId() == noteId) {
                        note.setNote(updatedText);
                        noteAdapter.notifyDataSetChanged();
                        break;
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return true;
    }
}
