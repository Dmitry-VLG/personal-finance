package app.fintrack.core.model;

import java.util.*;

public class Wallet {
    private int schemaVersion = 1; // эволюция формата
    private final String ownerLogin;
    private final java.util.List<Transaction> operations = new ArrayList<>();
    private final java.util.Map<String, Budget> budgets = new HashMap<>();

    public Wallet(String ownerLogin) { this.ownerLogin = Objects.requireNonNull(ownerLogin); }
    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int v) { this.schemaVersion = v; }
    public String getOwnerLogin() { return ownerLogin; }
    public java.util.List<Transaction> getOperations() { return operations; }
    public java.util.Map<String, Budget> getBudgets() { return budgets; }
}

