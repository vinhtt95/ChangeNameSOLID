package com.vinhtt.changeName.view.components;

import com.vinhtt.changeName.model.FileItem;
import com.vinhtt.changeName.viewmodel.FileTreeItemViewModel; // Import ViewModel
import com.vinhtt.changeName.viewmodel.MainViewModel;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

// Sửa Generic Type từ <FileItem> thành <FileTreeItemViewModel>
public class PreviewTreeCell extends TreeCell<FileTreeItemViewModel> {

    private TextField textField;
    private final ContextMenu contextMenu = new ContextMenu();
    private final MainViewModel viewModel;

    public PreviewTreeCell(MainViewModel viewModel) {
        this.viewModel = viewModel;
        createContextMenu();
    }

    private void createContextMenu() {
        MenuItem runItem = new MenuItem("Run This File");
        runItem.setOnAction(e -> {
            // getTreeItem() bây giờ trả về TreeItem<FileTreeItemViewModel>, khớp với MainViewModel
            if (getTreeItem() != null) viewModel.runSingleItem(getTreeItem());
        });

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> startEdit());

        contextMenu.getItems().addAll(runItem, renameItem);
    }

    @Override
    public void startEdit() {
        if (getItem() == null || getItem().isDirectory()) return;
        super.startEdit();

        if (textField == null) {
            createTextField();
        }
        setText(null);
        setGraphic(textField);
        textField.selectAll();
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem().toString()); // Sử dụng toString() của ViewModel
        setGraphic(getTreeItem().getGraphic());
    }

    @Override
    protected void updateItem(FileTreeItemViewModel item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setContextMenu(null);
        } else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setText(null);
                setGraphic(textField);
            } else {
                setText(getString());
                setGraphic(getTreeItem().getGraphic());

                // Truy cập Model thông qua ViewModel để check logic hiển thị màu sắc
                if (item.getModel().getDestinationPath() != null) {
                    setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                } else {
                    setStyle("");
                }
            }
            setContextMenu(contextMenu);
        }
    }

    private void createTextField() {
        // Lấy tên file hiện tại (đã xử lý logic trong ViewModel)
        textField = new TextField(getStringWithoutExtension());
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ENTER) {
                String newName = textField.getText();
                viewModel.manualRename(getTreeItem(), newName);
                commitEdit(getItem());
            } else if (t.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    private String getStringWithoutExtension() {
        String s = getString();
        int dot = s.lastIndexOf('.');
        return (dot > 0) ? s.substring(0, dot) : s;
    }
}