package com.vinhtt.changeName.service.impl;

import com.vinhtt.changeName.service.IRenamingStrategy;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;
import com.vinhtt.changeName.util.PathUtils;

public class ParentFolderRenamingStrategy implements IRenamingStrategy {

    @Override
    public Optional<Path> calculateNewPath(File sourceFile) {
        if (sourceFile.isDirectory()) return Optional.empty();

        File parent = sourceFile.getParentFile();
        if (parent == null) return Optional.empty();

        String parentName = parent.getName();
        String fileName = sourceFile.getName();

        // Logic cũ: Kiểm tra tên file có chứa tên folder cha không (case-insensitive)
        if (fileName.toLowerCase().contains(parentName.toLowerCase())) {
            String extension = PathUtils.getExtension(fileName);
            String newName = parentName + (extension.isEmpty() ? "" : "." + extension);

            // Move ra khỏi folder cha (lên 1 cấp: parent của parent)
            File grandParent = parent.getParentFile();
            if (grandParent != null) {
                return Optional.of(grandParent.toPath().resolve(newName));
            }
        }
        return Optional.empty();
    }
}