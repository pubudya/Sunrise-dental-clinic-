package com.sunrisedental.clinic.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for sending JSON responses from servlets.
 */
public final class JsonUtil {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, localDateAdapter())
            .registerTypeAdapter(LocalTime.class, localTimeAdapter())
            .create();

    private JsonUtil() {
    }

    public static Gson gson() {
        return GSON;
    }

    private static TypeAdapter<LocalDate> localDateAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, LocalDate value) throws IOException {
                out.value(value != null ? value.toString() : null);
            }

            @Override
            public LocalDate read(JsonReader in) throws IOException {
                String value = in.nextString();
                return value == null || value.isBlank() ? null : LocalDate.parse(value);
            }
        };
    }

    private static TypeAdapter<LocalTime> localTimeAdapter() {
        return new TypeAdapter<>() {
            @Override
            public void write(JsonWriter out, LocalTime value) throws IOException {
                out.value(value != null ? value.toString() : null);
            }

            @Override
            public LocalTime read(JsonReader in) throws IOException {
                String value = in.nextString();
                return value == null || value.isBlank() ? null : LocalTime.parse(value);
            }
        };
    }

    public static void writeJson(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.print(GSON.toJson(data));
        out.flush();
    }

    public static Map<String, Object> success(Object data) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", true);
        map.put("data", data);
        return map;
    }

    public static Map<String, Object> error(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", false);
        map.put("message", message);
        return map;
    }
}
