package app.fintrack.core.spi;

import app.fintrack.core.model.Wallet;

public interface WalletRepository {
    Wallet load(String login);
    void save(Wallet wallet);
}
