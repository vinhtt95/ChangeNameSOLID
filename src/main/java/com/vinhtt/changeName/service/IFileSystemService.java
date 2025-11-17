package com.vinhtt.changeName.service;

import com.vinhtt.changeName.model.FileItem;

public interface IFileSystemService {
    boolean moveFile(FileItem item);
    void deleteEmptyDirectories(FileItem item); // Xóa folder cha cũ nếu rỗng
    void deletePartsFiles(FileItem item); // Xóa file .parts
}