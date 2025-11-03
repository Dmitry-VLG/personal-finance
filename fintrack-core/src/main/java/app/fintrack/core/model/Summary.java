package app.fintrack.core.model;

import java.math.BigDecimal;
import java.util.Map;

public record Summary(
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        Map<String, BigDecimal> expensesByCategory,
        Map<String, BudgetStatus> budgetStatuses
) {
    public record BudgetStatus(
            BigDecimal limit,
            BigDecimal spent,
            BigDecimal remaining,
            boolean exceeded
    ) {}
}
