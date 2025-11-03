package app.fintrack.infra.runtime;

import app.fintrack.core.model.Wallet;

import java.util.Optional;

public class SessionContext {
    private String currentLogin;
    private Wallet currentWallet;

    public Optional<String> currentLogin() { return Optional.ofNullable(currentLogin); }
    public Optional<Wallet> currentWallet() { return Optional.ofNullable(currentWallet); }

    public void open(String login, Wallet wallet) {
        this.currentLogin = login;
        this.currentWallet = wallet;
    }

    public void close() {
        this.currentLogin = null;
        this.currentWallet = null;
    }
}
