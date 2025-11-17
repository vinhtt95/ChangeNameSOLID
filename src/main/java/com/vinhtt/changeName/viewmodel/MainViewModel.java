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

    public StringProperty sourceFolderPathProperty() {
        return sourceFolderPath;
    }

    public ObjectProperty<TreeItem<FileTreeItemViewModel>> leftTreeRootProperty() {
        return leftTreeRoot;
    }

    public ObjectProperty<TreeItem<FileTreeItemViewModel>> rightTreeRootProperty() {
        return rightTreeRoot;
    }

    /**
     * Loads the folder structure from the specified directory.
     * Initializes both the source tree (left) and the preview tree (right).
     *
     * @param folder The root directory to load.
     */
    public void loadFolder(File folder) {
        sourceFolderPath.set(folder.getAbsolutePath());

        TreeItem<FileTreeItemViewModel> leftRoot = createSourceTreeItem(folder);
        leftTreeRoot.set(leftRoot);

        List<FileItem> allItems = collectAllItems(leftRoot);
        applyRenamingStrategy(allItems);

        TreeItem<FileTreeItemViewModel> rightRoot = buildPreviewTree(folder, allItems);
        rightTreeRoot.set(rightRoot);
    }

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

    private void applyRenamingStrategy(List<FileItem> items) {
        for (FileItem item : items) {
            if (!item.isDirectory()) {
                Optional<Path> newPath = renamingStrategy.calculateNewPath(item.getSourceFile());
                newPath.ifPresent(item::setDestinationPath);
            }
        }
    }

    private TreeItem<FileTreeItemViewModel> buildPreviewTree(File rootFolder, List<FileItem> items) {
        FileItem rootItem = new FileItem(rootFolder);
        TreeItem<FileTreeItemViewModel> rootTreeItem = new TreeItem<>(new FileTreeItemViewModel(rootItem));
        rootTreeItem.setExpanded(true);

        Map<String, TreeItem<FileTreeItemViewModel>> folderNodes = new HashMap<>();
        folderNodes.put(rootFolder.getAbsolutePath(), rootTreeItem);

        for (FileItem item : items) {
            if (item.isDirectory()) continue;

            Path effectivePath = item.getDestinationPath() != null ? item.getDestinationPath() : item.getSourceFile().toPath();

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

        if (cache.containsKey(pathStr)) {
            return cache.get(pathStr);
        }

        if (!pathStr.startsWith(rootFolder.getAbsolutePath())) {
            return null;
        }

        TreeItem<FileTreeItemViewModel> grandParentNode = getOrCreateParentNode(rootFolder, targetParentPath.getParent(), cache);

        if (grandParentNode != null) {
            File folderFile = targetParentPath.toFile();
            FileItem folderItem = new FileItem(folderFile);

            TreeItem<FileTreeItemViewModel> folderNode = new TreeItem<>(new FileTreeItemViewModel(folderItem));
            folderNode.setExpanded(true);

            grandParentNode.getChildren().add(folderNode);
            cache.put(pathStr, folderNode);
            return folderNode;
        }

        return null;
    }

    private List<FileItem> collectAllItems(TreeItem<FileTreeItemViewModel> root) {
        List<FileItem> list = new ArrayList<>();
        if (root == null) return list;

        if (root.getValue() != null) list.add(root.getValue().getModel());
        for (TreeItem<FileTreeItemViewModel> child : root.getChildren()) {
            list.addAll(collectAllItems(child));
        }
        return list;
    }

    /**
     * Manually updates the destination name of a specific file item.
     * Preserves the original file extension.
     *
     * @param treeItem The tree item containing the file to rename.
     * @param newNameWithoutExt The new filename entered by the user (without extension).
     */
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
            FileTreeItemViewModel currentViewModel = treeItem.getValue();
            treeItem.setValue(null);
            treeItem.setValue(currentViewModel);
        }
    }

    /**
     * Executes the move operation for a single selected item.
     * Updates both trees: marks as DONE in preview tree, removes from source tree.
     *
     * @param rightTreeItem The tree item from the preview tree to process.
     */
    public void runSingleItem(TreeItem<FileTreeItemViewModel> rightTreeItem) {
        FileItem item = rightTreeItem.getValue().getModel();

        if (item != null && !item.isDirectory() && item.getDestinationPath() != null && item.getStatus() != FileStatus.DONE) {
            boolean success = fileSystemService.moveFile(item);

            if (success) {
                FileTreeItemViewModel vm = rightTreeItem.getValue();
                rightTreeItem.setValue(null);
                rightTreeItem.setValue(vm);

                if (leftTreeRoot.get() != null) {
                    removeItemFromTree(leftTreeRoot.get(), item);
                }
            }
        }
    }

    private boolean removeItemFromTree(TreeItem<FileTreeItemViewModel> root, FileItem itemToRemove) {
        Iterator<TreeItem<FileTreeItemViewModel>> iterator = root.getChildren().iterator();
        while (iterator.hasNext()) {
            TreeItem<FileTreeItemViewModel> child = iterator.next();

            if (child.getValue() != null && child.getValue().getModel() == itemToRemove) {
                iterator.remove();
                return true;
            }

            if (removeItemFromTree(child, itemToRemove)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generates a physical preview of the file structure (Dry Run).
     */
    public void executeDryRun() {
        String pathStr = sourceFolderPath.get();
        if (pathStr == null || pathStr.isEmpty()) return;

        File rootFolder = new File(pathStr);
        if (!rootFolder.exists()) return;

        List<FileItem> allItems = collectAllItems(rightTreeRoot.get());

        dryRunService.executeDryRun(allItems, rootFolder);
    }

    /**
     * Executes the move operation for all items in the preview tree.
     * Reloads the folder structure upon completion.
     */
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