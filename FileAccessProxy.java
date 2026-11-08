import java.io.IOException;

public final class FileAccessProxy implements FileService {
    private final String user;
    private final RealFileService real;
    private final FileManager manager;

    FileAccessProxy(String user, RealFileService real, FileManager manager) {
        this.user = user;
        this.real = real;
        this.manager = manager;
        real.as(user);
    }

    private FileRecord readable(String id) {
        FileRecord f = manager.find(id);
        if (!manager.canRead(user, f)) {
            throw new SecurityException("Read denied for " + user);
        }
        return f;
    }

    private void owner(String id) {
        if (!manager.find(id).owner.equals(user)) {
            throw new SecurityException("Owner permission required for " + user);
        }
    }

    public FileRecord create(String name, String type, String content, String folder) throws IOException {
        return real.create(name, type, content, folder);
    }

    public String read(String id) {
        readable(id);
        return real.read(id);
    }

    public FileRecord info(String id) {
        readable(id);
        return real.info(id);
    }

    public void rename(String id, String name) throws IOException {
        owner(id);
        real.rename(id, name);
    }

    public void delete(String id) throws IOException {
        owner(id);
        real.delete(id);
    }

    public void share(String id, String target) throws IOException {
        owner(id);
        real.share(id, target);
    }

    public void move(String id, String folder) throws IOException {
        owner(id);
        real.move(id, folder);
    }
}
