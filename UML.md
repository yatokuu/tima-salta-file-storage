# Tima & Salta's File Storage — UML class diagram

The green palette distinguishes the two patterns: dark green for the file-management bridge, mint green for storage implementations, and lime green for the access proxy.

```mermaid
%%{init: {'theme':'base','themeVariables': {'background':'#f7fcf7','primaryColor':'#e8f5e9','primaryTextColor':'#163b25','primaryBorderColor':'#2e7d32','lineColor':'#388e3c','fontFamily':'Arial'}}}%%
classDiagram
    direction LR
    class Main {
      +main(args) void
    }
    class Storage {
      <<interface>>
      +load() State
      +save(state) void
      +name() String
    }
    class FolderStorage {
      -directory Path
      +load() State
      +save(state) void
    }
    class SnapshotStorage {
      -path Path
      +load() State
      +save(state) void
    }
    class FileManager {
      -storage Storage
      -state State
      +find(id) FileRecord
      +canRead(user, file) boolean
      +createFolder(name) void
      +deleteFolder(name) void
    }
    class State {
      +files Map
      +folders Set
    }
    class FileRecord {
      +id String
      +name String
      +type String
      +content String
      +owner String
      +created String
      +folder String
      +sharedWith Set
    }
    class FileService {
      <<interface>>
      +create(name, type, content, folder) FileRecord
      +read(id) String
      +info(id) FileRecord
      +rename(id, name) void
      +delete(id) void
      +share(id, user) void
      +move(id, folder) void
    }
    class RealFileService {
      -manager FileManager
    }
    class FileAccessProxy {
      -user String
      -real RealFileService
      -manager FileManager
    }
    Storage <|.. FolderStorage
    Storage <|.. SnapshotStorage
    FileManager o-- Storage : Bridge
    FileManager *-- State
    State *-- FileRecord
    FileService <|.. RealFileService
    FileService <|.. FileAccessProxy
    FileAccessProxy o-- RealFileService : Protection Proxy
    FileAccessProxy --> FileManager : checks permissions
    RealFileService --> FileManager : performs operations
    Main --> FileAccessProxy : client calls
    classDef bridge fill:#c8e6c9,stroke:#1b5e20,stroke-width:3px,color:#10351d
    classDef storage fill:#e0f2f1,stroke:#2e7d32,stroke-width:2px,color:#10351d
    classDef proxy fill:#dcedc8,stroke:#558b2f,stroke-width:3px,color:#263d16
    classDef data fill:#f1f8e9,stroke:#7cb342,stroke-width:2px,color:#263d16
    cssClass "FileManager,Storage" bridge
    cssClass "FolderStorage,SnapshotStorage" storage
    cssClass "FileService,RealFileService,FileAccessProxy" proxy
    cssClass "State,FileRecord" data
```

`FileManager` uses the `Storage` interface, so either persistent implementation can be selected without changing file-management code. `FileAccessProxy` checks the owner and sharing data in `FileRecord` before delegating to `RealFileService`.
