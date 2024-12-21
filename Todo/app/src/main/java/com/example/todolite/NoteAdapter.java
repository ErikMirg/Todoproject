package com.example.todolite;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    private ArrayList<Note> noteList;
    private Context context;
    private ToDoListActivity activity;

    public NoteAdapter(ArrayList<Note> noteList, Context context) {
        this.noteList = noteList;
        this.context = context;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = noteList.get(position);
        holder.noteText.setText(note.getNote());
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(!note.isEnabled());

        if (note.isEnabled()) {
            holder.noteText.setPaintFlags(holder.noteText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.noteText.setPaintFlags(holder.noteText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, NoteDetailsActivity.class);
            intent.putExtra("noteId", note.getId());
            intent.putExtra("noteText", note.getNote());
            context.startActivity(intent);
        });

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateNoteEnabled(note.getId(), isChecked);
            note.setEnabled(!isChecked);
            if (!isChecked) {
                holder.noteText.setPaintFlags(holder.noteText.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
            } else {
                holder.noteText.setPaintFlags(holder.noteText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            }
        });


        holder.deleteButton.setOnClickListener(v -> {
            deleteNoteFromServer(note);
            removeNoteAtPosition(position);
        });
    }

    private void removeNoteAtPosition(int position) {
        if (position >= 0 && position < noteList.size()) {
            noteList.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }


    void updateNoteEnabled(int noteId, boolean isEnabled) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.URL_UPDATE_NOTE_ENABLED);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "note_id=" + URLEncoder.encode(String.valueOf(noteId), "UTF-8") +
                        "&enabled=" + URLEncoder.encode(String.valueOf(isEnabled ? 1 : 0), "UTF-8");
                try (OutputStream os = urlConnection.getOutputStream();
                     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"))) {
                    writer.write(postData);
                }

                if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Log.i("ToDoListActivity", "Note enabled state updated successfully.");
                } else {
                    Log.e("ToDoListActivity", "Failed to update note enabled state.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }
    void deleteNoteFromServer(Note note) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(Constants.URL_DELETE_NOTE); // Укажите правильный URL
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");

                String postData = "note_id=" + URLEncoder.encode(String.valueOf(note.getId()), "UTF-8");
                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(postData);
                writer.flush();
                writer.close();
                os.close();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Успешное удаление
                } else {
                    // Обработка ошибок
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteText;
        CheckBox checkBox;
        ImageView deleteButton;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteText = itemView.findViewById(R.id.noteText);
            checkBox = itemView.findViewById(R.id.checkBox);
            deleteButton = itemView.findViewById(R.id.action_delete);
        }
    }
}
