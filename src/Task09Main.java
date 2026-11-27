// ============================================================================
// TASK 09: Adaptive Bitrate Video Streaming Player
// Combines Strategy (bitrate selection) + Facade (A/V sync & seamless quality switch)
// ============================================================================

// Strategy Interface: Determines video resolution from network bandwidth
interface BitrateStrategy {
    String selectResolution(double bandwidthMbps);
}

// Concrete Strategy 1: Caps resolution to conserve mobile data
class DataSaverStrategy implements BitrateStrategy {
    public String selectResolution(double bandwidthMbps) {
        return "480p (Data Saver)";
    }
}

// Concrete Strategy 2: Dynamically scales resolution based on bandwidth
class BalancedAutoStrategy implements BitrateStrategy {
    public String selectResolution(double bandwidthMbps) {
        if (bandwidthMbps >= 15.0) return "1080p FullHD";
        if (bandwidthMbps >= 5.0) return "720p HD";
        return "480p SD";
    }
}

// Concrete Strategy 3: Forces maximum resolution when available
class UltraHdStrategy implements BitrateStrategy {
    public String selectResolution(double bandwidthMbps) {
        return bandwidthMbps >= 25.0 ? "4K UltraHD" : "1440p QHD";
    }
}

// Subsystem 1: Measures network throughput and buffer health
class BufferMonitor {
    public double getAvailableBandwidthMbps() {
        System.out.println("[BufferMonitor] Measuring network throughput: 18.5 Mbps");
        return 18.5;
    }
}

// Subsystem 2: Decodes video frames at chosen resolution
class VideoDecoder {
    public void setStreamResolution(String streamUrl, String resolution) {
        System.out.printf("[VideoDecoder] Decoding %s at %s%n", streamUrl, resolution);
    }
}

// Subsystem 3: Keeps audio clock locked during video quality transitions
class AudioSyncEngine {
    public void maintainSync(long timestampMs) {
        System.out.printf("[AudioSyncEngine] Audio clock locked at %dms (zero interruption)%n", timestampMs);
    }
}

// Subsystem 4: Renders closed captions overlaid on video
class SubtitleRenderer {
    public void renderTrack(String lang) {
        System.out.println("[SubtitleRenderer] Rendering subtitles (" + lang + ")");
    }
}

// Facade: Manages playback and allows hot-swapping bitrate strategies mid-stream
class MediaPlayerFacade {
    private final BufferMonitor bufferMonitor = new BufferMonitor();
    private final VideoDecoder videoDecoder = new VideoDecoder();
    private final AudioSyncEngine audioSync = new AudioSyncEngine();
    private final SubtitleRenderer subtitles = new SubtitleRenderer();
    private BitrateStrategy strategy;
    private String currentStreamUrl;

    public MediaPlayerFacade(BitrateStrategy strategy) {
        this.strategy = strategy;
    }

    public void playStream(String streamUrl) {
        this.currentStreamUrl = streamUrl;
        double bw = bufferMonitor.getAvailableBandwidthMbps();
        String res = strategy.selectResolution(bw);
        videoDecoder.setStreamResolution(streamUrl, res);
        audioSync.maintainSync(0);
        subtitles.renderTrack("EN");
    }

    // Hot-swaps quality strategy without breaking A/V synchronization
    public void changeQualityMode(BitrateStrategy newStrategy) {
        this.strategy = newStrategy;
        double bw = bufferMonitor.getAvailableBandwidthMbps();
        String newRes = strategy.selectResolution(bw);
        videoDecoder.setStreamResolution(currentStreamUrl, newRes);
        audioSync.maintainSync(45200); // Preserves current playback timestamp
    }
}

// Client Driver
public class Task09Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 09: Adaptive Bitrate Video Streaming Player ===");
        MediaPlayerFacade player = new MediaPlayerFacade(new BalancedAutoStrategy());
        player.playStream("https://cdn.stream.io/movie_01.m3u8");

        System.out.println("--- Switching Quality Mode Mid-Playback ---");
        player.changeQualityMode(new DataSaverStrategy());
    }
}
