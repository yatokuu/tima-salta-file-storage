import java.io.IOException;
import java.util.UUID;

public final class RealFileService implements FileService {
    private final FileManager manager;
    private String currentUser;

    RealFileService(FileManager manager) {
        this.manager = manager;
    }

    public FileRecord create(String name, String type, String content, String folder) throws IOException {
        FileManager.valid(name, "File name");
        FileManager.valid(type, "File type");
        manager.folderExists(folder);
        manager.unique(name, type, folder, "");
        FileRecord f = new FileRecord(
                UUID.randomUUID().toString().substring(0, 8),
                name, content, type, currentUser, folder);
        manager.state.files.put(f.id, f);
        manager.save();
        return f;
    }

    void as(String user) {
        currentUser = user;
    }

    public String read(String id) {
        return manager.find(id).content;
    }

    public FileRecord info(String id) {
        return manager.find(id);
    }

    public void rename(String id, String name) throws IOException {
        FileManager.valid(name, "New name");
        FileRecord f = manager.find(id);
        manager.unique(name, f.type, f.folder, id);
        f.name = name;
        manager.save();
    }

    public void delete(String id) throws IOException {
        manager.find(id);
        manager.state.files.remove(id);
        manager.save();
    }

    public void share(String id, String user) throws IOException {
        if (!Main.USERS.contains(user)) {
            throw new IllegalArgumentException("Unknown user");
        }
        FileRecord f = manager.find(id);
        if (user.equals(f.owner)) {
            throw new IllegalArgumentException("Already owner");
        }
        f.sharedWith.add(user);
        manager.save();
    }

    public void move(String id, String folder) throws IOException {
        manager.folderExists(folder);
        FileRecord f = manager.find(id);
        manager.unique(f.name, f.type, folder, id);
        f.folder = folder;
        manager.save();
    }
}
