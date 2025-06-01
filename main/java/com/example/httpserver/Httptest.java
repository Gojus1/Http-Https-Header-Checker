package com.example.httpserver;


import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Httptest {

    public static List<String> getRawHeaders(String inputUrl) throws IOException {
        List<String> headers = new ArrayList<>();

        boolean isHttps = inputUrl.startsWith("https://");
        String cleanedUrl = inputUrl.replaceFirst("https?://", "").split("/")[0];
        int port = isHttps ? 443 : 80;

        BufferedReader in;
        PrintWriter out;

        if (isHttps) {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket socket = (SSLSocket) factory.createSocket(cleanedUrl, port);
            socket.startHandshake();
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } else {
            Socket socket = new Socket(cleanedUrl, port);
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        out.print("HEAD / HTTP/1.1\r\n");
        out.print("Host: " + cleanedUrl + "\r\n");
        out.print("Connection: close\r\n");
        out.print("\r\n");
        out.flush();

        String line;
        while ((line = in.readLine()) != null && !line.isEmpty()) {
            headers.add(line);
        }

        in.close();
        out.close();

        return headers;
    }
}


