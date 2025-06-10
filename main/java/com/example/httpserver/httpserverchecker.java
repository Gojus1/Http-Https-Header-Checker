package com.example.httpserver;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Pattern;

public class httpserverchecker {

    public enum FilterType {
        INCLUDE,
        EXCLUDE,
        REGEX_INCLUDE,
        REGEX_EXCLUDE
    }


    public static class HeaderFilter {

        private final Set<String> exactHeaders;
        private final Set<Pattern> regexPatterns;
        private final FilterType filterType;

        private HeaderFilter(FilterType filterType) {
            this.filterType = filterType;
            this.exactHeaders = new HashSet<>();
            this.regexPatterns = new HashSet<>();
        }

        public void addExactHeader(String headerName) {
            String normalized = headerName.toLowerCase();
            exactHeaders.add(normalized);
        }

        public void addRegexPattern(String pattern) {
            Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
            regexPatterns.add(compiledPattern);
        }

        public boolean matches(String headerName) {
            final String normalizedHeaderName = headerName.toLowerCase();

            boolean matchExact = exactHeaders.contains(normalizedHeaderName);

            boolean matchRegex = regexPatterns.stream()
                    .anyMatch(p -> p.matcher(normalizedHeaderName).matches());

            return switch (filterType) {
                case INCLUDE -> matchExact;
                case EXCLUDE -> !matchExact;
                case REGEX_INCLUDE -> matchRegex;
                case REGEX_EXCLUDE -> !matchRegex;
            };
        }
    }

    public static HeaderFilter createHeaderFilter(FilterType filterType) {
        HeaderFilter newFilter = new HeaderFilter(filterType);
        return newFilter;
    }

    public static String fetchHeaders(String inputUrl, HeaderFilter filter) {

        StringBuilder result = new StringBuilder();

        try {
            boolean isHttps = inputUrl.startsWith("https://");

            String urlWithoutProtocol = inputUrl.replaceFirst("https?://", "");
            String cleanedUrl = urlWithoutProtocol.split("/")[0];

            int port = isHttps ? 443 : 80;


            BufferedReader in;
            PrintWriter out;


            if (isHttps) {
                SSLSocketFactory sslFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
                SSLSocket sslSocket = (SSLSocket) sslFactory.createSocket(cleanedUrl, port);
                sslSocket.startHandshake();

                OutputStream os = sslSocket.getOutputStream();
                OutputStreamWriter osw = new OutputStreamWriter(os);
                out = new PrintWriter(osw);

                InputStream is = sslSocket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                in = new BufferedReader(isr);

            } else {
                Socket socket = new Socket(cleanedUrl, port);

                out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }

            String headRequestLine = "HEAD / HTTP/1.1\r\n";
            String hostHeader = "Host: " + cleanedUrl + "\r\n";
            String connectionHeader = "Connection: close\r\n";

            out.print(headRequestLine);
            out.print(hostHeader);
            out.print(connectionHeader);
            out.print("\r\n");
            out.flush();


            String statusLine = in.readLine();
            if (statusLine != null) {
                result.append("Response: ").append(statusLine).append("\n\n");
                result.append("Headers:\n");
            }

            String line;
            while ((line = in.readLine()) != null && !line.isEmpty()) {
                int colonIndex = line.indexOf(':');
                if (colonIndex > 0) {
                    String headerName = line.substring(0, colonIndex).trim();
                    boolean shouldInclude = filter.matches(headerName);

                    if (shouldInclude) {
                        result.append("- ").append(line).append("\n");
                    }
                }
            }

            in.close();
            out.close();

        } catch (UnknownHostException e) {
            result.append("Error: Unknown host: ").append(e.getMessage());
        } catch (SSLHandshakeException e) {
            result.append("Error: SSL handshake: ").append(e.getMessage());
        } catch (IOException e) {
            result.append("Error: I/O: ").append(e.getMessage());
        } catch (Exception e) {
            result.append("Error: ").append(e.getMessage());
        }


//        try {
//            List<String> allLines = Httptest.getRawHeaders(inputUrl);
//
//            if (!allLines.isEmpty()) {
//                result.append("Response: ").append(allLines.get(0)).append("\n\nHeaders:\n");
//            }
//
//
//            for (String line : allLines) {
//                if (!line.contains(":")) continue;
//                String headerName = line.substring(0, line.indexOf(':')).trim();
//                if (filter.matches(headerName)) {
//                    result.append("- ").append(line).append("\n");
//                }
//            }
//        } catch (Exception e) {
//            result.append("Error: ").append(e.getMessage());
//        }

        return result.toString();
    }
}






