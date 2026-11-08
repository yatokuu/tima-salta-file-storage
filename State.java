import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class State implements Serializable {
    private static final long serialVersionUID = 1L;
    final Map<String, FileRecord> files = new LinkedHashMap<>();
    final Set<String> folders = new TreeSet<>();
}
