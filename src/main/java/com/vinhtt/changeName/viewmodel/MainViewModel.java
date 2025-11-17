package com.vinhtt.changeName.viewmodel;

import com.vinhtt.changeName.model.FileItem;
import com.vinhtt.changeName.model.FileStatus;
import com.vinhtt.changeName.service.IDryRunService;
import com.vinhtt.changeName.service.IFileSystemService;
import com.vinhtt.changeName.service.IRenamingStrategy;
import com.vinhtt.changeName.service.impl.DryRunServiceImpl;
import com.vinhtt.changeName.service.impl.FileSystemServiceImpl;
import com.vinhtt.changeName.service.impl.ParentFolderRenamingStrategy;
import com.vinhtt.changeName.util.PathUtils;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class MainViewModel {

    private final StringProperty sourceFolderPath = new SimpleStringProperty();
    private final ObjectProperty<TreeItem<FileTreeItemViewModel>> leftTreeRoot = new SimpleObjectProperty<>();
    private final ObjectProperty<TreeItem<FileTreeItemViewModel>> rightTreeRoot = new SimpleObjectProperty<>();

    private final IRenamingStrategy renamingStrategy;
    private final IDryRunService dryRunService;
    private final IFileSystemService fileSystemService;

    public MainViewModel() {
        this.renamingStrategy = new ParentFolderRenamingStrategy();
        this.dryRunService = new DryRunServiceImpl();
        this.fileSystemService = new FileSystemServiceImpl();
    }

    public StringProperty sourceFolderPathProperty() { return sourceFolderPath; }
    public ObjectProperty<TreeItem<FileTreeItemViewModel>> leftTreeRootProperty() { return leftTreeRoot; }
    public ObjectProperty<TreeItem<FileTreeItemViewModel>> rightTreeRootProperty() { return rightTreeRoot; }

    /**
     * Load folder và dựng 2 cây:
     * - Left: Cấu trúc thực tế.
     * - Right: Cấu trúc dự kiến sau khi move (phẳng hóa và tái cấu trúc).
     */
    public void loadFolder(File folder) {
        sourceFolderPath.set(folder.getAbsolutePath());

        // 1. Build Left Tree (Source Structure) & Collect all items
        TreeItem<FileTreeItemViewModel> leftRoot = createSourceTreeItem(folder);
        leftTreeRoot.set(leftRoot);

        // 2. Calculate Paths & Build Right Tree (Preview Structure)
        List<FileItem> allItems = collectAllItems(leftRoot);
        applyRenamingStrategy(allItems);

        TreeItem<FileTreeItemViewModel> rightRoot = buildPreviewTree(folder, allItems);
        rightTreeRoot.set(rightRoot);
    }

    // --- Helper build Left Tree ---
    private TreeItem<FileTreeItemViewModel> createSourceTreeItem(File file) {
        FileItem itemModel = new FileItem(file);
        FileTreeItemViewModel itemViewModel = new FileTreeItemViewModel(itemModel);
        TreeItem<FileTreeItemViewModel> treeItem = new TreeItem<>(itemViewModel);
        treeItem.setExpanded(true);

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                Arrays.stream(children)
                        .filter(child -> !child.isHidden())
                        .forEach(child -> treeItem.getChildren().add(createSourceTreeItem(child)));
            }
        }
        return treeItem;
    }

    // --- Logic: Apply Strategy ---
    private void applyRenamingStrategy(List<FileItem> items) {
        for (FileItem item : items) {
            if (!item.isDirectory()) {
                Optional<Path> newPath = renamingStrategy.calculateNewPath(item.getSourceFile());
                newPath.ifPresent(item::setDestinationPath);
            }
        }
    }

    // --- Logic: Build Preview Tree from Destination Paths ---
    private TreeItem<FileTreeItemViewModel> buildPreviewTree(File rootFolder, List<FileItem> items) {
        // Root node cho cây Preview
        FileItem rootItem = new FileItem(rootFolder);
        TreeItem<FileTreeItemViewModel> rootTreeItem = new TreeItem<>(new FileTreeItemViewModel(rootItem));
        rootTreeItem.setExpanded(true);

        // Cache để tái sử dụng các node folder cha trong quá trình dựng cây
        Map<String, TreeItem<FileTreeItemViewModel>> folderNodes = new HashMap<>();
        folderNodes.put(rootFolder.getAbsolutePath(), rootTreeItem);

        for (FileItem item : items) {
            if (item.isDirectory()) continue; // Bỏ qua folder nguồn, ta sẽ tự tạo folder đích nếu cần

            // Xác định path hiển thị: Nếu có dest thì dùng dest, không thì dùng source
            Path effectivePath = item.getDestinationPath() != null ? item.getDestinationPath() : item.getSourceFile().toPath();

            // Tìm hoặc tạo các node folder cha trên cây Preview
            TreeItem<FileTreeItemViewModel> parentNode = getOrCreateParentNode(rootFolder, effectivePath.getParent(), folderNodes);

            if (parentNode != null) {
                TreeItem<FileTreeItemViewModel> fileNode = new TreeItem<>(new FileTreeItemViewModel(item));
                parentNode.getChildren().add(fileNode);
            }
        }
        return rootTreeItem;
    }

    private TreeItem<FileTreeItemViewModel> getOrCreateParentNode(File rootFolder, Path targetParentPath, Map<String, TreeItem<FileTreeItemViewModel>> cache) {
        String pathStr = targetParentPath.toAbsolutePath().toString();

        // Base case: Nếu path khớp với cache (bao gồm cả root)
        if (cache.containsKey(pathStr)) {
            return cache.get(pathStr);
        }

        // Security check: Không hiển thị folder nằm ngoài root đã chọn
        if (!pathStr.startsWith(rootFolder.getAbsolutePath())) {
            return null;
        }

        // Recursive: Tìm node ông nội
        TreeItem<FileTreeItemViewModel> grandParentNode = getOrCreateParentNode(rootFolder, targetParentPath.getParent(), cache);

        if (grandParentNode != null) {
            // Tạo node cha mới (Folder ảo trên cây Preview)
            File folderFile = targetParentPath.toFile();
            FileItem folderItem = new FileItem(folderFile);
            // Lưu ý: Folder ảo này chưa có status move, nó chỉ đại diện cấu trúc

            TreeItem<FileTreeItemViewModel> folderNode = new TreeItem<>(new FileTreeItemViewModel(folderItem));
            folderNode.setExpanded(true);

            grandParentNode.getChildren().add(folderNode);
            cache.put(pathStr, folderNode);
            return folderNode;
        }

        return null;
    }

    // --- Các hàm Utilities ---
    private List<FileItem> collectAllItems(TreeItem<FileTreeItemViewModel> root) {
        List<FileItem> list = new ArrayList<>();
        if (root == null) return list;

        if (root.getValue() != null) list.add(root.getValue().getModel());
        for (TreeItem<FileTreeItemViewModel> child : root.getChildren()) {
            list.addAll(collectAllItems(child));
        }
        return list;
    }

    // --- Giữ nguyên logic Command ---
    public void manualRename(TreeItem<FileTreeItemViewModel> treeItem, String newNameWithoutExt) {
        FileItem item = treeItem.getValue().getModel();
        if (item.isDirectory()) return;

        String currentName = (item.getDestinationPath() != null)
                ? item.getDestinationPath().getFileName().toString()
                : item.getSourceFile().getName();

        String ext = PathUtils.getExtension(currentName);
        String newFullName = newNameWithoutExt + (ext.isEmpty() ? "" : "." + ext);

        Path parentPath = (item.getDestinationPath() != null)
                ? item.getDestinationPath().getParent()
                : item.getSourceFile().toPath().getParent();

        if (parentPath != null) {
            item.setDestinationPath(parentPath.resolve(newFullName));
            // Refresh UI
            FileTreeItemViewModel currentViewModel = treeItem.getValue();
            treeItem.setValue(null);
            treeItem.setValue(currentViewModel);
        }
    }

    public void runSingleItem(TreeItem<FileTreeItemViewModel> treeItem) {
        FileItem item = treeItem.getValue().getModel();
        if (item != null && !item.isDirectory() && item.getDestinationPath() != null) {
            boolean success = fileSystemService.moveFile(item);
            if (success) {
                if (treeItem.getParent() != null) {
                    treeItem.getParent().getChildren().remove(treeItem);
                }
            }
        }
    }

    /**
     * Generates a physical preview of the file structure (Dry Run).
     */
    public void executeDryRun() {
        // Lấy root folder hiện tại từ đường dẫn đã nhập
        String pathStr = sourceFolderPath.get();
        if (pathStr == null || pathStr.isEmpty()) return;

        File rootFolder = new File(pathStr);
        if (!rootFolder.exists()) return;

        List<FileItem> allItems = collectAllItems(rightTreeRoot.get());

        // Truyền thêm rootFolder vào service
        dryRunService.executeDryRun(allItems, rootFolder);
    }

    public void executeRunAll() {
        List<FileItem> allItems = collectAllItems(rightTreeRoot.get());
        for (FileItem item : allItems) {
            if (item.getDestinationPath() != null && item.getStatus() != FileStatus.DONE) {
                fileSystemService.moveFile(item);
            }
        }
        loadFolder(new File(sourceFolderPath.get()));
    }
}