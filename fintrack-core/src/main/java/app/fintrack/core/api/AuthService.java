package app.fintrack.core.api;

import java.util.Optional;

public interface AuthService {
    void register(String login, char[] password);
    void login(String login, char[] password);
    void logout();
    Optional<String> currentLogin();
}
