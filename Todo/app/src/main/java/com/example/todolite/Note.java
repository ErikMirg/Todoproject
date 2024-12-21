package com.example.todolite;

public class Note {
    private int id;
    private String note;
    private boolean enabled;

    public Note(int id, String note, boolean enabled) {
        this.id = id;
        this.note = note;
        this.enabled = enabled;
    }

    public int getId() {
        return id;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
