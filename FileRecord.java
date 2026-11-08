import java.io.Serializable;
import java.time.Instant;
import java.util.Set;
import java.util.TreeSet;

public final class FileRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    final String id;
    final String type;
    final String owner;

    String created;
    String name;
    String content;
    String folder;

    final Set<String> sharedWith = new TreeSet<>();

    FileRecord(String id, String name, String content, String type, String owner, String folder) {
        this.id = id;
        this.name = name;
        this.content = content;
        this.type = type;
        this.owner = owner;
        this.folder = folder;
        this.created = Instant.now().toString();
    }
}
