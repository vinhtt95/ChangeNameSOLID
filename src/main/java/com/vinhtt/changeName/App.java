package com.vinhtt.changeName;

import atlantafx.base.theme.CupertinoDark;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.Taskbar;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class App extends Application {

    // Đường dẫn tương đối tới file icon (tính từ vị trí file App.class)
    private static final String ICON_PATH = "assets/icon.png";

    @Override
    public void start(Stage stage) throws IOException {
        // 1. Kích hoạt Theme macOS Dark Mode từ thư viện AtlantaFX
        Application.setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());

        // 2. Load giao diện từ FXML
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/MainView.fxml"));
        Parent root = fxmlLoader.load();

        // 3. Khởi tạo Scene
        Scene scene = new Scene(root, 1000, 700);

        // 4. Load thêm CSS tùy chỉnh (nếu cần override font size hoặc màu sắc cụ thể)
        // Lưu ý: Đảm bảo file styles.css tồn tại, nếu không có thể comment dòng này lại
        try {
            scene.getStylesheets().add(
                    Objects.requireNonNull(App.class.getResource("assets/styles.css")).toExternalForm()
            );
        } catch (NullPointerException e) {
            System.err.println("Warning: styles.css not found. Using default AtlantaFX theme only.");
        }

        // 5. Cấu hình Icon cho ứng dụng (Window Title & Mac Dock)
        setupAppIcon(stage);

        stage.setTitle("Video Organizer Pro");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Thiết lập icon cho ứng dụng.
     * Xử lý riêng cho JavaFX Stage (Windows/Linux taskbar + Title bar)
     * và java.awt.Taskbar (macOS Dock).
     */
    private void setupAppIcon(Stage stage) {
        // A. Set icon cho JavaFX Stage (Hiển thị trên thanh tiêu đề cửa sổ)
        try (InputStream iconStream = App.class.getResourceAsStream(ICON_PATH)) {
            if (iconStream != null) {
                stage.getIcons().add(new Image(iconStream));
            } else {
                System.err.println("Warning: Icon not found at " + ICON_PATH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // B. Set icon cho macOS Dock (Chỉ chạy trên macOS)
        // Cần dùng luồng mới vì luồng cũ (iconStream trên) đã đóng hoặc đọc hết
        if (Taskbar.isTaskbarSupported()) {
            Taskbar taskbar = Taskbar.getTaskbar();
            if (taskbar.isSupported(Taskbar.Feature.ICON_IMAGE)) {
                try (InputStream dockIconStream = App.class.getResourceAsStream(ICON_PATH)) {
                    if (dockIconStream != null) {
                        // Dùng ImageIO đọc stream thành java.awt.Image
                        java.awt.Image awtImage = ImageIO.read(dockIconStream);
                        taskbar.setIconImage(awtImage);
                    }
                } catch (IOException e) {
                    System.err.println("Error setting Mac Dock icon: " + e.getMessage());
                }
            }
        }
    }

    public static void main(String[] args) {
        launch();
    }
}