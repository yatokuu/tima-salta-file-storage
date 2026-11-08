# Tima & Salta's File Storage System

A console based Java file manager using the Bridge and Protection Proxy patterns. It uses only the Java standard library (Java 11 or newer).

## Run

From this directory:

```sh
javac -d build *.java
java -cp build Main
```

At startup the program displays the **TIMA & SALTA'S FILE STORAGE SYSTEM** banner. Choose a storage implementation, then choose one of three simulated users: `tima`, `salta`, or `guest`. Data is saved under `data/` relative to the directory where the program is run. The two stores have separate data sets.

## What the menu does

Create, list, search, read, inspect, rename, delete, and share files. Create, display, and delete folders, and move files between a folder and the root. Every listed file displays its short ID; enter that ID for file operations. Search checks file names, ignoring case. File information shows its UTF-8 byte size and ISO 8601 creation time.

The console lists only files the current user can read. The three users are predefined; switching users simulates a different current user without passwords. A user owns each file they create. Owners can read, rename, delete, share, and move their files. A different user can read or inspect a file only after its owner shares it with them. Viewers cannot rename, delete, share, or move that file. Folder creation and deletion are demonstration operations available to all simulated users; they do not grant access to files inside.

Folder names are unique. File name **and type** must be unique within a folder, ignoring case. A folder containing files cannot be deleted; move or delete its files first. Root is represented by a blank folder input. Names and types must be 1–80 characters and cannot contain `/` or `\`. Invalid menu numbers are requested again; operation errors are printed without ending the program.

## Patterns

**Bridge:** `FileManager` is the abstraction for file and folder management. It holds a `Storage` interface by composition. `FolderStorage` saves readable `index.properties` metadata and a separate UTF-8 `.content` file per file. `SnapshotStorage` saves the complete state in one Java serialized file. `FileManager` is unchanged when the storage choice changes. This reduces coupling because file rules depend only on `Storage`, rather than either file format.

**Protection Proxy:** `FileService` is the common interface. `RealFileService` performs file operations using `FileManager`. The console creates `FileAccessProxy` and calls only that proxy. The proxy reads the file's actual owner and `sharedWith` set from `FileManager` for every protected request. It rejects unauthorized calls with `SecurityException` before the real service runs. Creation is allowed to the current user, who becomes that file's owner.

Each class and interface is in its own `.java` file. `Main.java` contains only the console application. See [UML.md](UML.md) for the green class diagram.

## Testing

Run:

```sh
javac -d build *.java
java -cp build TestRunner
```

`TestRunner` creates temporary storage directories, so it does not change the interactive app's `data/`. It runs the same scenario against both implementations. See [TESTING.md](TESTING.md) for test cases and recorded results.

## Scope

This is a teaching example. User identity is simulated by the menu; it is not authentication. Folder operations have no separate ownership model. Java serialization is used only for local application data; do not load snapshot files from untrusted sources.

