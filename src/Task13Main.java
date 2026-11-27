// ============================================================================
// TASK 13: Database Query Caching & Data Access Layer
// Combines Strategy (cache eviction policy) + Facade (sanitize -> cache/DB -> metrics)
// ============================================================================
import java.util.LinkedHashMap;
import java.util.Map;

// Strategy Interface: Governs cache admission/eviction policy
interface CachePolicyStrategy {
    void putWithPolicy(Map<String, String> cache, String key, String value, int maxCapacity);
}

// Concrete Strategy 1: Least Recently Used (LRU) eviction
class LruEvictionStrategy implements CachePolicyStrategy {
    public void putWithPolicy(Map<String, String> cache, String key, String value, int maxCapacity) {
        if (cache.size() >= maxCapacity && !cache.containsKey(key)) {
            String eldestKey = cache.keySet().iterator().next();
            cache.remove(eldestKey);
            System.out.println("[LruEvictionStrategy] Evicted least recently used key: " + eldestKey);
        }
        cache.put(key, value);
    }
}

// Concrete Strategy 2: First-In, First-Out (FIFO) eviction
class FifoEvictionStrategy implements CachePolicyStrategy {
    public void putWithPolicy(Map<String, String> cache, String key, String value, int maxCapacity) {
        if (cache.size() >= maxCapacity && !cache.containsKey(key)) {
            String firstKey = cache.keySet().iterator().next();
            cache.remove(firstKey);
            System.out.println("[FifoEvictionStrategy] Evicted oldest inserted key: " + firstKey);
        }
        cache.put(key, value);
    }
}

// Concrete Strategy 3: Time-To-Live (TTL) expiration policy
class TtlExpirationStrategy implements CachePolicyStrategy {
    public void putWithPolicy(Map<String, String> cache, String key, String value, int maxCapacity) {
        System.out.println("[TtlExpirationStrategy] Stored key with 60s TTL: " + key);
        cache.put(key, value);
    }
}

// Subsystem 1: Prevents SQL injection and normalizes queries
class QuerySanitizer {
    public String sanitize(String sql) {
        if (sql.contains("; DROP")) throw new SecurityException("SQL Injection blocked!");
        return sql.trim();
    }
}

// Subsystem 2: In-memory key-value cache store
class MemoryCacheStore {
    private final Map<String, String> store = new LinkedHashMap<>(16, 0.75f, true);
    private final int maxCapacity = 2;

    public String get(String key) { return store.get(key); }

    public void put(String key, String val, CachePolicyStrategy policy) {
        policy.putWithPolicy(store, key, val, maxCapacity);
    }
}

// Subsystem 3: Executes SQL queries against pooled DB connections
class ConnectionPool {
    public String runOnDatabase(String sql, String params) {
        System.out.println("[ConnectionPool] Cache MISS -> Querying DB: " + sql + " " + params);
        return "ResultSet{rows=42 for " + params + "}";
    }
}

// Subsystem 4: Records cache hit/miss telemetry
class QueryMetricsRecorder {
    public void record(String sql, boolean cacheHit) {
        System.out.printf("[QueryMetricsRecorder] Query: '%s' | CacheHit: %b%n", sql, cacheHit);
    }
}

// Facade: Exposes unified query execution with transparent caching
class DataAccessFacade {
    private final QuerySanitizer sanitizer = new QuerySanitizer();
    private final MemoryCacheStore cache = new MemoryCacheStore();
    private final ConnectionPool dbPool = new ConnectionPool();
    private final QueryMetricsRecorder metrics = new QueryMetricsRecorder();
    private CachePolicyStrategy strategy;

    public DataAccessFacade(CachePolicyStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(CachePolicyStrategy strategy) {
        this.strategy = strategy;
    }

    public String executeQuery(String sql, String params) {
        String cleanSql = sanitizer.sanitize(sql);
        String cacheKey = cleanSql + "::" + params;

        String cached = cache.get(cacheKey);
        if (cached != null) {
            System.out.println("[MemoryCacheStore] Cache HIT for " + cacheKey);
            metrics.record(cleanSql, true);
            return cached;
        }

        String result = dbPool.runOnDatabase(cleanSql, params);
        cache.put(cacheKey, result, strategy);
        metrics.record(cleanSql, false);
        return result;
    }
}

// Client Driver
public class Task13Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 13: Database Query Caching & Data Access Layer ===");
        DataAccessFacade dao = new DataAccessFacade(new LruEvictionStrategy());
        dao.executeQuery("SELECT * FROM users WHERE id=?", "[1]");
        dao.executeQuery("SELECT * FROM users WHERE id=?", "[1]"); // Cache HIT
        dao.executeQuery("SELECT * FROM users WHERE id=?", "[2]");
        dao.executeQuery("SELECT * FROM users WHERE id=?", "[3]"); // Triggers LRU eviction
    }
}
