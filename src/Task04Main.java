// ============================================================================
// TASK 04: File Compression & Archival Utility
// Combines Strategy (compression algorithm) + Facade (read -> hash -> pack)
// ============================================================================
import java.util.List;

// Strategy Interface: Encapsulates data compression algorithms
interface CompressionStrategy {
    byte[] compress(byte[] rawData);
}

// Concrete Strategy 1: Run-Length Encoding compression
class RunLengthCompression implements CompressionStrategy {
    public byte[] compress(byte[] rawData) {
        System.out.println("[RunLengthCompression] Compressing with RLE...");
        return new byte[rawData.length / 2]; // Simulated 50% reduction
    }
}

// Concrete Strategy 2: Dictionary-based (LZW) compression
class DictionaryCompression implements CompressionStrategy {
    public byte[] compress(byte[] rawData) {
        System.out.println("[DictionaryCompression] Compressing with LZW dictionary...");
        return new byte[rawData.length / 3]; // Simulated 66% reduction
    }
}

// Concrete Strategy 3: Raw archival without compression
class NoCompression implements CompressionStrategy {
    public byte[] compress(byte[] rawData) {
        System.out.println("[NoCompression] Storing raw bytes without compression...");
        return rawData;
    }
}

// Subsystem 1: Reads source files from disk into memory
class FileReader {
    public byte[] readAll(List<String> files) {
        System.out.println("[FileReader] Reading " + files.size() + " source files...");
        return new byte[1024]; // Simulated payload
    }
}

// Subsystem 2: Generates SHA-256 checksum for data integrity
class ChecksumGenerator {
    public String generateSha256(byte[] data) {
        String hash = "SHA256-" + data.length;
        System.out.println("[ChecksumGenerator] Integrity hash: " + hash);
        return hash;
    }
}

// Subsystem 3: Bundles compressed bytes and checksum into final archive file
class ArchivePackager {
    public void pack(String destination, byte[] compressedData, String checksum) {
        System.out.printf("[ArchivePackager] Saved %d bytes to %s (Checksum: %s)%n",
                compressedData.length, destination, checksum);
    }
}

// Facade: Provides one-step archive creation while allowing compression swaps
class ArchiveFacade {
    private final FileReader reader = new FileReader();
    private final ChecksumGenerator checksumGen = new ChecksumGenerator();
    private final ArchivePackager packager = new ArchivePackager();
    private CompressionStrategy strategy;

    public ArchiveFacade(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public void createArchive(List<String> sourceFiles, String destination) {
        byte[] raw = reader.readAll(sourceFiles);
        String hash = checksumGen.generateSha256(raw);
        byte[] compressed = strategy.compress(raw);
        packager.pack(destination, compressed, hash);
    }
}

// Client Driver
public class Task04Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 04: File Compression & Archival Utility ===");
        ArchiveFacade archiver = new ArchiveFacade(new DictionaryCompression());
        archiver.createArchive(List.of("a.txt", "b.txt"), "backup.arc");

        archiver.setStrategy(new NoCompression());
        archiver.createArchive(List.of("video.mp4"), "media.arc");
    }
}
