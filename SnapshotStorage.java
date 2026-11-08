import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class SnapshotStorage implements Storage {
    private final Path path;

    SnapshotStorage(Path path) {
        this.path = path;
    }

    public String name() {
        return "snapshot";
    }

    public State load() throws IOException, ClassNotFoundException {
        if (!Files.exists(path)) {
            return new State();
        }
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))) {
            return (State) in.readObject();
        }
    }

    public void save(State state) throws IOException {
        Files.createDirectories(path.toAbsolutePath().getParent());
        Path temp = path.resolveSibling(path.getFileName() + ".tmp");
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(temp))) {
            out.writeObject(state);
        }
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
    }
}
