package com.vinhtt.changeName.viewmodel;

import com.vinhtt.changeName.model.FileItem;

public class FileTreeItemViewModel {

    private final FileItem model;

    public FileTreeItemViewModel(FileItem model) {
        this.model = model;
    }

    public FileItem getModel() {
        return model;
    }

    public boolean isDirectory() {
        return model.isDirectory();
    }

    /**
     * Logic hiển thị được chuyển về đây thay vì nằm trong Model.
     * TreeView sẽ gọi hàm này để render text nếu không dùng CellFactory tùy chỉnh phức tạp.
     */
    @Override
    public String toString() {
        // Nếu file có destinationPath (đã được lên lịch đổi tên/di chuyển)
        // thì hiển thị tên file đích để user preview.
        if (model.getDestinationPath() != null) {
            return model.getDestinationPath().getFileName().toString();
        }
        return model.getSourceFile().getName();
    }
}