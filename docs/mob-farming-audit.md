# Mob Farming Audit

Mục tiêu: log lại cơ chế `farm quái` hiện tại để phục vụ hướng phát triển "up quái là có hết".

## 1. Luồng hiện tại

### 1.1. Khi người chơi đánh quái

- File: `src/mob/Mob.java`
- Khi gây damage lên quái:
  - người chơi nhận `SM/TN` qua `Service.gI().addSMTN(...)`
  - người chơi tăng tiến độ luyện tập qua `TrainingService.gI().tangTnsmLuyenTap(...)`
  - nhiều hệ nhiệm vụ / achievement / badges cũng tăng theo

Điểm quan trọng:
- Người chơi đang nhận tiến bộ cơ bản ngay từ `damage`, không phải chờ đến cú last hit mới có gì.
- Điều này hợp với nhịp treo máy farm.

### 1.2. Khi quái chết

- File: `src/mob/Mob.java`
- Hàm chính:
  - `sendMobDieAffterAttacked(...)`
  - `mobReward(...)`
  - `getItemMobReward(...)`
  - `dropItemTask(...)`

Luồng:
1. Quái chết
2. Tạo danh sách item rơi qua `getItemMobReward(...)`
3. Nếu có item task thì cộng thêm qua `dropItemTask(...)`
4. Gửi item rơi ra map
5. Nếu người chơi có hút vật phẩm thì nhặt tự động

## 2. Tài nguyên người chơi đang lấy từ farm quái

### 2.1. Sức mạnh / tiềm năng

- Nhận trực tiếp từ việc gây damage lên quái.
- Đây là trục tiến bộ ổn định nhất hiện tại.

Nhận xét:
- Đây là nền rất tốt cho server casual.
- Nếu muốn cảm giác "đánh là mạnh lên", chỉ cần cân lại rate hoặc thêm vài mốc thưởng phụ là đủ.

### 2.2. Vàng

Trong `Mob.java` hiện có nhiều cụm drop vàng theo map:

- `Map 3 hành tinh`
  - tỉ lệ khoảng `1/20`
  - lượng vàng nhỏ `500-3000`

- `Map Nappa`
  - tỉ lệ khoảng `1/100`
  - lượng vàng `2000-6000`

- `Map lạnh`
  - tỉ lệ khoảng `30%`
  - lượng vàng cao hơn hẳn `150000-250000`

- `Map tương lai`
  - tỉ lệ khoảng `15%`
  - lượng vàng `80000-150000`

- `Map phó bản`
  - tỉ lệ khoảng `1%`
  - lượng vàng `80000-200000`

Nhận xét:
- Vàng đang lệch rất mạnh theo map.
- Một số map đầu cho vàng rất thấp, một số map giữa/sau cho vàng bật hẳn lên.
- Đây là chỗ nên log thêm theo tier map đầu / mid / late để tránh người chơi đầu game thấy farm quái "không ra gì".

### 2.3. Ngọc

- Có rơi ngọc trực tiếp:
  - `if (Util.isTrue(1, 1000000))` rồi rơi item `77`
  - nghĩa là cực hiếm

- Có một nhánh quà ngọc miễn phí qua:
  - `player.event.luotNhanNgocMienPhi`
  - khi thỏa điều kiện thì rơi 2 item `77`

Nhận xét:
- Nếu mục tiêu là "up quái là có hết", ngọc hiện tại vẫn quá hiếm nếu chỉ trông vào drop tự nhiên.
- Đây là một khoảng thiếu tài nguyên rõ rệt.

### 2.4. Item nhiệm vụ

- `dropItemTask(...)` xử lý item rơi cho task main.
- Đây là phần quan trọng để farm quái gắn chặt vào tiến trình nhiệm vụ.

Nhận xét:
- Cơ chế ổn.
- Nhưng thưởng hoàn thành task lại đang rất yếu vì `rewardDoneTask(...)` hiện để trống.

### 2.5. Item map-specific / event-specific

`Mob.java` hiện có rất nhiều nhánh rơi theo map:

- item event Noel / mùa
- item cho map lạnh
- item cho map tương lai
- item cho map doanh trại
- item cho map up kích hoạt
- item set kích hoạt
- item sao
- đá nâng cấp
- ngọc rồng / sao pha lê
- thỏi vàng `457`
- hồng ngọc `861`
- mảnh thiên sứ
- item riêng cho một số map farm đặc biệt

Nhận xét:
- Lượng drop hiện tại rất nhiều.
- Vấn đề không phải là "thiếu đồ rơi", mà là:
  - khó nhìn ra cái gì là core reward loop
  - cái gì là phụ
  - map nào đáng farm vì mục tiêu nào

## 3. Những điểm tốt cho hướng "up quái là có hết"

- Người chơi có `SM/TN` trực tiếp từ combat.
- Quái có rất nhiều nhánh rơi item theo map.
- Có sẵn auto-hút đồ nếu có charm.
- Có thể dùng chính `Mob.java` để tạo reward loop theo map mà không cần dựng hệ mới.

## 4. Những khoảng thiếu hiện tại

### 4.1. Thiếu reward loop rõ ràng theo tầng bản đồ

Hiện tại reward có nhiều nhưng chưa rõ cấu trúc:
- map đầu farm để lấy gì
- map giữa farm để lấy gì
- map cuối farm để lấy gì

Người chơi có thể farm được đồ, nhưng không chắc hiểu:
- nên cắm ở đâu
- để kiếm tài nguyên gì
- trong bao lâu thì đủ một vòng tiến bộ

### 4.2. Thiếu phần thưởng nhiệm vụ chính

- File: `src/nro/services/TaskService.java`
- Hàm `rewardDoneTask(Player player)` đang để trống.

Nhận xét:
- Đây là khoảng trống lớn.
- Nếu lấp vào bằng quà gắn với nhịp farm:
  - vàng
  - ngọc khóa
  - item hỗ trợ farm
  - nguyên liệu map tiếp theo
thì tiến trình sẽ mượt hơn rất nhiều.

### 4.3. Ngọc từ farm còn quá yếu

- Drop ngọc trực tiếp hiện quá hiếm.
- Nếu muốn người chơi cảm giác "cày là có", ngọc hoặc tài nguyên tương đương nên có thêm các nguồn:
  - quà nhiệm vụ
  - daily
  - token đổi ngọc
  - mốc diệt quái

### 4.4. Drop nhiều nhưng chưa được đóng gói thành mục tiêu farm

Hiện tại có rất nhiều nhánh drop, nhưng thiếu một bảng logic kiểu:

- đầu game:
  - farm quái để lấy vàng cơ bản
  - lấy item nhiệm vụ
  - tích vật phẩm hỗ trợ

- mid game:
  - farm map X để lấy đá / item kích hoạt / nguyên liệu

- late game:
  - farm map Y để săn đồ hiếm / nguyên liệu nâng cấp / item seasonal

## 5. Đề xuất cấu trúc farm nên hướng tới

### 5.1. Early game

Mục tiêu:
- luôn có cái rơi
- thấy lên đều
- không bí vàng
- nhiệm vụ hoàn thành xong có quà thật

Đề xuất:
- tăng nhẹ vàng map đầu
- cho item token nhỏ từ quái map đầu để đổi quà ở `Ong Gohan`
- thêm thưởng task main ở các mốc đầu

### 5.2. Mid game

Mục tiêu:
- cắm map có mục đích rõ
- có nguyên liệu nâng sức mạnh / kích hoạt / nâng đồ

Đề xuất:
- chia rõ map farm theo nhóm tài nguyên
- thêm mô tả ingame hoặc NPC gợi ý "map này farm gì"
- dùng `Ong Gohan` hoặc NPC khác để đổi token thành tài nguyên cần thiết

### 5.3. Late game

Mục tiêu:
- vẫn có lý do treo máy
- săn đồ hiếm nhưng không hoàn toàn vô vọng

Đề xuất:
- giữ rare drop
- nhưng thêm pity nhẹ qua token hoặc mảnh tích lũy

## 6. Đề xuất kỹ thuật ưu tiên

### Ưu tiên 1

- Log và nhóm lại toàn bộ drop trong `Mob.java` theo:
  - vàng
  - ngọc
  - item nhiệm vụ
  - item map-specific
  - item hiếm
  - item seasonal

### Ưu tiên 2

- Cài thưởng thật cho `rewardDoneTask(...)` trong `TaskService.java`

### Ưu tiên 3

- Thiết kế một vòng `farm token -> đổi quà` qua `Ong Gohan`

### Ưu tiên 4

- Mở rộng `DailyGiftService` để bù các tài nguyên mà farm thuần đang thiếu

## 7. Checklist tiếp theo

- [x] Log luồng quái chết và reward cơ bản
- [x] Xác định nguồn tiến bộ từ combat
- [x] Xác định các nhánh rơi vàng / ngọc / item
- [x] Phát hiện khoảng trống `rewardDoneTask(...)`
- [ ] Gom toàn bộ drop theo tier map
- [ ] Đề xuất bảng farm đầu / mid / late
- [ ] Đề xuất quà task main cụ thể
- [ ] Đề xuất token gameplay cho `Ong Gohan`
