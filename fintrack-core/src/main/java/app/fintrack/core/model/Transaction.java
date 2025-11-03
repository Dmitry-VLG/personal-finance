package app.fintrack.core.model;

import java.math.BigDecimal;
import java.time.Instant;

public record Transaction(
        String id,
        TxType type,
        String category,
        BigDecimal amount,
        Instant timestamp,
        String note,
        String counterpartyLogin
) {
    public enum TxType { INCOME, EXPENSE, TRANSFER_IN, TRANSFER_OUT }
}
