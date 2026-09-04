package com.example.dayflow;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.List;

public class JsonManager {
    private final Gson gson;
    private final File file;

    public JsonManager(Context context) {
        gson = new Gson();
        file = new File(context.getFilesDir(), "dayflow.json");
    }
    public void saveDays(List<Day> days) {

        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(days, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Day> loadDays() {
        if (!file.exists()) {
            return null;
        }
        try (FileReader reader = new FileReader(file)) {

            Type type = new TypeToken<List<Day>>() {}.getType();

            return gson.fromJson(reader, type);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}