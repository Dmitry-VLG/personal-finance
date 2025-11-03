package app.fintrack.infra.store;

import app.fintrack.core.model.Wallet;
import app.fintrack.core.spi.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.nio.file.Files;

public class FileWalletRepository implements WalletRepository {
    private final File baseDir;
    private final ObjectMapper om;

    public FileWalletRepository(File baseDir) {
        this.baseDir = baseDir;
        this.om = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override public Wallet load(String login) {
        try {
            File f = new File(baseDir, "wallet_"+login+".json");
            if (!f.exists()) return new Wallet(login);
            return om.readValue(f, Wallet.class);
        } catch (Exception e) { throw new IllegalStateException("Cannot load wallet for "+login, e); }
    }

    @Override public void save(Wallet wallet) {
        try {
            if (!baseDir.exists()) Files.createDirectories(baseDir.toPath());
            File f = new File(baseDir, "wallet_"+wallet.getOwnerLogin()+".json");
            om.writerWithDefaultPrettyPrinter().writeValue(f, wallet);
        } catch (Exception e) { throw new IllegalStateException("Cannot save wallet", e); }
    }
}
