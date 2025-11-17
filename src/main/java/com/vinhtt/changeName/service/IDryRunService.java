package com.vinhtt.changeName.service;

import com.vinhtt.changeName.model.FileItem;
import java.io.File;
import java.util.List;

public interface IDryRunService {
    /**
     * @param items Danh sách các file cần tạo preview
     * @param rootFolder Thư mục gốc mà user đã chọn (để tính relative path)
     */
    void executeDryRun(List<FileItem> items, File rootFolder);
}