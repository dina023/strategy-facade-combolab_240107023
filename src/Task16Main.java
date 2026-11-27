// ============================================================================
// TASK 16: Automated Loan Underwriting & Risk Assessment System
// Combines Strategy (risk scoring models) + Facade (fraud short-circuit & audit)
// ============================================================================

// Strategy Interface: Computes risk score (0-100, higher = safer)
interface RiskScoringStrategy {
    int evaluateRisk(int creditScore, double dtiRatio, double annualRevenue);
}

// Concrete Strategy 1: Strict underwriting prioritizing high credit scores and low debt
class ConservativeCreditStrategy implements RiskScoringStrategy {
    public int evaluateRisk(int creditScore, double dtiRatio, double annualRevenue) {
        return (creditScore >= 740 && dtiRatio <= 0.30) ? 88 : 42;
    }
}

// Concrete Strategy 2: Balanced consumer retail underwriting
class StandardRetailStrategy implements RiskScoringStrategy {
    public int evaluateRisk(int creditScore, double dtiRatio, double annualRevenue) {
        return (creditScore >= 660 && dtiRatio <= 0.42) ? 75 : 50;
    }
}

// Concrete Strategy 3: Commercial underwriting weighted toward revenue growth
class CashFlowGrowthStrategy implements RiskScoringStrategy {
    public int evaluateRisk(int creditScore, double dtiRatio, double annualRevenue) {
        return (annualRevenue >= 150000 && dtiRatio <= 0.50) ? 82 : 48;
    }
}

// Subsystem 1: Fetches external credit bureau score
class CreditBureauAdapter {
    public int fetchCreditScore(String applicantId) {
        System.out.println("[CreditBureauAdapter] Pulling credit report for " + applicantId);
        return 710;
    }
}

// Subsystem 2: Computes Debt-to-Income (DTI) ratio
class DebtToIncomeCalculator {
    public double calculateDti(double monthlyDebt, double monthlyIncome) {
        double dti = monthlyDebt / monthlyIncome;
        System.out.printf("[DebtToIncomeCalculator] Computed DTI ratio: %.2f%n", dti);
        return dti;
    }
}

// Subsystem 3: Screens application for identity theft or synthetic fraud flags
class FraudScreeningEngine {
    public boolean hasFraudFlag(String applicantId) {
        boolean flagged = applicantId.startsWith("FRAUD");
        System.out.println("[FraudScreeningEngine] Fraud check flagged: " + flagged);
        return flagged;
    }
}

// Subsystem 4: Writes immutable regulatory compliance audit logs
class ComplianceAuditLogger {
    public void logDecision(String applicantId, String decision, String reason) {
        System.out.printf("[ComplianceAuditLogger] AUDIT | Applicant: %s | Decision: %s | Reason: %s%n",
                applicantId, decision, reason);
    }
}

// Facade: Orchestrates underwriting with immediate fraud short-circuiting and audit logging
class UnderwritingFacade {
    private final CreditBureauAdapter bureau = new CreditBureauAdapter();
    private final DebtToIncomeCalculator dtiCalc = new DebtToIncomeCalculator();
    private final FraudScreeningEngine fraudEngine = new FraudScreeningEngine();
    private final ComplianceAuditLogger auditLogger = new ComplianceAuditLogger();
    private RiskScoringStrategy strategy;

    public UnderwritingFacade(RiskScoringStrategy strategy) {
        this.strategy = strategy;
    }

    public void setStrategy(RiskScoringStrategy strategy) {
        this.strategy = strategy;
    }

    public String evaluateApplication(String applicantId, double monthlyDebt, double monthlyIncome) {
        // Short-circuit immediately if fraud is detected
        if (fraudEngine.hasFraudFlag(applicantId)) {
            String report = "REJECTED (Immediate Fraud Short-Circuit)";
            auditLogger.logDecision(applicantId, "REJECTED", "Fraud flag triggered");
            return report;
        }

        int score = bureau.fetchCreditScore(applicantId);
        double dti = dtiCalc.calculateDti(monthlyDebt, monthlyIncome);
        int riskRating = strategy.evaluateRisk(score, dti, monthlyIncome * 12);

        String decision = riskRating >= 70 ? "APPROVED" : "DECLINED";
        auditLogger.logDecision(applicantId, decision, "RiskRating=" + riskRating);
        return String.format("DecisionReport{applicant='%s', status='%s', rating=%d}",
                applicantId, decision, riskRating);
    }
}

// Client Driver
public class Task16Main {
    public static void main(String[] args) {
        System.out.println("=== TASK 16: Automated Loan Underwriting & Risk Assessment ===");
        UnderwritingFacade underwriter = new UnderwritingFacade(new StandardRetailStrategy());

        System.out.println(underwriter.evaluateApplication("FRAUD-999", 1000, 5000));
        System.out.println(underwriter.evaluateApplication("APP-101", 1500, 5000));

        underwriter.setStrategy(new ConservativeCreditStrategy());
        System.out.println(underwriter.evaluateApplication("APP-101", 1500, 5000));
    }
}
