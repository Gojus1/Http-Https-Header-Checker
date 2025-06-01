package com.example.httpserver;

import java.util.*;

public class Headers {

    public static Map<String, String> parse(List<String> lines) {
        Map<String, String> headers = new LinkedHashMap<>();

        for (String line : lines) {
            int colonIndex = line.indexOf(':');
            if (colonIndex > 0) {
                String key = line.substring(0, colonIndex).trim();
                String value = line.substring(colonIndex + 1).trim();
                headers.put(key, value);
            }
        }

        return headers;
    }
}
