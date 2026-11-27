// ============================================================================
// TASK 11: Cloud Backup & Retention Manager
// Combines Strategy (backup scope) + Facade (scan -> encrypt -> upload -> purge)
// ============================================================================
import java.util.List;

// Strategy Interface: Determines which files are selected for backup
interface BackupStrategy {
    List<String> selectFiles(List<String> allFiles);
}

// Concrete Strategy 1: Backs up every file regardless of modification date
class FullBackupStrategy implements BackupStrategy {
    public List<String> selectFiles(List<String> allFiles) {
        System.out.println("[FullBackupStrategy] Selecting all " + allFiles.size() + " files.");
        return allFiles;
    }
}

// Concrete Strategy 2: Backs up only files changed since the last backup
class IncrementalBackupStrategy implements BackupStrategy {
    public List<String> selectFiles(List<String> allFiles) {
        System.out.println("[IncrementalBackupStrategy] Selecting only files modified since last run.");
        return List.of(allFiles.get(0));
    }
}

// Concrete Strategy 3: Backs up all files changed since the last Full backup
class DifferentialBackupStrategy implements BackupStrategy {
    public List<String> selectFiles(List<String> allFiles) {
        System.out.println("[DifferentialBackupStrategy] Selecting files modified since last Full backup.");
        return allFiles.subList(0, Math.min(2, allFiles.size()));
    }
}

// Subsystem 1: Scans directory tree for candidate files
class FileScanner {
    public List<String> scanDirectory(String directory) {
        System.out.println("[FileScanner] Scanning directory: " + directory);
        return List.of("db.sqlite", "config.yml", "assets.zip");
    }
}

// Subsystem 2: Encrypts archive payload with AES-256
class AesEncryptor {
    public byte[] encrypt(List<String> files) {
        System.out.println("[AesEncryptor] Encrypting " + files.size() + " file(s) with AES-256...");
        return new byte[512];
    }
}

// Subsystem 3: Uploads encrypted blob to remote object storage
class CloudStorageClient {
    public void upload(String bucketPath, byte[] encryptedPayload) {
        System.out.printf("[CloudStorageClient] Uploaded %d encrypted bytes to %s%n",
                encryptedPayload.length, bucketPath);
    }
}

// Subsystem 4: Enforces retention policy by deleting expired snapshots
class RetentionCleaner {
    public void purgeExpired(int retentionDays) {
        System.out.println("[RetentionCleaner] Purged snapshots older than " + retentionDays + " days.");
    }
}

// Facade: Orchestrates the complete cloud backup and cleanup lifecycle
class BackupFacade {
    private final FileScanner scanner = new FileScanner();
    private final AesEncryptor encryptor = new AesEncryptor();
    private final CloudStorageClient cloudClient = new CloudStorageClient();
    private final RetentionCleaner cleaner = new RetentionCleaner();
    private BackupStrategy strategy;

    public BackupFacade(BackupStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(BackupStrategy strategy) {
        this.strategy = strategy;
    }

    public void runBackupJob(String directory) {
        List<String> allFiles = scanner.scanDirectory(directory);
        List<String> targetFiles = strategy.selectFiles(allFiles);
        byte[] encrypted = encryptor.encrypt(targetFiles);
        cloudClient.upload("s3://company-backups" + directory, encrypted);
        cleaner.purgeExpired(30);
    }
}

// Client Driver
public class Task11Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 11: Cloud Backup & Retention Manager ===");
        BackupFacade backup = new BackupFacade(new FullBackupStrategy());
        backup.runBackupJob("/var/data");

        backup.setStrategy(new IncrementalBackupStrategy());
        backup.runBackupJob("/var/data");
    }
}
