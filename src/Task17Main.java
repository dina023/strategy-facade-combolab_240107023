// ============================================================================
// TASK 17: Multiplayer Matchmaking & Lobby Allocation Service
// Combines Strategy (matchmaking rules) + Facade (timeout fallback & server spin-up)
// ============================================================================

// Strategy Interface: Evaluates whether a lobby meets matchmaking criteria
interface MatchmakingStrategy {
    String getModeName();
    boolean canFormLobby(int mmrSpread, int maxPingMs, int waitSeconds);
}

// Concrete Strategy 1: Tight MMR skill window for ranked competitive play
class StrictSkillMmrStrategy implements MatchmakingStrategy {
    public String getModeName() { return "StrictSkillMMR"; }
    public boolean canFormLobby(int mmrSpread, int maxPingMs, int waitSeconds) {
        return mmrSpread <= 50;
    }
}

// Concrete Strategy 2: Prioritizes low network latency over skill parity
class LowLatencyPriorityStrategy implements MatchmakingStrategy {
    public String getModeName() { return "LowLatencyPriority"; }
    public boolean canFormLobby(int mmrSpread, int maxPingMs, int waitSeconds) {
        return maxPingMs <= 35;
    }
}

// Concrete Strategy 3: Broad quick-fill fallback strategy to minimize wait times
class CasualQuickFillStrategy implements MatchmakingStrategy {
    public String getModeName() { return "CasualQuickFill"; }
    public boolean canFormLobby(int mmrSpread, int maxPingMs, int waitSeconds) {
        return mmrSpread <= 400 && maxPingMs <= 120;
    }
}

// Subsystem 1: Tracks player queue wait time and MMR spread
class PlayerQueueManager {
    public int getQueueWaitSeconds(String partyId) {
        System.out.println("[PlayerQueueManager] Checking wait time for party " + partyId);
        return 75; // Exceeds 60s threshold
    }
    public int getCurrentMmrSpread() { return 180; }
}

// Subsystem 2: Evaluates regional data center ping latency
class RegionPingEvaluator {
    public int evaluateMaxPingMs(String region) {
        System.out.println("[RegionPingEvaluator] Measuring ping in region " + region);
        return 42;
    }
}

// Subsystem 3: Verifies party size and team roster symmetry
class PartyBalanceValidator {
    public boolean validateRoster(String partyId) {
        System.out.println("[PartyBalanceValidator] Validating team balance for " + partyId);
        return true;
    }
}

// Subsystem 4: Provisions dedicated game server container
class GameServerProvisioner {
    public void allocateServer(String partyId, String strategyUsed) {
        System.out.printf("[GameServerProvisioner] Provisioned server for %s using [%s]%n",
                partyId, strategyUsed);
    }
}

// Facade: Coordinates matchmaking and automatically falls back if queue wait exceeds threshold
class MatchmakingFacade {
    private final PlayerQueueManager queueManager = new PlayerQueueManager();
    private final RegionPingEvaluator pingEvaluator = new RegionPingEvaluator();
    private final PartyBalanceValidator balanceValidator = new PartyBalanceValidator();
    private final GameServerProvisioner serverProvisioner = new GameServerProvisioner();
    private MatchmakingStrategy strategy;

    public MatchmakingFacade(MatchmakingStrategy strategy) {
        this.strategy = strategy;
    }

    public void findMatch(String partyId, String region) {
        if (!balanceValidator.validateRoster(partyId)) return;

        int waitSec = queueManager.getQueueWaitSeconds(partyId);
        int mmrSpread = queueManager.getCurrentMmrSpread();
        int ping = pingEvaluator.evaluateMaxPingMs(region);

        MatchmakingStrategy active = this.strategy;
        // Automatic fallback if wait time exceeds 60s and primary strategy cannot match
        if (!active.canFormLobby(mmrSpread, ping, waitSec) && waitSec > 60) {
            System.out.printf("[MatchmakingFacade] Wait time (%ds) exceeded threshold! Falling back to CasualQuickFill...%n", waitSec);
            active = new CasualQuickFillStrategy();
        }

        if (active.canFormLobby(mmrSpread, ping, waitSec)) {
            serverProvisioner.allocateServer(partyId, active.getModeName());
        }
    }
}

// Client Driver
public class Task17Main {
    public static void main(String[] args) {
        System.out.println("\n=== TASK 17: Multiplayer Matchmaking & Lobby Allocation ===");
        MatchmakingFacade matchmaker = new MatchmakingFacade(new StrictSkillMmrStrategy());
        matchmaker.findMatch("SQUAD-ALPHA", "EU-CENTRAL");
    }
}
