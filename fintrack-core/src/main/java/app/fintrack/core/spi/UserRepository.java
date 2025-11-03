package app.fintrack.core.spi;

import app.fintrack.core.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByLogin(String login);
    void save(User user);
}
