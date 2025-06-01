package com.example.httpserver;

import java.util.List;

public class Console {
    public static void printHeaders(List<HeaderList> headers) {
        for (HeaderList h : headers) {
            System.out.println("- " + h);
        }
    }

    public static void printError(String message) {
        System.err.println("[ERROR] " + message);
    }
}
