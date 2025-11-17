package com.vinhtt.changeName.service;

import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

public interface IRenamingStrategy {
    /**
     * Tính toán đường dẫn mới dựa trên file gốc.
     * @param sourceFile File gốc
     * @return Optional chứa Path mới nếu file khớp rule, empty nếu không đổi.
     */
    Optional<Path> calculateNewPath(File sourceFile);
}