// ============================================================================
// TASK 10: Multi-Criteria Route Navigation Engine
// Combines Strategy (routing criteria) + Facade (GPS, tiles, traffic, voice)
// ============================================================================

// Strategy Interface: Computes path based on optimization criteria
interface RoutingStrategy {
    String calculateRoute(String origin, String destination, String trafficLevel);
}

// Concrete Strategy 1: Minimizes travel duration using live traffic
class FastestTimeRouting implements RoutingStrategy {
    public String calculateRoute(String origin, String destination, String trafficLevel) {
        return String.format("Fastest route %s -> %s via Highway A1 (Traffic: %s)", origin, destination, trafficLevel);
    }
}

// Concrete Strategy 2: Minimizes total kilometers driven
class ShortestDistanceRouting implements RoutingStrategy {
    public String calculateRoute(String origin, String destination, String trafficLevel) {
        return String.format("Shortest route %s -> %s via City Center (12.4 km)", origin, destination);
    }
}

// Concrete Strategy 3: Excludes all toll roads and bridges
class AvoidTollsRouting implements RoutingStrategy {
    public String calculateRoute(String origin, String destination, String trafficLevel) {
        return String.format("Toll-free route %s -> %s via Scenic Bypass", origin, destination);
    }
}

// Subsystem 1: Tracks vehicle's live coordinates
class GpsTracker {
    public String getCurrentLocation() {
        System.out.println("[GpsTracker] Acquiring GPS fix...");
        return "43.2389° N, 76.8897° E";
    }
}

// Subsystem 2: Loads vector map tiles for the route corridor
class MapTileLoader {
    public void loadRegionTiles(String routeSummary) {
        System.out.println("[MapTileLoader] Loading map tiles for: " + routeSummary);
    }
}

// Subsystem 3: Fetches real-time congestion telemetry
class TrafficFeedClient {
    public String fetchTrafficStatus() {
        System.out.println("[TrafficFeedClient] Fetching live traffic feed...");
        return "Moderate";
    }
}

// Subsystem 4: Announces turn-by-turn audio prompts
class VoiceGuidanceUnit {
    public void announce(String instruction) {
        System.out.println("[VoiceGuidanceUnit] Voice prompt: \"" + instruction + "\"");
    }
}

// Facade: Starts navigation and supports dynamic mid-trip recalculation
class NavigationFacade {
    private final GpsTracker gps = new GpsTracker();
    private final MapTileLoader tileLoader = new MapTileLoader();
    private final TrafficFeedClient trafficClient = new TrafficFeedClient();
    private final VoiceGuidanceUnit voice = new VoiceGuidanceUnit();
    private RoutingStrategy strategy;
    private String activeDestination;

    public NavigationFacade(RoutingStrategy strategy) {
        this.strategy = strategy;
    }

    public void startNavigation(String origin, String destination) {
        this.activeDestination = destination;
        gps.getCurrentLocation();
        String traffic = trafficClient.fetchTrafficStatus();
        String route = strategy.calculateRoute(origin, destination, traffic);
        tileLoader.loadRegionTiles(route);
        voice.announce("Starting navigation: " + route);
    }

    // Swaps routing criteria mid-trip and recalculates from current GPS position
    public void switchRoutingStrategy(RoutingStrategy newStrategy) {
        this.strategy = newStrategy;
        String currentPos = gps.getCurrentLocation();
        String traffic = trafficClient.fetchTrafficStatus();
        String updatedRoute = strategy.calculateRoute(currentPos, activeDestination, traffic);
        tileLoader.loadRegionTiles(updatedRoute);
        voice.announce("Route recalculated: " + updatedRoute);
    }
}

// Client Driver
public class Task10Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 10: Multi-Criteria Route Navigation Engine ===");
        NavigationFacade nav = new NavigationFacade(new FastestTimeRouting());
        nav.startNavigation("Almaty Central", "Medeu");

        System.out.println("--- Swapping Strategy Mid-Trip ---");
        nav.switchRoutingStrategy(new AvoidTollsRouting());
    }
}
