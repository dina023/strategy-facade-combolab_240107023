// ============================================================================
// TASK 06: Image Filter & Thumbnail Pipeline
// Combines Strategy (pixel filters) + Facade (decode -> resize -> filter -> encode)
// ============================================================================

// Strategy Interface: Applies pixel transformations to a 2D image matrix
interface FilterStrategy {
    int[][] apply(int[][] pixels);
}

// Concrete Strategy 1: Converts pixels to monochrome grayscale
class GrayscaleFilter implements FilterStrategy {
    public int[][] apply(int[][] pixels) {
        System.out.println("[GrayscaleFilter] Converting pixel matrix to grayscale...");
        return pixels;
    }
}

// Concrete Strategy 2: Applies warm vintage sepia tone
class SepiaToneFilter implements FilterStrategy {
    public int[][] apply(int[][] pixels) {
        System.out.println("[SepiaToneFilter] Applying warm sepia tone matrix...");
        return pixels;
    }
}

// Concrete Strategy 3: Increases dynamic range contrast
class HighContrastFilter implements FilterStrategy {
    public int[][] apply(int[][] pixels) {
        System.out.println("[HighContrastFilter] Boosting pixel contrast levels...");
        return pixels;
    }
}

// Subsystem 1: Reads image file into a raw pixel matrix
class ImageDecoder {
    public int[][] decode(String inputPath) {
        System.out.println("[ImageDecoder] Decoding image from " + inputPath);
        return new int[100][100];
    }
}

// Subsystem 2: Scales pixel matrix to target thumbnail dimensions
class ThumbnailResizer {
    public int[][] resize(int[][] pixels, int width, int height) {
        System.out.printf("[ThumbnailResizer] Resizing to %dx%d%n", width, height);
        return new int[height][width];
    }
}

// Subsystem 3: Compresses and saves processed pixels to disk
class ImageEncoder {
    public void encode(int[][] pixels, String outputPath) {
        System.out.println("[ImageEncoder] Encoding and saving output to " + outputPath);
    }
}

// Facade: Coordinates the end-to-end image processing pipeline
class ImagePipelineFacade {
    private final ImageDecoder decoder = new ImageDecoder();
    private final ThumbnailResizer resizer = new ThumbnailResizer();
    private final ImageEncoder encoder = new ImageEncoder();
    private FilterStrategy strategy;

    public ImagePipelineFacade(FilterStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(FilterStrategy strategy) {
        this.strategy = strategy;
    }

    public void processImage(String inputPath, int width, int height) {
        int[][] raw = decoder.decode(inputPath);
        int[][] resized = resizer.resize(raw, width, height);
        int[][] filtered = strategy.apply(resized);
        encoder.encode(filtered, "thumb_" + inputPath);
    }
}

// Client Driver
public class Task06Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 06: Image Filter & Thumbnail Pipeline ===");
        ImagePipelineFacade pipeline = new ImagePipelineFacade(new GrayscaleFilter());
        pipeline.processImage("portrait.jpg", 150, 150);

        pipeline.setStrategy(new HighContrastFilter());
        pipeline.processImage("landscape.png", 300, 200);
    }
}
