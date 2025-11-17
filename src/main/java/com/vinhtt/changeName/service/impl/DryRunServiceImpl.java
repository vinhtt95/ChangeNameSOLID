package com.vinhtt.changeName.service.impl;

import com.vinhtt.changeName.model.FileItem;
import com.vinhtt.changeName.service.IDryRunService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class DryRunServiceImpl implements IDryRunService {

    // Đường dẫn cứng cho folder temp giống logic cũ của bạn
    private static final String DRY_RUN_BASE_PATH = "/Users/vinhtt/Downloads/Temp/Videos/Clone";

    @Override
    public void executeDryRun(List<FileItem> items, File rootFolder) {
        File baseDir = new File(DRY_RUN_BASE_PATH);

        // 1. Dọn dẹp folder temp cũ
        if (baseDir.exists()) {
            deleteDirectory(baseDir);
        }
        if (!baseDir.mkdirs()) {
            System.err.println("Cannot create dry run directory: " + baseDir.getAbsolutePath());
            return;
        }

        Path rootPath = rootFolder.toPath();

        // 2. Tạo cấu trúc file giả lập
        for (FileItem item : items) {
            if (item.isDirectory()) continue;

            // Lấy đường dẫn đích (nếu đã rename) hoặc đường dẫn gốc (nếu giữ nguyên)
            Path effectivePath = item.getDestinationPath() != null
                    ? item.getDestinationPath()
                    : item.getSourceFile().toPath();

            try {
                // Logic cốt lõi: Tính đường dẫn tương đối từ Root -> Đích
                // Ví dụ Root: /Videos
                // File đích: /Videos/PhimA.mkv (đã move up từ /Videos/PhimA/PhimA.mkv)
                // Relative: PhimA.mkv
                // Kết quả DryRun: /Temp/Clone/PhimA.mkv

                Path relativePath;
                try {
                    relativePath = rootPath.relativize(effectivePath);
                } catch (IllegalArgumentException e) {
                    // Trường hợp file bị move ra khỏi phạm vi root folder (ít gặp với logic hiện tại)
                    // Fallback: Chỉ dùng tên file
                    relativePath = effectivePath.getFileName();
                }

                File dryRunFile = new File(baseDir, relativePath.toString());

                // Tạo các thư mục cha trong folder Clone nếu cần
                if (!dryRunFile.getParentFile().exists()) {
                    dryRunFile.getParentFile().mkdirs();
                }

                // Tạo file rỗng
                dryRunFile.createNewFile();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 3. Mở Finder
        try {
            Desktop.getDesktop().open(baseDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }
}