package app.fintrack.infra.service;

import app.fintrack.core.api.WalletService;
import app.fintrack.core.model.*;
import app.fintrack.core.util.Preconditions;
import app.fintrack.infra.runtime.SessionContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class WalletServiceImpl implements WalletService {
    private final SessionContext session;
    public WalletServiceImpl(SessionContext session) { this.session = session; }

    @Override public void addIncome(String category, BigDecimal amount, String note) { add(Transaction.TxType.INCOME, category, amount, note, null); }
    @Override public void addExpense(String category, BigDecimal amount, String note) { add(Transaction.TxType.EXPENSE, category, amount, note, null); }

    private void add(Transaction.TxType type, String category, BigDecimal amount, String note, String counterparty) {
        Preconditions.check(session.currentWallet().isPresent(), "Сначала login");
        Preconditions.check(category != null && !category.isBlank(), "Категория обязательна");
        Preconditions.check(amount != null && amount.compareTo(BigDecimal.ZERO) > 0, "Сумма должна быть > 0");
        Wallet w = session.currentWallet().get();
        Transaction tx = new Transaction(UUID.randomUUID().toString(), type, category, amount, Instant.now(), note, counterparty);
        w.getOperations().add(tx);
    }

    @Override public void setBudget(String category, BigDecimal limit) {
        Preconditions.check(session.currentWallet().isPresent(), "Сначала login");
        Preconditions.check(category != null && !category.isBlank(), "Категория обязательна");
        Preconditions.check(limit != null && limit.compareTo(BigDecimal.ZERO) > 0, "Лимит должен быть > 0");
        Wallet w = session.currentWallet().get();
        w.getBudgets().put(category, new Budget(category, limit));
    }

    @Override public Summary getSummary(Set<String> categories, LocalDate from, LocalDate to) {
        Preconditions.check(session.currentWallet().isPresent(), "Сначала login");
        Wallet w = session.currentWallet().get();
        var ops = w.getOperations().stream()
                .filter(tx -> filterByDate(tx, from, to))
                .filter(tx -> categories == null || categories.isEmpty() || categories.contains(tx.category()))
                .toList();

        BigDecimal income = sum(ops, Transaction.TxType.INCOME, Transaction.TxType.TRANSFER_IN);
        BigDecimal expense = sum(ops, Transaction.TxType.EXPENSE, Transaction.TxType.TRANSFER_OUT);

        Map<String, BigDecimal> byCat = ops.stream()
                .filter(tx -> tx.type() == Transaction.TxType.EXPENSE)
                .collect(Collectors.groupingBy(Transaction::category,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::amount, BigDecimal::add)));

        Map<String, Summary.BudgetStatus> statuses = new HashMap<>();
        for (var e : w.getBudgets().entrySet()) {
            String cat = e.getKey();
            BigDecimal limit = e.getValue().limit();
            BigDecimal spent = byCat.getOrDefault(cat, BigDecimal.ZERO);
            BigDecimal remaining = limit.subtract(spent);
            boolean exceeded = remaining.compareTo(BigDecimal.ZERO) < 0;
            statuses.put(cat, new Summary.BudgetStatus(limit, spent, remaining, exceeded));
        }

        return new Summary(income, expense, byCat, statuses);
    }

    private boolean filterByDate(Transaction tx, LocalDate from, LocalDate to) {
        LocalDate d = LocalDate.ofInstant(tx.timestamp(), ZoneId.systemDefault());
        if (from != null && d.isBefore(from)) return false;
        if (to != null && d.isAfter(to)) return false;
        return true;
    }

    private BigDecimal sum(List<Transaction> ops, Transaction.TxType... types) {
        Set<Transaction.TxType> set = Set.of(types);
        return ops.stream()
                .filter(tx -> set.contains(tx.type()))
                .map(Transaction::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
