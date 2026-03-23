package edu.eci.arep.server;

/**
 * Utility class that builds well-formed HTTP/1.1 response strings for common
 * status codes and content types used by the HttpServer.
 *
 * All methods return the complete response including headers and body,
 * ready to be written directly to a socket output stream.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class HttpResponse {

    private static final String CRLF = "\r\n";

    /**
     * Builds a 200 OK response with a plain-text or HTML body.
     *
     * @param contentType   the MIME type of the body (e.g.,"text/html")
     * @param body          the response body as a String
     * @return the complete HTTP response string
     */
    public static String ok(String contentType, String body) {
        return "HTTP/1.1 200 OK" + CRLF
                + "Content-Type: " + contentType + CRLF
                + "Content-Length: " + body.getBytes().length + CRLF
                + "Connection: close" + CRLF
                + CRLF
                + body;
    }

    /**
     * Builds a 200 OK response with a binary body (e.g., PNG images).
     *
     * @param contentType the MIME type of the body (e.g., {@code "image/png"})
     * @param body        the response body as a byte array
     * @return the HTTP response header string; the caller must append the binary body separately
     */
    public static String okBinary(String contentType, byte[] body) {
        return "HTTP/1.1 200 OK" + CRLF
                + "Content-Type: " + contentType + CRLF
                + "Content-Length: " + body.length + CRLF
                + "Connection: close" + CRLF
                + CRLF;
    }

    /**
     * Builds a 404 Not Found response with a plain HTML error page.
     *
     * @param path the requested path that was not found
     * @return the complete HTTP 404 response string
     */
    public static String notFound(String path) {
        String body = "<html><body><h1>404 Not Found</h1><p>Resource not found: "
                + path + "</p></body></html>";
        return "HTTP/1.1 404 Not Found" + CRLF
                + "Content-Type: text/html" + CRLF
                + "Content-Length: " + body.getBytes().length + CRLF
                + "Connection: close" + CRLF
                + CRLF
                + body;
    }

    /**
     * Builds a 500 Internal Server Error response with a plain HTML error page.
     *
     * @param message a brief description of the error
     * @return the complete HTTP 500 response string
     */
    public static String internalError(String message) {
        String body = "<html><body><h1>500 Internal Server Error</h1><p>"
                + message + "</p></body></html>";
        return "HTTP/1.1 500 Internal Server Error" + CRLF
                + "Content-Type: text/html" + CRLF
                + "Content-Length: " + body.getBytes().length + CRLF
                + "Connection: close" + CRLF
                + CRLF
                + body;
    }
}
