package com.example.todolite;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class NoteDetailsActivity extends AppCompatActivity {

    private NoteAdapter noteAdapter;
    private int noteId;
    private String noteText;
    private EditText noteEditText;
    private boolean isEditing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_details);

        noteEditText = findViewById(R.id.noteEditText);

        noteId = getIntent().getIntExtra("noteId", -1);
        noteText = getIntent().getStringExtra("noteText");

        if (noteId == -1 || noteText == null) {
            Toast.makeText(this, "Ошибка загрузки заметки", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        noteEditText.setText(noteText);

        noteEditText.setEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            if (isEditing) {
                saveNote();
                item.setTitle("Edit");
            } else {
                item.setTitle("Save");
            }
            isEditing = !isEditing;

            noteEditText.setEnabled(isEditing);

            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("NotifyDataSetChanged")
    private void saveNote() {
        String updatedText = noteEditText.getText().toString();
        if (updatedText.isEmpty()) {
            Toast.makeText(this, "Текст не может быть пустым", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.URL_UPDATE_NOTE);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "note_id=" + URLEncoder.encode(String.valueOf(noteId), "UTF-8") +
                        "&note_text=" + URLEncoder.encode(updatedText, "UTF-8");

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    runOnUiThread(() -> {
                        noteText = updatedText;
                        Toast.makeText(NoteDetailsActivity.this, "Заметка обновлена", Toast.LENGTH_SHORT).show();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("noteId", noteId);
                        resultIntent.putExtra("updatedText", updatedText);
                        setResult(RESULT_OK, resultIntent);
                        noteAdapter.notifyDataSetChanged();
                        finish();
                    });
                } else {
                    runOnUiThread(() -> Toast.makeText(NoteDetailsActivity.this, "Ошибка обновления заметки", Toast.LENGTH_SHORT).show());
                }
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(NoteDetailsActivity.this, "Ошибка подключения", Toast.LENGTH_SHORT).show());
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
}
