import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public final class FileManager {
    private final Storage storage;
    final State state;

    FileManager(Storage storage) throws IOException, ClassNotFoundException {
        this.storage = storage;
        this.state = storage.load();
    }

    Collection<FileRecord> files() {
        return Collections.unmodifiableCollection(state.files.values());
    }

    Set<String> folders() {
        return Collections.unmodifiableSet(state.folders);
    }

    FileRecord find(String id) {
        FileRecord f = state.files.get(id);
        if (f == null) {
            throw new IllegalArgumentException("File ID not found: " + id);
        }
        return f;
    }

    boolean canRead(String user, FileRecord f) {
        return f.owner.equals(user) || f.sharedWith.contains(user);
    }

    void save() throws IOException {
        storage.save(state);
    }

    void createFolder(String name) throws IOException {
        valid(name, "Folder name");
        if (!state.folders.add(name)) {
            throw new IllegalArgumentException("Folder already exists");
        }
        save();
    }

    void deleteFolder(String name) throws IOException {
        if (!state.folders.contains(name)) {
            throw new IllegalArgumentException("Folder not found");
        }
        for (FileRecord f : files()) {
            if (f.folder.equals(name)) {
                throw new IllegalStateException("Folder is not empty");
            }
        }
        state.folders.remove(name);
        save();
    }

    void unique(String name, String type, String folder, String exceptId) {
        for (FileRecord f : files()) {
            if (!f.id.equals(exceptId)
                    && f.folder.equals(folder)
                    && f.name.equalsIgnoreCase(name)
                    && f.type.equalsIgnoreCase(type)) {
                throw new IllegalArgumentException("A file with that name and type already exists in this folder");
            }
        }
    }

    void folderExists(String folder) {
        if (!folder.isEmpty() && !state.folders.contains(folder)) {
            throw new IllegalArgumentException("Folder not found");
        }
    }

    static void valid(String value, String label) {
        if (value == null || value.isBlank() || value.length() > 80 || value.contains("/") || value.contains("\\")) {
            throw new IllegalArgumentException(label + " must be 1-80 characters and contain no slash");
        }
    }
}
