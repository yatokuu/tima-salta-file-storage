import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/** Stores metadata in an index and content in separate UTF-8 files. */
public final class FolderStorage implements Storage {
    private final Path directory;

    FolderStorage(Path directory) {
        this.directory = directory;
    }

    public String name() {
        return "folder";
    }

    public State load() throws IOException {
        State state = new State();
        Path index = directory.resolve("index.properties");
        if (!Files.exists(index)) {
            return state;
        }
        Properties p = new Properties();
        try (Reader reader = Files.newBufferedReader(index, StandardCharsets.UTF_8)) {
            p.load(reader);
        }
        int folderCount = Integer.parseInt(p.getProperty("folder.count", "0"));
        for (int i = 0; i < folderCount; i++) {
            state.folders.add(p.getProperty("folder." + i));
        }
        int count = Integer.parseInt(p.getProperty("file.count", "0"));
        for (int i = 0; i < count; i++) {
            String prefix = "file." + i + ".";
            String id = p.getProperty(prefix + "id");
            FileRecord f = new FileRecord(id, p.getProperty(prefix + "name"),
                    Files.readString(directory.resolve(id + ".content"), StandardCharsets.UTF_8),
                    p.getProperty(prefix + "type"), p.getProperty(prefix + "owner"), p.getProperty(prefix + "folder"));
            f.created = p.getProperty(prefix + "created", f.created);
            String shared = p.getProperty(prefix + "shared", "");
            if (!shared.isEmpty()) {
                f.sharedWith.addAll(Arrays.asList(shared.split(",")));
            }
            state.files.put(id, f);
        }
        return state;
    }

    public void save(State state) throws IOException {
        Files.createDirectories(directory);
        Properties p = new Properties();
        p.setProperty("folder.count", Integer.toString(state.folders.size()));
        int i = 0;
        for (String folder : state.folders) {
            p.setProperty("folder." + i++, folder);
        }
        p.setProperty("file.count", Integer.toString(state.files.size()));
        i = 0;
        Set<String> active = new HashSet<>();
        for (FileRecord f : state.files.values()) {
            String prefix = "file." + i++ + ".";
            p.setProperty(prefix + "id", f.id);
            p.setProperty(prefix + "name", f.name);
            p.setProperty(prefix + "type", f.type);
            p.setProperty(prefix + "owner", f.owner);
            p.setProperty(prefix + "folder", f.folder);
            p.setProperty(prefix + "created", f.created);
            p.setProperty(prefix + "shared", String.join(",", f.sharedWith));
            String contentFile = f.id + ".content";
            active.add(contentFile);
            Files.writeString(directory.resolve(contentFile), f.content, StandardCharsets.UTF_8);
        }
        Path temp = directory.resolve("index.properties.tmp");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            p.store(writer, "Tima & Salta file index");
        }
        Files.move(temp, directory.resolve("index.properties"), StandardCopyOption.REPLACE_EXISTING);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.content")) {
            for (Path path : stream) {
                if (!active.contains(path.getFileName().toString())) {
                    Files.delete(path);
                }
            }
        }
    }
}
