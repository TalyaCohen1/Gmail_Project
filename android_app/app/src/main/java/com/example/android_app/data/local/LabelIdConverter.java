package com.example.android_app.data.local;
import androidx.room.TypeConverter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LabelIdConverter {
    @TypeConverter
    public String fromList(List<String> list) {
        return list != null ? String.join(",", list) : "";
    }

    @TypeConverter
    public List<String> toList(String data) {
        return data != null && !data.isEmpty()
                ? Arrays.asList(data.split(","))
                : Collections.emptyList();
    }
}
