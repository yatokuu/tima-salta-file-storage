import java.nio.file.Files;
import java.nio.file.Path;

public class TestRunner {
    private static int passed = 0;

    public static void main(String[] args) throws Exception {
        Path base = Files.createTempDirectory("tima-salta-tests-");
        run("folder store", new FolderStorage(base.resolve("folder")));
        run("snapshot store", new SnapshotStorage(base.resolve("snapshot.bin")));
        System.out.println("PASS: " + passed + " checks across both stores");
    }

    private static void run(String label, Storage storage) throws Exception {
        FileManager manager = new FileManager(storage);
        FileService owner = proxy("tima", manager);
        FileService viewer = proxy("salta", manager);
        FileService stranger = proxy("guest", manager);

        manager.createFolder("Coursework");
        FileRecord file = owner.create("BridgeNotes", "txt", "Hello bridge", "Coursework");

        check(file.owner.equals("tima"), label + " owner recorded");
        check(manager.files().size() == 1, label + " list");
        check(file.content.getBytes(java.nio.charset.StandardCharsets.UTF_8).length == 12, label + " size");
        denied(() -> viewer.read(file.id), label + " unshared read denied");
        owner.share(file.id, "salta");
        check(viewer.read(file.id).equals("Hello bridge"), label + " shared read");
        denied(() -> stranger.read(file.id), label + " stranger read denied");
        denied(() -> viewer.rename(file.id, "No"), label + " viewer rename denied");
        denied(() -> viewer.delete(file.id), label + " viewer delete denied");
        denied(() -> viewer.share(file.id, "guest"), label + " viewer share denied");
        denied(() -> viewer.move(file.id, ""), label + " viewer move denied");
        failed(() -> manager.deleteFolder("Coursework"), label + " nonempty folder retained");

        FileManager reopened = new FileManager(storage);
        check(reopened.find(file.id).created.equals(file.created), label + " creation date persisted");
        check(proxy("salta", reopened).read(file.id).equals("Hello bridge"), label + " content and share persisted");
        FileService reopenedOwner = proxy("tima", reopened);
        reopenedOwner.rename(file.id, "Updated");
        check(reopened.find(file.id).name.equals("Updated"), label + " rename");
        reopenedOwner.move(file.id, "");
        reopened.deleteFolder("Coursework");
        check(reopened.folders().isEmpty(), label + " empty folder deletion");
        reopenedOwner.delete(file.id);
        check(new FileManager(storage).files().isEmpty(), label + " deletion persisted");
        System.out.println(label + ": passed");
    }

    private static FileService proxy(String user, FileManager manager) {
        return new FileAccessProxy(user, new RealFileService(manager), manager);
    }

    private static void check(boolean condition, String name) {
        if (!condition) {
            throw new AssertionError(name);
        }
        passed++;
    }

    private static void denied(Throwing action, String name) throws Exception {
        try {
            action.run();
            throw new AssertionError(name);
        } catch (SecurityException expected) {
            passed++;
        }
    }

    private static void failed(Throwing action, String name) throws Exception {
        try {
            action.run();
            throw new AssertionError(name);
        } catch (IllegalStateException expected) {
            passed++;
        }
    }

    interface Throwing {
        void run() throws Exception;
    }
}
