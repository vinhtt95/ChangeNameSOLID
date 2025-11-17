package com.vinhtt.changeName.view;

import com.vinhtt.changeName.view.components.PreviewTreeCell;
import com.vinhtt.changeName.viewmodel.FileTreeItemViewModel; // Import ViewModel
import com.vinhtt.changeName.viewmodel.MainViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.stage.DirectoryChooser;

import java.io.File;

public class MainController {

    @FXML private TextField txtPath;

    // Sửa Generic Type của TreeView
    @FXML private TreeView<FileTreeItemViewModel> leftTree;
    @FXML private TreeView<FileTreeItemViewModel> rightTree;

    private MainViewModel viewModel;

    public void initialize() {
        viewModel = new MainViewModel();

        // Binding
        txtPath.textProperty().bindBidirectional(viewModel.sourceFolderPathProperty());

        // Binding root property (Type đã khớp)
        leftTree.rootProperty().bind(viewModel.leftTreeRootProperty());
        rightTree.rootProperty().bind(viewModel.rightTreeRootProperty());

        // Setup Right Tree Cell Factory (Editable)
        rightTree.setEditable(true);
        rightTree.setCellFactory(tv -> new PreviewTreeCell(viewModel));

        // Setup Left Tree (Optional: để hiển thị đẹp hơn cũng nên dùng CellFactory đơn giản hoặc toString mặc định của ViewModel)
    }

    @FXML
    private void onBrowse() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Folder to Organize");
        File selectedDirectory = directoryChooser.showDialog(txtPath.getScene().getWindow());

        if (selectedDirectory != null) {
            viewModel.loadFolder(selectedDirectory);
        }
    }

    @FXML
    private void onDryRunPreview() {
        viewModel.executeDryRun();
    }

    @FXML
    private void onRunAll() {
        viewModel.executeRunAll();
    }
}