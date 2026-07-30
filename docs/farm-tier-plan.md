# Farm Tier Plan

Mục tiêu: chia vòng lặp farm hiện tại thành các tầng `đầu game / giữa game / cuối game` để dễ cân bằng và theo dõi.

## 1. Nhóm tầng farm theo map hiện tại

### Tầng 1: Early game

Nhóm map phù hợp:
- `Map 3 hành tinh`
  - Theo `MapService.isMap3Planets(...)`
  - Bao trùm phần lớn map thường đầu game

- `Map up SKH`
  - Theo `MapService.isMapUpSKH(...)`
  - Gồm:
    - `1, 2, 3`
    - `8, 9, 11`
    - `15, 16, 17`

Vai trò:
- map treo cơ bản
- farm sức mạnh / tiềm năng nền
- farm vàng nhỏ
- farm item cơ bản
- farm item task

### Tầng 2: Mid game

Nhóm map phù hợp:
- `Map Nappa`
  - `63 -> 83`

- `Map tương lai`
  - Theo `MapService.isMapTuongLai(...)`

- `Map lạnh`
  - `105 -> 110`

Vai trò:
- chuyển từ farm sinh tồn sang farm có mục đích
- kiếm vàng ổn hơn
- kiếm đá / item kích hoạt / item hỗ trợ set
- bắt đầu có rare drop đáng giá

### Tầng 3: Late game / Special farm

Nhóm map phù hợp:
- `Map phó bản`
  - kho báu
  - doanh trại
  - con đường rắn độc
  - khí gas hủy diệt

- `Map ngục tù`
  - `155`

- `Map ngũ hành sơn`
  - `122 -> 124`

- `Map hành tinh thực vật`
  - `160 -> 163`

- `Map riêng tư`
  - `186`

Vai trò:
- farm nguyên liệu chuyên biệt
- farm item hiếm
- farm token đặc thù
- farm đồ phục vụ nâng tiến trình hoặc endgame utility

## 2. Reward loop hiện tại theo tầng

### Early game

Nguồn thưởng đang thấy:
- `SM/TN` từ combat
- vàng map thường:
  - tỉ lệ khoảng `1/20`
  - lượng `500 -> 3000`
- item nhiệm vụ
- item cơ bản:
  - ngọc rồng thường
  - sao pha lê
  - đá nâng cấp
  - item `225`
- một số item kích hoạt / đồ sao ở map up SKH

Điểm mạnh:
- có cảm giác đánh là có rơi
- hợp treo máy

Điểm yếu:
- vàng đầu game còn mỏng
- ngọc gần như không đáng kể nếu chỉ nhìn farm tự nhiên
- chưa có token farm đầu game rõ ràng để đổi quà

### Mid game

Nguồn thưởng đang thấy:
- vàng tăng rõ ở map Nappa / tương lai / lạnh
- đồ kích hoạt
- đồ sao / đá nâng cấp
- đồ thần linh / set thần / mảnh thiên sứ theo điều kiện
- item hiếm map-specific

Điểm mạnh:
- đã bắt đầu có cảm giác farm có mục tiêu

Điểm yếu:
- chưa có bảng mục tiêu farm rõ ràng
- người chơi khó biết:
  - map nào farm vàng tốt
  - map nào farm đá tốt
  - map nào farm item set tốt

### Late game / Special farm

Nguồn thưởng đang thấy:
- item chuyên map
- token / nguyên liệu riêng
- đồ hiếm
- phần thưởng gắn điều kiện set / active / trạng thái nhân vật

Điểm mạnh:
- đủ nền để làm endgame farm loop

Điểm yếu:
- reward đang phân tán nhiều nhánh
- chưa đóng gói thành đích đến rõ ràng cho người chơi

## 3. Đề xuất mục tiêu farm cho từng tầng

### Early game nên farm để lấy gì

Mục tiêu:
- vàng cơ bản
- item nhiệm vụ
- token đổi quà tân thủ
- tài nguyên đủ để không bị khựng nhịp chơi

Đề xuất:
- thêm `token early game` rơi nhẹ ở `Map 3 hành tinh` và `Map up SKH`
- token này đổi ở `Ong Gohan` lấy:
  - vàng
  - ngọc khóa
  - đậu
  - item hồi phục
  - vật phẩm hỗ trợ cày

### Mid game nên farm để lấy gì

Mục tiêu:
- vàng ổn định
- đá nâng cấp
- item kích hoạt
- item tiến tới set / build

Đề xuất:
- chia rõ vai trò:
  - `Nappa`: vàng + đồ đệm
  - `Tương lai`: vàng + đá / item nâng cấp
  - `Map lạnh`: đồ giá trị cao hơn + rare drop

### Late game nên farm để lấy gì

Mục tiêu:
- nguyên liệu hiếm
- token map đặc biệt
- đồ build / đồ sưu tầm / đồ utility

Đề xuất:
- mỗi cụm map đặc biệt nên có một danh tính rõ:
  - map A farm nguyên liệu X
  - map B farm token Y
  - map C farm đồ hiếm Z

## 4. Khoảng thiếu nên lấp trước

### Thiếu 1: Token farm đầu game

Hiện đầu game có rơi đồ, nhưng chưa có đồng tiền gameplay rõ ràng để người chơi cảm thấy:
- "cày quái một lúc là đổi được cái gì đó chắc chắn"

Đề xuất:
- tạo token farm đầu game
- đổi ở `Ong Gohan`

### Thiếu 2: Quà nhiệm vụ chính

`TaskService.rewardDoneTask(...)` đang để trống.

Đây là khe rất tốt để bơm:
- vàng
- ngọc khóa
- token farm
- item hỗ trợ treo máy

### Thiếu 3: Chỉ dẫn farm ingame

Người chơi hiện khó tự hiểu:
- farm map nào để lấy gì

Đề xuất:
- `Ong Gohan` hoặc NPC tương ứng có menu gợi ý:
  - "Đầu game nên farm ở đâu"
  - "Muốn kiếm vàng thì đi đâu"
  - "Muốn kiếm đá thì đi đâu"

## 5. Đề xuất bảng cân bằng sơ bộ

### Early

- Reward chính:
  - `SM/TN`
  - vàng nhỏ
  - token early
  - item nhiệm vụ

- Không nên kỳ vọng:
  - đồ hiếm lớn
  - ngọc rơi trực tiếp nhiều

### Mid

- Reward chính:
  - vàng vừa
  - đá nâng cấp
  - item kích hoạt
  - item map-specific

### Late

- Reward chính:
  - token đặc biệt
  - đồ hiếm
  - nguyên liệu build
  - rare cosmetic / utility

## 6. Việc nên làm tiếp

- [ ] Chọn 1 item làm token early game
- [ ] Thiết kế bảng đổi quà token ở `Ong Gohan`
- [ ] Điền thưởng thật vào `rewardDoneTask(...)`
- [ ] Log chi tiết hơn các drop map giữa
- [ ] Chốt danh tính từng map late game
