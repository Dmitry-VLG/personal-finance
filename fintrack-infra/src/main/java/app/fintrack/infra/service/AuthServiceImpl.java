package app.fintrack.infra.service;

import app.fintrack.core.api.AuthService;
import app.fintrack.core.model.User;
import app.fintrack.core.model.Wallet;
import app.fintrack.core.spi.UserRepository;
import app.fintrack.core.spi.WalletRepository;
import app.fintrack.infra.runtime.SessionContext;
import app.fintrack.infra.security.PasswordHasher;

import java.util.Optional;

public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final WalletRepository wallets;
    private final SessionContext session;

    public AuthServiceImpl(UserRepository users, WalletRepository wallets, SessionContext session) {
        this.users = users; this.wallets = wallets; this.session = session;
    }

    @Override public void register(String login, char[] password) {
        if (users.findByLogin(login).isPresent()) throw new IllegalArgumentException("Пользователь уже существует");
        String hash = PasswordHasher.hash(password);
        users.save(new User(login, hash));
    }

    @Override public void login(String login, char[] password) {
        User u = users.findByLogin(login).orElseThrow(() -> new IllegalArgumentException("Не найден пользователь"));
        if (!PasswordHasher.verify(password, u.passwordHash())) throw new IllegalArgumentException("Неверный пароль");
        Wallet w = wallets.load(login); // загрузка кошелька при входе
        session.open(login, w);
    }

    @Override public void logout() {
        session.currentWallet().ifPresent(wallets::save); // сохранение кошелька при выходе
        session.close();
    }

    @Override public Optional<String> currentLogin() { return session.currentLogin(); }
}
