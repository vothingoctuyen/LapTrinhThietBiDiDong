package com.example.admin.myapplication;

import android.support.annotation.NonNull;

/**
 * Created by Admin on 11/30/2018.
 */

public class Song implements Comparable<Song> {

    public String getTitle() {
        return title;
    }

    public String getFile() {
        return File;
    }

    private String title;
    private String File;

    public Song(String title, String file) {
        this.title = title;
        File = file;
    }

    @Override
    public int compareTo(@NonNull Song song) {
        return title.compareToIgnoreCase(song.title);
    }
}
