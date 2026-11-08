import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

/**
 * Tima & Salta's File Storage: a small Bridge + Protection Proxy example.
 */
public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    static final List<String> USERS = Arrays.asList("tima", "salta", "guest");

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("   TIMA & SALTA'S FILE STORAGE SYSTEM");
        System.out.println("   Two stores. One manager. Safe sharing.");
        System.out.println("========================================");
        System.out.println("Choose storage: 1) Folder store  2) Snapshot store");
        int choice = number("Storage", 1, 2);
        Storage storage = choice == 1 ? new FolderStorage(Paths.get("data/folder-store"))
                : new SnapshotStorage(Paths.get("data/snapshot-store.bin"));
        try {
            FileManager manager = new FileManager(storage);
            String user = chooseUser();
            while (true) {
                System.out.println("\n[" + user + " | " + storage.name() + "]");
                System.out.println("1 Create file    2 List files      3 Search files");
                System.out.println("4 Read file      5 File info       6 Rename file");
                System.out.println("7 Delete file    8 Share file      9 Create folder");
                System.out.println("10 Show folders  11 Move file      12 Delete folder");
                System.out.println("13 Switch user   0 Exit");
                int action = number("Action", 0, 13);
                if (action == 0) {
                    return;
                }
                if (action == 13) {
                    user = chooseUser();
                    continue;
                }
                FileService service = new FileAccessProxy(user, new RealFileService(manager), manager);
                try {
                    execute(action, user, service, manager);
                } catch (IllegalArgumentException | IllegalStateException | SecurityException | IOException e) {
                    System.out.println("ERROR: " + e.getMessage());
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Cannot open storage: " + e.getMessage());
        }
    }

    private static void execute(int action, String user, FileService service, FileManager manager) throws IOException {
        switch (action) {
            case 1:
                service.create(prompt("Name"), prompt("Type (e.g. txt)"), prompt("Content"), prompt("Folder (blank = root)"));
                System.out.println("File created.");
                break;
            case 2:
                System.out.println("Files visible to " + user + ":");
                for (FileRecord f : manager.files()) {
                    if (manager.canRead(user, f)) {
                        printFile(f);
                    }
                }
                break;
            case 3:
                String query = prompt("Name to search").toLowerCase(Locale.ROOT);
                for (FileRecord f : manager.files()) {
                    if (manager.canRead(user, f)
                            && f.name.toLowerCase(Locale.ROOT).contains(query)) {
                        printFile(f);
                    }
                }
                break;
            case 4:
                System.out.println("Content: " + service.read(prompt("File ID")));
                break;
            case 5:
                FileRecord info = service.info(prompt("File ID"));
                System.out.println("Name: " + info.name + " | Type: " + info.type + " | Size: "
                        + info.content.getBytes(StandardCharsets.UTF_8).length + " bytes | Created: " + info.created
                        + " | Folder: " + displayFolder(info.folder) + " | Owner: " + info.owner);
                break;
            case 6:
                service.rename(prompt("File ID"), prompt("New name"));
                System.out.println("Renamed.");
                break;
            case 7:
                service.delete(prompt("File ID"));
                System.out.println("Deleted.");
                break;
            case 8:
                service.share(prompt("File ID"), prompt("Share with (tima/salta/guest)"));
                System.out.println("Shared.");
                break;
            case 9:
                manager.createFolder(prompt("Folder name"));
                System.out.println("Folder created.");
                break;
            case 10:
                System.out.println("/ (root)");
                for (String folder : manager.folders()) {
                    System.out.println("/" + folder);
                    for (FileRecord f : manager.files()) {
                        if (f.folder.equals(folder) && manager.canRead(user, f)) {
                            System.out.println("  " + f.id + "  " + f.name);
                        }
                    }
                }
                break;
            case 11:
                service.move(prompt("File ID"), prompt("Destination folder (blank = root)"));
                System.out.println("Moved.");
                break;
            case 12:
                manager.deleteFolder(prompt("Folder name"));
                System.out.println("Folder deleted.");
                break;
            default:
                throw new IllegalArgumentException("Unknown action");
        }
    }

    private static void printFile(FileRecord f) {
        System.out.println(f.id + "  " + displayFolder(f.folder) + "/" + f.name + "." + f.type
                + (f.sharedWith.isEmpty() ? "" : "  [shared]"));
    }

    private static String displayFolder(String folder) {
        return folder.isEmpty() ? "root" : folder;
    }

    private static String chooseUser() {
        System.out.println("Users: 1) tima  2) salta  3) guest");
        return USERS.get(number("User", 1, 3) - 1);
    }

    private static String prompt(String label) {
        System.out.print(label + ": ");
        return INPUT.nextLine().trim();
    }

    private static int number(String label, int min, int max) {
        while (true) {
            try {
                int n = Integer.parseInt(prompt(label));
                if (n >= min && n <= max) {
                    return n;
                }
            } catch (NumberFormatException ignored) {
            }
            System.out.println("Enter a number from " + min + " to " + max + ".");
        }
    }
}
