package com.example.httpserver;

public class HeaderList {
    private final String name;
    private final String value;

    public HeaderList(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    // override
    public String toString() {
        return name + ": " + value;
    }
}
