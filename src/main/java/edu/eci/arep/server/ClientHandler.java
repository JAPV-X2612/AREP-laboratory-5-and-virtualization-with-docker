package edu.eci.arep.server;

import edu.eci.arep.controller.GreetingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Handles a single client connection in its own thread. Reads the HTTP
 * request, delegates to the appropriate controller or static-file resolver,
 * writes the response, and closes the socket.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private static final String WEBROOT = "webroot";

    private final Socket clientSocket;
    private final GreetingController greetingController;

    /**
     * Constructs a ClientHandler for the given socket.
     *
     * @param clientSocket      the accepted client socket
     * @param greetingController the controller that handles API requests
     */
    public ClientHandler(Socket clientSocket, GreetingController greetingController) {
        this.clientSocket = clientSocket;
        this.greetingController = greetingController;
    }

    /**
     * Processes the client request: parses it, routes it, and writes the response.
     * Always closes the client socket before returning.
     */
    @Override
    public void run() {
        try (
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            OutputStream output = clientSocket.getOutputStream()
        ) {
            HttpRequest request = new HttpRequest(reader);
            logger.info("[{}] {} {}", Thread.currentThread().getName(),
                    request.getMethod(), request.getPath());

            String path = request.getPath();

            if (path.startsWith("/api/")) {
                handleApiRequest(request, output);
            } else {
                handleStaticResource(path, output);
            }

        } catch (IOException e) {
            logger.error("Error handling client connection: {}", e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                logger.warn("Failed to close client socket: {}", e.getMessage());
            }
        }
    }

    /**
     * Routes an API request to the appropriate controller method and writes
     * the JSON or text response.
     *
     * @param request the parsed HTTP request
     * @param output  the socket output stream to write the response to
     * @throws IOException if writing to the stream fails
     */
    private void handleApiRequest(HttpRequest request, OutputStream output) throws IOException {
        String apiPath = request.getPath().substring(4); // strip "/api"
        String responseBody;

        switch (apiPath) {
            case "/greeting" -> responseBody = greetingController.greeting(
                    request.getQueryParam("name", "World"));
            default -> {
                String notFound = HttpResponse.notFound(request.getPath());
                output.write(notFound.getBytes(StandardCharsets.UTF_8));
                return;
            }
        }

        String response = HttpResponse.ok("application/json", responseBody);
        output.write(response.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Resolves and serves a static file from the webroot classpath directory.
     * Returns 404 if the file is not found, or 500 on read failure.
     *
     * @param requestPath the URL path of the requested resource
     * @param output      the socket output stream to write the response to
     * @throws IOException if writing to the stream fails
     */
    private void handleStaticResource(String requestPath, OutputStream output) throws IOException {
        String filePath = requestPath.equals("/") ? "/index.html" : requestPath;
        filePath = URLDecoder.decode(filePath, StandardCharsets.UTF_8);

        String resourcePath = WEBROOT + filePath;
        try (var stream = getClass().getClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                output.write(HttpResponse.notFound(requestPath).getBytes(StandardCharsets.UTF_8));
                return;
            }

            byte[] fileBytes = stream.readAllBytes();
            String contentType = resolveContentType(filePath);

            if (contentType.startsWith("text/")) {
                String header = HttpResponse.ok(contentType + "; charset=UTF-8",
                        new String(fileBytes, StandardCharsets.UTF_8));
                output.write(header.getBytes(StandardCharsets.UTF_8));
            } else {
                String header = HttpResponse.okBinary(contentType, fileBytes);
                output.write(header.getBytes(StandardCharsets.UTF_8));
                output.write(fileBytes);
            }

        } catch (IOException e) {
            logger.error("Failed to read static resource '{}': {}", resourcePath, e.getMessage());
            output.write(HttpResponse.internalError("Could not read resource: " + requestPath)
                    .getBytes(StandardCharsets.UTF_8));
        }
    }

    /**
     * Maps a file extension to its MIME content-type string.
     *
     * @param filePath the file path or name including extension
     * @return the MIME type string for the extension, or "application/octet-stream" if unknown
     */
    private String resolveContentType(String filePath) {
        if (filePath.endsWith(".html")) return "text/html";
        if (filePath.endsWith(".css"))  return "text/css";
        if (filePath.endsWith(".js"))   return "application/javascript";
        if (filePath.endsWith(".json")) return "application/json";
        if (filePath.endsWith(".png"))  return "image/png";
        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) return "image/jpeg";
        if (filePath.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }
}
