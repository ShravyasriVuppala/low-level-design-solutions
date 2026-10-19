package filesystem;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/*
    "Design an in-memory file system that supports creating files and directories,
    navigating paths, and basic file operations."
    Requirements
    1. Single root system - supports create, delete, rename, move, get contents of file/folder
    2. File should store string content
    3. Reject invalid operations
    4. System should be thread safe

    Entities
    File - name, string content
    Folder - name, children<FileSystemEntry>
    FileSystemEntry - Abstract class
    FileSystem - Orchestrator
 */
public class FileSystem {
    private final FileSystemEntry root;

    public FileSystem() {
        this.root = new Folder("/");
    }

    //create file: path - /home/users/dev/notes.txt
    public FileSystemEntry createFile(String path, String content){
        //resolve path and get parent
        //create file and add to parent
        Folder parent = resolveParent(path);
        String name = extractName(path);

        File file = new File(name, content);
        parent.addChild(file);
        return file;
    }
    //remove file: path - /home/users/dev/notes.txt
    public void delete(String path, String content){
        //resolve path and get parent
        //create file and add to parent
        Folder parent = resolveParent(path);
        String name = extractName(path);
        FileSystemEntry entry = parent.removeChild(name);
        if(entry == null){
            throw new IllegalArgumentException("Entry not found "+ path);
        }
    }

    public void move(String srcPath, String destPath) {
        if (srcPath.equals("/")) {
            throw new IllegalArgumentException("Cannot move root");
        }
        Folder srcParent = resolveParent(srcPath);
        String srcName = extractName(srcPath);
        FileSystemEntry entry = srcParent.getChild(srcName);

        if (entry == null) {
            throw new IllegalArgumentException("Source not found: " + srcPath);
        }

        Folder destParent = resolveParent(destPath);
        String destName = extractName(destPath);
        //if entry is directory, we need to check for loop. dest should not be under src hierarchy
        if (entry.isDirectory()) {
            Folder current = destParent;
            while (current != null) {
                if (current == entry) {
                    throw new IllegalArgumentException("Cannot move folder into itself");
                }
                current = current.getParent();
            }
        }

        if (destParent.hasChild(destName)) {
            throw new IllegalStateException("Destination already exists: " + destPath);
        }

        srcParent.removeChild(srcName);
        entry.setName(destName);
        destParent.addChild(entry);
    }


    //helper to get parent from path
    private Folder resolveParent(String path){
        int lastSlash = path.lastIndexOf('/');
        String parentPath = lastSlash == 0 ? "/" : path.substring(0, lastSlash);
        FileSystemEntry parent = resolvePath(parentPath);

        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent is not a directory");
        }
        return (Folder) parent;
    }

    private FileSystemEntry resolvePath(String path){
        //validates
        //get all folders in path and validate each folder
        String[] parts = path.substring(1).split("/");
        FileSystemEntry current = root;
        for(String part : parts){
            //this folder should be in current folder's children
            //this should be a directory
            if(!current.isDirectory()){
                throw new IllegalArgumentException("Not a directory");
            }
            FileSystemEntry child = ((Folder) current).getChild(part);
            if(child == null){
                throw new IllegalArgumentException("Path not found "+ path);
            }
            current = child;
        }
        return current;
    }


    private String extractName(String path){
        int idx = path.lastIndexOf('/');
        return path.substring(idx + 1);
    }
}

