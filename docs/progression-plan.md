# Progression Plan

Mục tiêu: định hướng server theo kiểu "cày cuốc vui vẻ", ưu tiên cảm giác farm quái có thưởng, tiến bộ đều, có lý do đăng nhập lại, và không ép người chơi phải đi qua nạp để mở core loop.

## 0. Định hướng chốt hiện tại

- Hướng chính: `up quái là có hết`.
- Không ưu tiên `nạp thẻ`.
- Không ưu tiên `TopService` lúc này.
- `OpenPowerService` hiện không phải nút thắt chính, tạm thời chưa cần đụng.
- `TrainingService` khá old-school, không phải trục chính vì người chơi thực tế sẽ treo máy farm.
- `Ong Gohan` là hub tốt để mở rộng hỗ trợ như giftcode, quà tân thủ, quà quay lại.
- `DailyGiftService` đáng giữ và mở rộng thêm.
- `ToriBot` nên chỉnh lại quà theo hướng hỗ trợ gameplay thay vì nghiêng về VIP/cash.

## 1. Hiện trạng cơ chế

### 1.1. Trục tiến trình chính

- `Nhiệm vụ chính`
  - File: `src/nro/services/TaskService.java`
  - Đây vẫn là xương sống dẫn người chơi đi map, giết quái, nói chuyện NPC và mở các mốc chơi tiếp theo.

- `Farm quái`
  - Đây là trục mà người chơi thực tế bám vào nhiều nhất.
  - Hiện plan vẫn chưa log đủ toàn bộ phần thưởng / drop / tài nguyên kiếm được trực tiếp từ quái.
  - Đây là phần cần ưu tiên đào sâu tiếp.

- `OpenPowerService`
  - File: `src/nro/services/OpenPowerService.java`
  - Có gate giới hạn sức mạnh.
  - Nhưng theo định hướng hiện tại, chưa xem là vấn đề chính cần sửa trước.

- `TrainingService`
  - File: `src/models/Training/TrainingService.java`
  - Có offline training, auto training, tăng sức mạnh khi vắng mặt.
  - Nhưng đây không còn hợp với nhịp chơi chính của server nếu người chơi chủ yếu treo máy farm.

- `Ong Gohan`
  - File: `src/nro/models/npc/npc_manifest/OngGohan.java`
  - Hiện đã có nhiều chức năng hỗ trợ:
    - quà miễn phí
    - hỗ trợ nhiệm vụ
    - đổi một số tài nguyên
    - mở thành viên
  - Đây là nơi rất hợp để thêm `giftcode`, quà tân thủ, quà quay lại, hỗ trợ catch-up.

- `DailyGiftService`
  - File: `src/player/dailyGift/DailyGiftService.java`
  - Hiện tại còn mỏng, mới giống cờ reset quà mỗi ngày.
  - Có tiềm năng mở rộng thành vòng lặp đăng nhập lại mỗi ngày.

- `ToriBot`
  - File: `src/nro/models/npc/npc_manifest/ToriBot.java`
  - Hiện đang thiên nhiều về VIP / cash / quà theo hướng mùa và nạp.
  - Chưa khớp định hướng "cày vui vẻ bằng gameplay".

- `NapThe`
  - File: `src/nro/services/NapThe.java`
  - Có sẵn nhưng hiện không phải ưu tiên.

- `TopService`
  - File: `src/services/top/TopService.java`
  - Có sẵn nhưng hiện không phải ưu tiên.

## 2. Đánh giá theo mục tiêu hiện tại

### 2.1. Điểm đang ổn

- Có sẵn xương sống nhiệm vụ.
- Có sẵn nhiều điểm để gắn phần thưởng và hỗ trợ mà không phải làm lại hệ thống từ đầu.
- `Ong Gohan` và `DailyGiftService` là hai chỗ rất tốt để mở rộng trải nghiệm casual.

### 2.2. Điểm cần chỉnh theo hướng mới

- `Farm quái` chưa được log và thiết kế như trục tiến trình chính.
  - Nếu đã chốt hướng "up quái là có hết" thì cần nhìn lại toàn bộ reward loop xoay quanh quái.

- `TrainingService` không còn là trọng tâm đáng đầu tư trước.
  - Có thể giữ nguyên hoặc chỉ chỉnh nhẹ, không nên dành ưu tiên cao.

- `DailyGiftService` còn quá mỏng.
  - Chưa tạo được lý do rõ ràng để người chơi quay lại mỗi ngày.

- `ToriBot` đang lệch hướng.
  - Quà và menu đang thiên nhiều về VIP/cash, chưa phục vụ nhịp cày cuốc vui vẻ.

- `Ong Gohan` tốt nhưng chưa được tận dụng hết.
  - Nên gom các cơ chế hỗ trợ người chơi mới / người chơi quay lại về đây.

## 3. Hướng bổ sung nên ưu tiên

### 3.1. Gói A: Lấy farm quái làm trung tâm

- Log lại toàn bộ tài nguyên mà quái đang cho:
  - vàng
  - ngọc
  - vật phẩm
  - nguyên liệu
  - item đổi thưởng
- Xác định những thứ người chơi đang thiếu khi chỉ cắm máy farm.
- Bổ sung các mốc "đánh là có" để người chơi thấy rõ tiến bộ:
  - vật phẩm tích lũy
  - nguyên liệu đổi quà
  - rơi item theo map / tier
  - mảnh / token đổi thưởng

### 3.2. Gói B: Làm mượt early game

- Rà lại các đoạn nhiệm vụ dễ gây khựng.
- Tăng quà nhiệm vụ ở các mốc đầu / giữa.
- Dùng `Ong Gohan` để hỗ trợ:
  - quà tân thủ
  - hỗ trợ vượt mốc đầu
  - giftcode
  - quà quay lại

### 3.3. Gói C: Mở rộng DailyGiftService

- Từ hệ rất mỏng hiện tại, mở thành:
  - quà đăng nhập ngày
  - nhiệm vụ ngày
  - mốc hoạt động ngày
- Thưởng nên thiên về gameplay:
  - tài nguyên farm
  - vật phẩm tiện ích
  - item hỗ trợ treo quái
  - quà nhỏ nhưng đều

### 3.4. Gói D: Chỉnh lại ToriBot

- Giảm trọng tâm VIP/cash.
- Chuyển dần sang quà gameplay:
  - quà hoạt động
  - quà mốc cày
  - quà mùa
  - quà đăng nhập
  - quà hỗ trợ nhóm bạn chơi cùng

### 3.5. Gói E: Catch-up cho người chơi đi sau

- Quà quay lại sau nhiều ngày offline.
- Bonus account mới 3-7 ngày đầu.
- Hỗ trợ tài nguyên nhẹ cho người chơi dưới ngưỡng tiến trình nhất định.

## 4. Đề xuất thứ tự triển khai

### Phase 1: Log lại vòng lặp farm quái

- Ghi toàn bộ reward loop từ quái.
- Xác định map nào đáng farm, map nào vô nghĩa.
- Xác định tài nguyên nào đang thiếu ở gameplay loop.

### Phase 2: Củng cố early game

- Rà lại task main.
- Thêm quà mốc đầu.
- Mở rộng `Ong Gohan` thành hub hỗ trợ.

### Phase 3: Làm vòng lặp ngày

- Mở rộng `DailyGiftService`.
- Thêm nhiệm vụ ngày.
- Thêm mốc hoạt động ngày.

### Phase 4: Chỉnh lại ToriBot

- Đổi quà theo hướng phục vụ gameplay.
- Giảm vai trò VIP/cash trong phần thưởng hiển thị công khai.

### Phase 5: Làm catch-up

- Quà quay lại.
- Buff nhẹ account mới.
- Hỗ trợ người chơi đi sau.

## 5. Checklist triển khai

### Hiện trạng đã chốt

- [x] Hướng chính là `up quái là có hết`
- [x] `OpenPowerService` chưa cần ưu tiên
- [x] `TrainingService` chưa cần ưu tiên
- [x] `Ong Gohan` là hub tốt
- [x] `DailyGiftService` đáng mở rộng
- [x] `ToriBot` cần chỉnh lại quà
- [x] `NapThe` chưa cần ưu tiên
- [x] `TopService` chưa cần ưu tiên

### Việc nên làm tiếp

- [ ] Log toàn bộ reward từ farm quái
- [ ] Log các điểm kẹt early game theo task
- [x] Audit `Xu NRO (1705)` và chốt hướng giữ làm tiền chung
- [ ] Đề xuất menu / flow mới cho `Ong Gohan`
- [ ] Tạo shop `Xu NRO` thường trực tại `Ong Gohan`
- [ ] Đề xuất bộ quà mới cho `ToriBot`
- [ ] Thiết kế `DailyGiftService` bản mở rộng
- [ ] Thiết kế catch-up cho account mới / account quay lại

## 6. File cần đào sâu tiếp

- `src/nro/services/TaskService.java`
- `src/nro/models/npc/npc_manifest/OngGohan.java`
- `src/player/dailyGift/DailyGiftService.java`
- `src/nro/models/npc/npc_manifest/ToriBot.java`
- `src/jdbc/daos/PlayerDAO.java`
- `src/jdbc/daos/NDVSqlFetcher.java`

## 7. Hướng làm tiếp ngay

Nếu bám đúng mục tiêu hiện tại, thứ nên làm tiếp không phải là thêm top hay nạp, mà là:

1. Log lại cơ chế farm quái hiện tại.
2. Thiết kế lại quà `ToriBot`.
3. Mở rộng `Ong Gohan` với giftcode / quà hỗ trợ.
4. Làm lại `DailyGiftService` cho có lý do đăng nhập hàng ngày.
