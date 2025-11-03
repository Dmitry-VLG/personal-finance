package app.fintrack.infra.store;

import app.fintrack.core.model.User;
import app.fintrack.core.spi.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class FileUserRepository implements UserRepository {
    private final File file;
    private final ObjectMapper om;
    private Map<String, User> users = new HashMap<>();

    public FileUserRepository(File baseDir, ObjectMapper om) {
        this.file = new File(baseDir, "users.json");
        this.om = om;
        load();
    }

    @Override public Optional<User> findByLogin(String login) {
        return Optional.ofNullable(users.get(login));
    }

    @Override public void save(User user) {
        users.put(user.login(), user);
        persist();
    }

    private void load() {
        try {
            if (file.exists()) {
                List<User> list = om.readValue(file, new TypeReference<>(){});
                users.clear();
                for (User u : list) users.put(u.login(), u);
            }
        } catch (Exception e) { throw new IllegalStateException("Cannot load users.json", e); }
    }

    private void persist() {
        try {
            if (!file.getParentFile().exists()) Files.createDirectories(file.getParentFile().toPath());
            om.writerWithDefaultPrettyPrinter().writeValue(file, new ArrayList<>(users.values()));
        } catch (Exception e) { throw new IllegalStateException("Cannot save users.json", e); }
    }
}
