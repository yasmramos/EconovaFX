package com.econovafx;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import javafx.application.Application;

/**
 * Entry point for the packaged application.
 *
 * <p>{@code mainClass} points here rather than at {@link App} so the JavaFX
 * runtime is not on the classpath when the launcher class is loaded. This is the
 * class GluonFX writes to its reflection configuration automatically; {@code App}
 * needs an explicit entry (see {@code pom.xml} and
 * {@code META-INF/native-image/reflect-config.json}).
 *
 * <p>A native Windows build is a windowed {@code .exe} with no console, so a
 * failure before the first window would be invisible. When
 * {@link Application#launch} propagates a startup failure, this writes it to
 * {@code ~/.econovafx/logs/startup-crash.log} (logback is not up yet at that
 * point, so the normal log file does not receive it) and rethrows.
 */
public class Launcher {

    private static final Path CRASH_LOG =
            Path.of(System.getProperty("user.home"), ".econovafx", "logs", "startup-crash.log");

    public static void main(String[] args) {
        try {
            Application.launch(App.class, args);
        } catch (Throwable t) {
            writeCrashLog(t);
            throw t;
        }
    }

    private static void writeCrashLog(Throwable failure) {
        StringWriter stack = new StringWriter();
        failure.printStackTrace(new PrintWriter(stack));
        String report = "EconovaFX failed to start at " + LocalDateTime.now()
                + System.lineSeparator() + System.lineSeparator() + stack;

        // The application cannot report the failure on screen (the toolkit may
        // never have started), so best-effort: console for a terminal run, file
        // for the packaged .exe.
        System.err.println(report);
        try {
            Files.createDirectories(CRASH_LOG.getParent());
            Files.writeString(CRASH_LOG, report, StandardCharsets.UTF_8);
        } catch (Exception ignored) {
            // Nothing left to do: the stack trace is already on stderr.
        }
    }
}
