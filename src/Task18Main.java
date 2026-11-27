// ============================================================================
// TASK 18: Security Log Ingestion & PII Redaction Pipeline
// Combines DUAL Strategies (LogParserStrategy + RedactionStrategy) + Facade
// ============================================================================
import java.util.ArrayList;
import java.util.List;

// Strategy Family 1: Parses raw log strings into structured event format
interface LogParserStrategy {
    String parse(String rawEvent);
}

class JsonLogParser implements LogParserStrategy {
    public String parse(String rawEvent) {
        return "PARSED_JSON(" + rawEvent + ")";
    }
}

class SyslogParser implements LogParserStrategy {
    public String parse(String rawEvent) {
        return "PARSED_SYSLOG(" + rawEvent + ")";
    }
}

// Strategy Family 2: Sanitizes Personally Identifiable Information (PII)
interface RedactionStrategy {
    String redact(String parsedEvent);
}

class MaskPiiStrategy implements RedactionStrategy {
    public String redact(String parsedEvent) {
        return parsedEvent.replaceAll("user=\\w+", "user=***MASKED***");
    }
}

class HashIdentifierStrategy implements RedactionStrategy {
    public String redact(String parsedEvent) {
        return parsedEvent.replaceAll("user=\\w+", "user=SHA256#9F8A");
    }
}

// Subsystem 1: Reads raw event stream
class StreamReader {
    public List<String> readEvents(List<String> rawEvents) {
        System.out.println("[StreamReader] Reading " + rawEvents.size() + " raw security events...");
        return rawEvents;
    }
}

// Subsystem 2: Validates parsed schema compliance
class SchemaValidator {
    public boolean isValid(String event) {
        return event != null && !event.isBlank();
    }
}

// Subsystem 3: Buffers sanitized records prior to network transmission
class BatchBuffer {
    private final List<String> buffer = new ArrayList<>();
    public void add(String record) { buffer.add(record); }
    public List<String> flush() {
        List<String> batch = new ArrayList<>(buffer);
        buffer.clear();
        return batch;
    }
}

// Subsystem 4: Forwards batches to SIEM with retry resilience
class SiemForwarder {
    private int attempts = 0;
    public boolean transmit(List<String> batch) {
        attempts++;
        if (attempts == 1) {
            System.out.println("[SiemForwarder] Transient network timeout on attempt 1...");
            return false;
        }
        System.out.println("[SiemForwarder] Successfully delivered " + batch.size() + " records: " + batch);
        return true;
    }
}

// Facade: Coordinates dual strategies, validation, buffering, and SIEM retry logic
class LogIngestionFacade {
    private final StreamReader reader = new StreamReader();
    private final SchemaValidator validator = new SchemaValidator();
    private final BatchBuffer buffer = new BatchBuffer();
    private final SiemForwarder forwarder = new SiemForwarder();
    private LogParserStrategy parserStrategy;
    private RedactionStrategy redactionStrategy;

    public LogIngestionFacade(LogParserStrategy parserStrategy, RedactionStrategy redactionStrategy) {
        this.parserStrategy = parserStrategy;
        this.redactionStrategy = redactionStrategy;
    }

    public void setStrategies(LogParserStrategy parser, RedactionStrategy redaction) {
        this.parserStrategy = parser;
        this.redactionStrategy = redaction;
    }

    public void ingestBatch(List<String> rawEvents) {
        for (String raw : reader.readEvents(rawEvents)) {
            String parsed = parserStrategy.parse(raw);
            if (validator.isValid(parsed)) {
                String sanitized = redactionStrategy.redact(parsed);
                buffer.add(sanitized);
            }
        }
        List<String> batch = buffer.flush();
        // Retry up to 3 times on transient SIEM failure
        for (int attempt = 1; attempt <= 3; attempt++) {
            if (forwarder.transmit(batch)) break;
        }
    }
}

// Client Driver
public class Task18Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 18: Security Log Ingestion & PII Redaction Pipeline ===");
        LogIngestionFacade pipeline = new LogIngestionFacade(new JsonLogParser(), new MaskPiiStrategy());
        pipeline.ingestBatch(List.of("{\"event\":\"login\",\"user=dina\"}", "{\"event\":\"sudo\",\"user=admin\"}"));

        pipeline.setStrategies(new SyslogParser(), new HashIdentifierStrategy());
        pipeline.ingestBatch(List.of("<34>Oct 11 sshd: user=dina accepted"));
    }
}
