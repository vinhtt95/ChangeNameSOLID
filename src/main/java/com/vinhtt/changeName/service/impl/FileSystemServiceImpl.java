package com.vinhtt.changeName.service.impl;

import com.vinhtt.changeName.model.FileItem;
import com.vinhtt.changeName.model.FileStatus;
import com.vinhtt.changeName.service.IFileSystemService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileSystemServiceImpl implements IFileSystemService {

    @Override
    public boolean moveFile(FileItem item) {
        if (item.getDestinationPath() == null) return false;

        try {
            Path source = item.getSourceFile().toPath();
            Path target = item.getDestinationPath();

            // Tạo thư mục cha nếu chưa tồn tại
            if (!Files.exists(target.getParent())) {
                Files.createDirectories(target.getParent());
            }

            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            item.setStatus(FileStatus.DONE);

            // Sau khi move thành công, clean dọn dẹp
            deletePartsFiles(item);
            deleteEmptyDirectories(item);

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            item.setStatus(FileStatus.ERROR);
            return false;
        }
    }

    @Override
    public void deleteEmptyDirectories(FileItem item) {
        // Logic: Xóa thư mục cha của file gốc (nơi file vừa rời đi)
        File parent = item.getSourceFile().getParentFile();
        if (parent != null && parent.exists() && parent.isDirectory()) {
            File[] files = parent.listFiles();
            if (files == null || files.length == 0) {
                parent.delete();
            }
        }
    }

    @Override
    public void deletePartsFiles(FileItem item) {
        File parent = item.getSourceFile().getParentFile();
        if (parent != null && parent.exists()) {
            File[] files = parent.listFiles((dir, name) -> name.endsWith(".parts"));
            if (files != null) {
                for (File f : files) f.delete();
            }
        }
    }
}