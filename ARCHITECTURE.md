# Architecture Design: Video Structure Organizer (JavaFX + MVVM)

Tài liệu này mô tả kiến trúc phần mềm cho ứng dụng sắp xếp thư mục video, được chuyển đổi từ project Java CLI cũ sang ứng dụng Desktop sử dụng JavaFX. Dự án tuân thủ nghiêm ngặt mô hình MVVM và các nguyên lý thiết kế SOLID.

## 1. Tổng quan Kiến trúc (High-Level Architecture)

Dự án được chia thành 3 lớp chính (Layers) theo mô hình MVVM:

* **Model Layer:** Chứa các thực thể dữ liệu (`Entity`) và logic nghiệp vụ cốt lõi (`Business Logic`). Nơi này hoàn toàn độc lập với giao diện người dùng.
* **ViewModel Layer:** Đóng vai trò trung gian, chứa trạng thái của UI (`State`) và các lệnh xử lý (`Commands`). Nó biến đổi dữ liệu từ Model thành định dạng mà View có thể hiển thị.
* **View Layer:** Giao diện người dùng (FXML và CSS). Chỉ chịu trách nhiệm hiển thị và liên kết dữ liệu (Data Binding) với ViewModel.

## 2. Cấu trúc Thư mục (Project Structure)

```text
src/
├── main/
│   ├── java/
│   │   └── com/vinhtt/changeName/
│   │       ├── App.java                  // Entry point
│   │       ├── model/                    // Data Entities
│   │       │   ├── FileItem.java         // Đại diện cho 1 file/folder
│   │       │   └── FileStatus.java       // Enum (PENDING, DONE, ERROR)
│   │       ├── service/                  // Business Logic (SOLID: SRP)
│   │       │   ├── impl/
│   │       │   │   ├── FileSystemServiceImpl.java
│   │       │   │   ├── DryRunServiceImpl.java
│   │       │   │   └── ParentFolderRenamingStrategy.java
│   │       │   ├── IFileSystemService.java
│   │       │   ├── IDryRunService.java
│   │       │   └── IRenamingStrategy.java
│   │       ├── view/                     // UI Controllers (Code-behind)
│   │       │   ├── MainController.java
│   │       │   └── components/
│   │       │       └── PreviewTreeCell.java // Custom Cell để edit tên file
│   │       ├── viewmodel/                // Presentation Logic
│   │       │   ├── MainViewModel.java
│   │       │   └── FileTreeItemViewModel.java
│   │       └── util/
│   │           └── PathUtils.java
│   └── resources/
│       └── com/vinhtt/changeName/
│           └── view/
│               └── MainView.fxml         // Giao diện chính
└── test/
```

## 3. Chi tiết Các Thành Phần (Component Details)

### A. Model & Services (Business Logic Layer)

Áp dụng **Single Responsibility Principle (SRP)** và **Open/Closed Principle (OCP)**.

1.  **`FileItem` (Entity):**
    * Chứa thông tin: `sourcePath`, `destinationPath`, `isDirectory`, `status`.
    * Đây là POJO (Plain Old Java Object) thuần túy.

2.  **`IRenamingStrategy` (Interface):**
    * Định nghĩa quy tắc đổi tên.
    * **Lợi ích:** Nếu sau này muốn đổi tên theo Regex khác, chỉ cần tạo class mới implement interface này mà không sửa code cũ (**OCP**).
    * *Implementation:* `ParentFolderRenamingStrategy` (Logic cũ: lấy tên folder cha làm tên file).

3.  **`IDryRunService` (Interface):**
    * Chịu trách nhiệm tạo cấu trúc file giả lập (Physical Preview).
    * *Implementation:* `DryRunServiceImpl` - Tạo folder thật và file rỗng (0 KB) tại đường dẫn đích để người dùng kiểm tra bằng Finder.

4.  **`IFileSystemService` (Interface):**
    * Chịu trách nhiệm thao tác file thật (Move, Delete, Rename).
    * Tách biệt việc tính toán đường dẫn (Strategy) và việc thực thi I/O (Service).

### B. ViewModel Layer

Áp dụng **Dependency Inversion Principle (DIP)**: ViewModel phụ thuộc vào Interfaces (Services), không phụ thuộc implementation cụ thể.

1.  **`MainViewModel`:**
    * **Properties:**
        * `sourceFolderPath`: StringProperty (Binding với TextField).
        * `leftTreeRoot`: ObjectProperty (Binding với TreeView bên trái).
        * `rightTreeRoot`: ObjectProperty (Binding với TreeView bên phải).
    * **Commands (Methods):**
        * `loadFolder(Path)`: Gọi Service đọc file, dựng cây LeftTree. Sau đó chạy Strategy để dựng cây RightTree dự kiến.
        * `updateManualRename(FileItem, String newName)`: Cập nhật `destinationPath` khi user sửa tên trên UI.
        * `executeDryRun()`: Gọi `IDryRunService`.
        * `executeRunAll()`: Gọi `IFileSystemService` cho toàn bộ list.

### C. View Layer

1.  **`MainView.fxml`:**
    * Sử dụng `SplitPane` chia đôi màn hình.
    * Cột trái: `TreeView` (Read-only).
    * Cột phải: `TreeView` (Editable).

2.  **`PreviewTreeCell`:**
    * Custom Cell Factory cho JavaFX TreeView.
    * Xử lý logic UI: Double click để hiện TextField nhập tên mới.
    * Chỉ cho phép sửa phần tên (Name), giữ nguyên phần mở rộng (Extension) ẩn bên dưới hoặc read-only.