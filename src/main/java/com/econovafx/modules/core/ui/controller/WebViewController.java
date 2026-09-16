package com.econovafx.modules.core.ui.controller;

import com.econovafx.modules.core.ui.web.LocalWebServer;
import io.avaje.inject.Component;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for the Web UI view that embeds SvelteKit application in JavaFX WebView.
 * Loads the web interface from a local HTTP server to avoid CORS issues with ES modules.
 */
@Component
public class WebViewController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(WebViewController.class);

    private final LocalWebServer localWebServer;

    @FXML
    private WebView webView;

    private WebEngine webEngine;

    /**
     * Constructor with dependency injection.
     * @param localWebServer The local web server instance
     */
    @Inject
    public WebViewController(LocalWebServer localWebServer) {
        this.localWebServer = localWebServer;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing WebViewController");

        // Ensure the web server is started
        if (!localWebServer.isRunning()) {
            localWebServer.start();
        }

        // Get web engine and configure it
        webEngine = webView.getEngine();

        // Load the web UI from local server
        String url = localWebServer.getBaseUrl();
        logger.info("Loading web UI from {}", url);
        webEngine.load(url);

        // Set up load worker to handle page load events
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                logger.info("Web UI loaded successfully");
                injectJavaBridge();
            } else if (newState == Worker.State.FAILED) {
                logger.error("Failed to load web UI");
            }
        });

        // Enable JavaScript (enabled by default, but explicit for clarity)
        webEngine.setJavaScriptEnabled(true);
    }

    /**
     * Inject a Java bridge object into the JavaScript context.
     * This allows the SvelteKit app to call Java methods.
     */
    private void injectJavaBridge() {
        try {
            JSObject window = (JSObject) webEngine.executeScript("window");
            
            // Create a bridge object that JS can call
            JavaBridge bridge = new JavaBridge();
            window.setMember("javaBridge", bridge);
            
            logger.info("Java bridge injected into JavaScript context");
        } catch (Exception e) {
            logger.warn("Could not inject Java bridge: {}", e.getMessage());
        }
    }

    /**
     * Execute JavaScript code in the web view.
     * @param script The JavaScript code to execute
     * @return The result of the script execution, or null
     */
    public Object executeJavaScript(String script) {
        if (webEngine != null) {
            return webEngine.executeScript(script);
        }
        return null;
    }

    /**
     * Reload the web UI.
     */
    public void reload() {
        if (webEngine != null) {
            webEngine.reload();
        }
    }

    /**
     * Bridge class exposed to JavaScript for Java-JS communication.
     */
    public class JavaBridge {
        
        /**
         * Example method that can be called from JavaScript.
         * @param message Message from JS
         * @return Response message
         */
        public String sendMessage(String message) {
            logger.info("Message from JavaScript: {}", message);
            return "Java received: " + message;
        }

        /**
         * Get application version.
         * @return Version string
         */
        public String getVersion() {
            return "1.0.0";
        }

        /**
         * Log a message from JavaScript to Java logger.
         * @param level Log level (info, warn, error)
         * @param message Log message
         */
        public void log(String level, String message) {
            switch (level.toLowerCase()) {
                case "error":
                    logger.error("[JS] {}", message);
                    break;
                case "warn":
                    logger.warn("[JS] {}", message);
                    break;
                default:
                    logger.info("[JS] {}", message);
            }
        }
    }
}
