package com.econovafx.ui.visual;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.stage.Stage;
import org.testfx.api.FxToolkit;
import org.testfx.util.WaitForAsyncUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for visual testing and screenshot capture
 */
public class VisualTestUtils {
    
    private static final String IMAGE_OUTPUT_DIR = "docs/images";
    
    /**
     * Captures a screenshot of the given node and saves it to docs/images
     */
    public static void captureNode(Node node, String filename) throws IOException {
        // Ensure output directory exists
        Path outputPath = Paths.get(IMAGE_OUTPUT_DIR);
        File outputDir = outputPath.toFile();
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        
        // Capture the node using JavaFX snapshot
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.TRANSPARENT);
        javafx.scene.image.WritableImage fxImage = node.snapshot(params, null);
        
        // Convert FX image to BufferedImage
        int width = (int) fxImage.getWidth();
        int height = (int) fxImage.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        
        int[] pixels = new int[width * height];
        fxImage.getPixelReader().getPixels(0, 0, width, height,
            javafx.scene.image.PixelFormat.getIntArgbInstance(), pixels, 0, width);
        
        image.setRGB(0, 0, width, height, pixels, 0, width);
        
        // Save to file
        File outputFile = new File(outputDir, filename + ".png");
        ImageIO.write(image, "png", outputFile);
        
        System.out.println("Screenshot saved: " + outputFile.getAbsolutePath());
    }
    
    /**
     * Sets up a stage with the given root node for visual testing
     */
    public static Scene setupScene(Stage stage, javafx.scene.Parent rootNode) {
        Scene scene = new Scene(rootNode, 1200, 800);
        stage.setScene(scene);
        stage.show();
        WaitForAsyncUtils.waitForFxEvents();
        return scene;
    }
}
