package app.fintrack.core.api;

import app.fintrack.core.model.Summary;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

public interface WalletService {
    void addIncome(String category, BigDecimal amount, String note);
    void addExpense(String category, BigDecimal amount, String note);
    void setBudget(String category, BigDecimal limit);
    Summary getSummary(Set<String> categories, LocalDate from, LocalDate to);
}
