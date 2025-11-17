package com.vinhtt.changeName.model;

import java.io.File;
import java.nio.file.Path;

public class FileItem {
    private final File sourceFile;
    private Path destinationPath; // Có thể null nếu file không cần di chuyển/đổi tên
    private FileStatus status;
    private final boolean isDirectory;

    public FileItem(File sourceFile) {
        this.sourceFile = sourceFile;
        this.isDirectory = sourceFile.isDirectory();
        this.status = FileStatus.PENDING;
        this.destinationPath = null; // Mặc định giữ nguyên
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public Path getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(Path destinationPath) {
        this.destinationPath = destinationPath;
    }

    public FileStatus getStatus() {
        return status;
    }

    public void setStatus(FileStatus status) {
        this.status = status;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    // Helper để hiển thị trên UI (TreeView dùng toString mặc định nếu không set CellFactory)
    @Override
    public String toString() {
        if (destinationPath != null) {
            return destinationPath.getFileName().toString();
        }
        return sourceFile.getName();
    }
}