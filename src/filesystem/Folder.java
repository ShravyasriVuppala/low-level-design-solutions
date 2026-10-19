package filesystem;

import java.util.HashMap;
import java.util.Map;

public class Folder extends FileSystemEntry{
    Map<String, FileSystemEntry> children;

    public Folder(String name) {
        super(name);
        children = new HashMap<>();
    }
    public FileSystemEntry getChild(String name){
        return children.get(name);
    }
    public boolean hasChild(String entry){
        if(children.containsKey(entry))
            return true;
        return false;
    }
    public boolean addChild(FileSystemEntry entry){
        //check if duplicate
        if (children.containsKey(entry.getName())) {
            throw new IllegalStateException("Already exists");
        }
        children.put(entry.getName(), entry);
        entry.setParent(this);
        return true;
    }

    public FileSystemEntry removeChild(String name){
        //check if duplicate
        if (!children.containsKey(name)) {
            throw new IllegalStateException("Already exists");
        }
        FileSystemEntry entry = children.remove(name);
        entry.setParent(null);
        return entry;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }
}
