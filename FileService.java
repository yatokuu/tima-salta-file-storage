import java.io.IOException;

public interface FileService {
    FileRecord create(String name, String type, String content, String folder) throws IOException;

    String read(String id);

    FileRecord info(String id);

    void rename(String id, String name) throws IOException;

    void delete(String id) throws IOException;

    void share(String id, String user) throws IOException;

    void move(String id, String folder) throws IOException;
}
