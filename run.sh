#!/bin/bash

# --- CẤU HÌNH ---
# Tên Main Class của bạn (đã cập nhật theo launcher mới)
MAIN_CLASS="com.vinhtt.changeName.Launcher"

# Kiểm tra xem Maven có được cài đặt không
if ! command -v mvn &> /dev/null; then
    echo "❌ Lỗi: Maven chưa được cài đặt. Vui lòng cài đặt Maven trước."
    exit 1
fi

echo "🚀 Đang chuẩn bị chạy Video Organizer Pro..."

# --- BƯỚC 1: COMPILE & BUILD ---
# Clean và Package lại để đảm bảo code mới nhất được áp dụng
echo "📦 Đang build dự án với Maven..."
mvn clean javafx:run

# Lưu ý: Lệnh 'mvn clean javafx:run' phía trên là cách chuẩn nhất
# để chạy JavaFX project hiện đại. Nó sẽ tự động xử lý:
# 1. Tải thư viện dependencies.
# 2. Compile code.
# 3. Cấu hình Module Path cho JavaFX.
# 4. Chạy class chính được khai báo trong pom.xml (App hoặc Launcher).

# Nếu lệnh trên chạy thành công, script sẽ kết thúc tại đó.
# Nếu bạn muốn chạy thủ công bằng java -jar (sau khi build), hãy dùng đoạn dưới đây:

# if [ $? -eq 0 ]; then
#     echo "✅ Build thành công! Đang khởi động..."
# else
#     echo "❌ Build thất bại. Vui lòng kiểm tra lỗi phía trên."
#     exit 1
# fi