package edu.eci.arep.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Parses an incoming HTTP/1.1 request from a socket's input stream, exposing the method, path, query parameters,
 * and body for downstream handlers.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class HttpRequest {

    private final String method;
    private final String path;
    private final Map<String, String> queryParams;
    private final String body;

    /**
     * Reads and parses the HTTP request from the given reader.
     *
     * @param reader a BufferedReader wrapping the socket's InputStream
     * @throws IOException if the underlying stream cannot be read
     */
    public HttpRequest(BufferedReader reader) throws IOException {
        String requestLine = reader.readLine();
        if (requestLine == null || requestLine.isBlank()) {
            this.method = "GET";
            this.path = "/";
            this.queryParams = new HashMap<>();
            this.body = "";
            return;
        }

        String[] parts = requestLine.split(" ");
        this.method = parts[0];

        String fullPath = parts.length > 1 ? parts[1] : "/";
        int queryIndex = fullPath.indexOf('?');
        if (queryIndex >= 0) {
            this.path = fullPath.substring(0, queryIndex);
            this.queryParams = parseQueryString(fullPath.substring(queryIndex + 1));
        } else {
            this.path = fullPath;
            this.queryParams = new HashMap<>();
        }

        // Consume headers
        int contentLength = 0;
        String headerLine;
        while ((headerLine = reader.readLine()) != null && !headerLine.isBlank()) {
            if (headerLine.toLowerCase().startsWith("content-length:")) {
                contentLength = Integer.parseInt(headerLine.split(":")[1].trim());
            }
        }

        // Read body if present
        if (contentLength > 0) {
            char[] buffer = new char[contentLength];
            reader.read(buffer, 0, contentLength);
            this.body = new String(buffer);
        } else {
            this.body = "";
        }
    }

    /**
     * Parses a URL query string into a key-value map.
     *
     * @param queryString the raw query string
     * @return a map of decoded parameter names to their values
     */
    private Map<String, String> parseQueryString(String queryString) {
        Map<String, String> params = new HashMap<>();
        for (String pair : queryString.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                params.put(kv[0], kv[1]);
            } else if (kv.length == 1) {
                params.put(kv[0], "");
            }
        }
        return params;
    }

    /**
     * Returns the HTTP method (e.g., GET, POST).
     *
     * @return the request method
     */
    public String getMethod() {
        return method;
    }

    /**
     * Returns the decoded request path without query string.
     *
     * @return the request path
     */
    public String getPath() {
        return path;
    }

    /**
     * Returns the value of a query parameter by name.
     *
     * @param name         the parameter name
     * @param defaultValue the value to return if the parameter is absent
     * @return the parameter value, or defaultValue if not present
     */
    public String getQueryParam(String name, String defaultValue) {
        return queryParams.getOrDefault(name, defaultValue);
    }

    /**
     * Returns the raw request body as a String.
     *
     * @return the request body, or an empty string if none
     */
    public String getBody() {
        return body;
    }
}
