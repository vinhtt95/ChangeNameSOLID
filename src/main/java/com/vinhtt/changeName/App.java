package com.vinhtt.changeName;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Áp dụng Theme macOS Dark Mode (CupertinoDark)
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/MainView.fxml"));
        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root, 1000, 700); // Tăng kích thước mặc định lên một chút vì font to

        // 2. Load CSS tùy chỉnh (Font size 16px)
        scene.getStylesheets().add(
                Objects.requireNonNull(App.class.getResource("assets/styles.css")).toExternalForm()
        );

        // 3. Set App Icon
        // Lưu ý: Đảm bảo bạn đã copy file icon.png vào folder resources/com/vinhtt/changeName/assets/
        try {
            Image icon = new Image(Objects.requireNonNull(App.class.getResourceAsStream("assets/icon.png")));
            stage.getIcons().add(icon);

            // Cho Mac Dock Icon (đôi khi cần set riêng trên macOS)
            // java.awt.Taskbar.getTaskbar().setIconImage(...); // Nếu cần thiết sau này
        } catch (Exception e) {
            System.out.println("Icon not found, using default.");
        }

        stage.setTitle("Change Name");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}