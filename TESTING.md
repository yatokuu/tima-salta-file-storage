# Test cases and results

Run on 2026-11-08 with `javac -d build *.java` and `java -cp build TestRunner` from the project directory.

| Test case | Expected result | Folder store | Snapshot store |
|---|---|---|---|
| Create file as Tima | Owner recorded; file listed; UTF-8 size correct | Pass | Pass |
| Salta reads before share | Denied | Pass | Pass |
| Tima shares with Salta; Salta reads | Content returned | Pass | Pass |
| Guest reads without share | Denied | Pass | Pass |
| Salta renames, deletes, shares, or moves | All four denied | Pass | Pass |
| Delete folder while it contains file | Rejected | Pass | Pass |
| Reopen storage | Creation date, content, and share retained | Pass | Pass |
| Owner renames and moves file | Changes applied | Pass | Pass |
| Delete empty folder | Allowed | Pass | Pass |
| Owner deletes file and reopens storage | File remains deleted | Pass | Pass |

Actual output:

```text
folder store: passed
snapshot store: passed
PASS: 32 checks across both stores
```

The interactive walkthrough in README.md also covers menu input and switching users. Automated tests use a temporary directory and do not alter the app's data.
