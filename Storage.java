import java.io.IOException;

public interface Storage {
    State load() throws IOException, ClassNotFoundException;

    void save(State state) throws IOException;

    String name();
}
