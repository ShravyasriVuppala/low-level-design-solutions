package filesystem;

public abstract class FileSystemEntry{
    private String name;
    private Folder parent;

    public FileSystemEntry(String name) {
        this.name = name;
    }

    public Folder getParent() {
        return parent;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract boolean isDirectory();

    public String getName() {
        return name;
    }

    public void setParent(Folder parent) {
        this.parent = parent;
    }
}
