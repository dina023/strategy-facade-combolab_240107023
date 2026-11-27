// ============================================================================
// TASK 02: Multi-Format Document Exporter
// Combines Strategy (serialization formats) + Facade (validate -> stamp -> write)
// ============================================================================
import java.util.LinkedHashMap;
import java.util.Map;

// Strategy Interface: Encapsulates document formatting algorithms
interface ExportStrategy {
    String format(Map<String, String> record);
}

// Concrete Strategy 1: Plain key-value string representation
class PlainTextExport implements ExportStrategy {
    public String format(Map<String, String> record) {
        return record.toString();
    }
}

// Concrete Strategy 2: Comma-Separated Values (CSV) format
class CsvExport implements ExportStrategy {
    public String format(Map<String, String> record) {
        return String.join(",", record.keySet()) + "\n" + String.join(",", record.values());
    }
}

// Concrete Strategy 3: Formatted Markdown report
class MarkdownExport implements ExportStrategy {
    public String format(Map<String, String> record) {
        StringBuilder sb = new StringBuilder("### Document Report\n");
        record.forEach((k, v) -> sb.append("- **").append(k).append("**: ").append(v).append("\n"));
        return sb.toString();
    }
}

// Subsystem 1: Validates document integrity before export
class ContentValidator {
    public boolean validate(Map<String, String> doc) {
        System.out.println("[ContentValidator] Validating document fields...");
        return doc != null && !doc.isEmpty();
    }
}

// Subsystem 2: Injects audit/export timestamps into the document
class MetadataStamp {
    public void stamp(Map<String, String> doc) {
        System.out.println("[MetadataStamp] Applying timestamp metadata...");
        doc.put("exportedAt", "2026-11-27T12:00:00Z");
    }
}

// Subsystem 3: Handles disk I/O persistence
class DiskWriter {
    public void write(String path, String content) {
        System.out.println("[DiskWriter] Writing to " + path + ":\n" + content);
    }
}

// Facade: Coordinates validation, stamping, strategy formatting, and disk writing
class DocumentExportFacade {
    private final ContentValidator validator = new ContentValidator();
    private final MetadataStamp stamper = new MetadataStamp();
    private final DiskWriter writer = new DiskWriter();
    private ExportStrategy strategy;

    public DocumentExportFacade(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    // Unified export pipeline
    public void exportReport(Map<String, String> document, String outputPath) {
        if (!validator.validate(document)) {
            throw new IllegalArgumentException("Invalid document");
        }
        stamper.stamp(document);
        String formatted = strategy.format(document);
        writer.write(outputPath, formatted);
    }
}

// Client Driver
public class Task02Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 02: Multi-Format Document Exporter ===");
        Map<String, String> data = new LinkedHashMap<>();
        data.put("Title", "Q4 Summary");
        data.put("Status", "Approved");

        DocumentExportFacade exporter = new DocumentExportFacade(new CsvExport());
        exporter.exportReport(data, "/reports/q4.csv");

        exporter.setStrategy(new MarkdownExport());
        exporter.exportReport(data, "/reports/q4.md");
    }
}
