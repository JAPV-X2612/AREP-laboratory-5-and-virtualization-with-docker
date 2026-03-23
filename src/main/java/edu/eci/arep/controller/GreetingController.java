package edu.eci.arep.controller;

/**
 * REST-style controller that handles greeting-related API requests.
 * Methods in this class are invoked by the ClientHandler based on the
 * request path and query parameters.
 *
 * @author Jesús Pinzón
 * @version 1.0
 * @since 2026-03-16
 */
public class GreetingController {

    private static final String TEMPLATE = "{\"message\": \"Hello, %s!\"}";

    /**
     * Produces a JSON greeting message for the given name.
     *
     * @param name the name to include in the greeting
     * @return a JSON string with the greeting message
     */
    public String greeting(String name) {
        return String.format(TEMPLATE, name);
    }
}
