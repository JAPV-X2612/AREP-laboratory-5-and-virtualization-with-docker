package edu.eci.arep.app;

import edu.eci.arep.server.TcpWebServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Application entry point. Reads the server port from the PORT environment
 * variable (defaulting to 8080), starts the TcpWebServer, and registers a
 * JVM shutdown hook for graceful termination.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class TcpWebServerApp {

    private static final Logger logger = LoggerFactory.getLogger(TcpWebServerApp.class);
    private static final int DEFAULT_PORT = 8080;

    /**
     * Main method. Parses the PORT environment variable, instantiates the
     * server, registers a shutdown hook, and starts accepting connections.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        int port = resolvePort();
        TcpWebServer server = new TcpWebServer(port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received. Stopping server...");
            server.stop();
        }, "shutdown-hook"));

        try {
            server.start();
        } catch (IOException e) {
            logger.error("Failed to start TcpWebServer on port {}: {}", port, e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Resolves the server port from the PORT environment variable.
     * Falls back to DEFAULT_PORT if the variable is absent or unparseable.
     *
     * @return the port number to bind the server to
     */
    private static int resolvePort() {
        String envPort = System.getenv("PORT");
        if (envPort != null) {
            try {
                return Integer.parseInt(envPort);
            } catch (NumberFormatException e) {
                logger.warn("Invalid PORT value '{}', using default {}.", envPort, DEFAULT_PORT);
            }
        }
        return DEFAULT_PORT;
    }
}
