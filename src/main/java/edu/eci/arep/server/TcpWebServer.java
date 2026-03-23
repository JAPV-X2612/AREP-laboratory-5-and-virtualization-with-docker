package edu.eci.arep.server;

import edu.eci.arep.controller.GreetingController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Concurrent TCP web server that accepts incoming connections on the configured
 * port and dispatches each one to a thread-pool worker via a ClientHandler.
 *
 * The server supports graceful shutdown: calling stop() closes the
 * ServerSocket, causing the accept loop to exit, and then waits for in-flight
 * requests to complete before the thread pool terminates.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class TcpWebServer {

    private static final Logger logger = LoggerFactory.getLogger(TcpWebServer.class);
    private static final int THREAD_POOL_SIZE = 10;
    private static final int SHUTDOWN_TIMEOUT_SECONDS = 30;

    private final int port;
    private final ExecutorService threadPool;
    private final GreetingController greetingController;

    private volatile boolean running;
    private ServerSocket serverSocket;

    /**
     * Constructs a TcpWebServer bound to the given port.
     *
     * @param port the TCP port to listen on
     */
    public TcpWebServer(int port) {
        this.port = port;
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.greetingController = new GreetingController();
        this.running = false;
    }

    /**
     * Starts the server: opens the ServerSocket and begins accepting client
     * connections in a loop until stop() is called.
     *
     * @throws IOException if the ServerSocket cannot be opened on the configured port
     */
    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;
        logger.info("TcpWebServer started on port {}", port);

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                threadPool.submit(new ClientHandler(clientSocket, greetingController));
            } catch (SocketException e) {
                if (running) {
                    logger.error("Unexpected socket error: {}", e.getMessage());
                }
                // If not running, the socket was closed intentionally — exit loop cleanly.
            }
        }
    }

    /**
     * Stops the server gracefully: closes the ServerSocket to unblock the
     * accept loop, then shuts down the thread pool and waits for in-flight
     * handlers to finish.
     */
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            logger.warn("Error closing server socket: {}", e.getMessage());
        }

        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(SHUTDOWN_TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
                logger.warn("Thread pool forced shutdown after {} seconds.", SHUTDOWN_TIMEOUT_SECONDS);
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("TcpWebServer stopped.");
    }
}
