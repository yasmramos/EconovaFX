package com.econovafx.modules.core.ui.web;

import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Local HTTP server for serving SvelteKit static web UI within JavaFX WebView.
 * Uses loopback address (127.0.0.1) with an ephemeral port to avoid CORS issues
 * with ES modules in JavaFX 17 WebView.
 */
public class LocalWebServer {

    private static final Logger logger = LoggerFactory.getLogger(LocalWebServer.class);

    private HttpServer server;
    private int port;
    private String baseUrl;
    private boolean running;

    // MIME type mapping for static assets
    private static final Map<String, String> MIME_TYPES = new HashMap<>();

    static {
        MIME_TYPES.put(".html", "text/html");
        MIME_TYPES.put(".htm", "text/html");
        MIME_TYPES.put(".css", "text/css");
        MIME_TYPES.put(".js", "text/javascript");
        MIME_TYPES.put(".json", "application/json");
        MIME_TYPES.put(".png", "image/png");
        MIME_TYPES.put(".jpg", "image/jpeg");
        MIME_TYPES.put(".jpeg", "image/jpeg");
        MIME_TYPES.put(".gif", "image/gif");
        MIME_TYPES.put(".svg", "image/svg+xml");
        MIME_TYPES.put(".ico", "image/x-icon");
        MIME_TYPES.put(".woff", "font/woff");
        MIME_TYPES.put(".woff2", "font/woff2");
        MIME_TYPES.put(".ttf", "font/ttf");
        MIME_TYPES.put(".eot", "application/vnd.ms-fontobject");
        MIME_TYPES.put(".xml", "application/xml");
        MIME_TYPES.put(".txt", "text/plain");
    }

    /**
     * Start the local web server on 127.0.0.1 with an ephemeral port.
     */
    public void start() {
        if (running) {
            logger.warn("LocalWebServer is already running");
            return;
        }

        try {
            // Bind to localhost with port 0 (ephemeral port)
            InetAddress loopback = InetAddress.getByName("127.0.0.1");
            server = HttpServer.create(new InetSocketAddress(loopback, 0), 0);

            port = server.getAddress().getPort();
            baseUrl = "http://127.0.0.1:" + port;

            logger.info("Starting local web server at {}", baseUrl);

            // Create context for serving static files
            server.createContext("/", exchange -> {
                String path = exchange.getRequestURI().getPath();

                // Remove leading slash
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }

                // Handle empty path or root
                if (path.isEmpty()) {
                    path = "index.html";
                }

                // Try to load the requested file from classpath
                String resourcePath = "/web/" + path;
                InputStream inputStream = getClass().getResourceAsStream(resourcePath);

                // If file not found and it's not a known asset extension, serve index.html (SPA fallback)
                if (inputStream == null && !isAssetExtension(path)) {
                    resourcePath = "/web/index.html";
                    inputStream = getClass().getResourceAsStream(resourcePath);
                }

                if (inputStream != null) {
                    // Determine MIME type
                    String mimeType = getMimeType(path);
                    exchange.getResponseHeaders().set("Content-Type", mimeType);

                    // Read and send response
                    byte[] content = inputStream.readAllBytes();
                    exchange.sendResponseHeaders(200, content.length);
                    exchange.getResponseBody().write(content);
                    inputStream.close();
                } else {
                    // File not found
                    String notFoundMsg = "404 Not Found: " + path;
                    exchange.sendResponseHeaders(404, notFoundMsg.length());
                    exchange.getResponseBody().write(notFoundMsg.getBytes());
                }

                exchange.close();
            });

            server.start();
            running = true;
            logger.info("Local web server started successfully on port {}", port);

        } catch (IOException e) {
            logger.error("Failed to start local web server", e);
            throw new RuntimeException("Failed to start local web server", e);
        }
    }

    /**
     * Stop the local web server.
     * @param delay Delay in seconds before stopping
     */
    public void stop(int delay) {
        if (server != null && running) {
            logger.info("Stopping local web server...");
            server.stop(delay);
            running = false;
            logger.info("Local web server stopped");
        }
    }

    /**
     * Get the assigned port number.
     * @return The port number, or -1 if not started
     */
    public int getPort() {
        return port;
    }

    /**
     * Get the base URL of the server.
     * @return The base URL, or null if not started
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Check if the server is running.
     * @return true if running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Get MIME type based on file extension.
     * @param path File path
     * @return MIME type string
     */
    private String getMimeType(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0) {
            String ext = path.substring(lastDot).toLowerCase();
            String mimeType = MIME_TYPES.get(ext);
            if (mimeType != null) {
                return mimeType;
            }
        }
        return "application/octet-stream";
    }

    /**
     * Check if the path has a known asset extension (not a route).
     * @param path File path
     * @return true if it's an asset file
     */
    private boolean isAssetExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        if (lastDot >= 0) {
            String ext = path.substring(lastDot).toLowerCase();
            return MIME_TYPES.containsKey(ext);
        }
        return false;
    }
}
