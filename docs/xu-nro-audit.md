# Xu NRO Audit

Muc tieu: log lai toan bo co che `Xu NRO` hien tai, xac dinh rui ro neu dung tiep, va chot huong trien khai shop doi thuong thuong truc phu hop voi server cay cuoc.

## 1. Ket luan nhanh

- `1705` khong phai item trong.
- `1705` hien dang la `Xu NRO`.
- `1705` da duoc dung o nhieu he thong:
  - shop doi thuong
  - Quy Lao Kame
  - Quoc Vuong
  - Tranh Ngoc Namek
  - reward event / boss / gift
- Vi vay, khong nen doi nghia item `1705` thanh item khac.
- Huong hop ly nhat hien tai:
  - giu `1705 = Xu NRO`
  - mo them 1 shop doi `Xu NRO` thuong truc
  - gan shop nay vao `Ong Gohan`

## 2. Cac diem dang dung Xu NRO

### 2.1. Item template

- File: `database/backup.sql`
- Dinh nghia:
  - `1705 = Xu NRO`
  - mo ta: vat pham su kien

### 2.2. Xu NRO la item cong don

- File: `src/nro/services/InventoryService.java`
- `1705` nam trong nhom stack item dac biet.

Y nghia:
- da co san co che cong don so luong
- phu hop lam tien trao doi

### 2.3. Quy Lao Kame dang doc Xu NRO

- File: `src/nro/models/npc/npc_manifest/QuyLaoKame.java`
- NPC co doc so luong `1705` trong tui.
- Menu `Doi diem su kien` hien thi theo so luong `Xu NRO`.
- Tuy nhien menu nay chi mo khi co event:
  - Pokemon
  - Trung Thu
  - Teachers Day
  - Halloween
  - Christmas
  - Lunar New Year

Ket luan:
- Quy Lao hien tai khong phai shop thuong truc cho loop cay cuoc.
- Neu dua loop farm chinh vao day thi se bi phu thuoc event flag.

### 2.4. Quoc Vuong dang dung cung item 1705

- File: `src/nro/models/npc/npc_manifest/QuocVuong.java`
- Quoc Vuong mo shop `TRUONG_LAO`.
- Shop nay dang dung `item_spec = 1705`.

Ket luan:
- `1705` da duoc xem nhu mot dong tien cho khu vuc doi thuong dac thu.

### 2.5. Tranh Ngoc Namek dang gan cung id 1705

- File: `src/consts/ConstTranhNgocNamek.java`
- `ITEM_TRANH_NGOC = 1705`

- File: `src/models/DragonNamecWar/TranhNgocService.java`
- Co cong/tru `1705` khi thang, thua, hoa.

Ket luan:
- day la diem nhay cam nhat
- neu thay doi nghia item `1705` se anh huong truc tiep den he tranh ngoc

### 2.6. Boss / reward / gift dang co the cho Xu NRO

- File: `src/boss/Boss.java`
- co drop `1705` trong mot so luong event reward

- File: `database/backup.sql`
- nhieu gift pack, moc qua, shop doi thuong da chua `1705`

Ket luan:
- `Xu NRO` da nam trong he reward tong the cua server

## 3. Rui ro neu su dung Xu NRO cho loop farm chinh

### 3.1. Rui ro nho

- Gia tri `Xu NRO` se tang nhanh hon hien tai.
- Nguoi choi co the doi nhanh mot so item shop cu.

### 3.2. Rui ro vua

- Shop `TRUONG_LAO` va mot so shop doi thuong event co the tro nen qua re neu farm map thuong cung roi `Xu NRO`.
- Can chia lai gia tri item trong cac shop dung `1705`.

### 3.3. Rui ro lon

- He `Tranh Ngoc Namek` cung dung `1705`.
- Neu farm thuong roi qua nhieu `Xu NRO`, phan thuong tranh ngoc se mat gia tri.

## 4. Danh gia huong di

## 4.1. Huong khong nen lam

- Doi ten hoac doi cong nang item `1705`
- Gan toan bo shop tan thu vao Quy Lao theo event

Ly do:
- de gay dung luong cu
- kho kiem soat kinh te
- phu thuoc event flag

### 4.2. Huong nen lam

- Giu `1705` la `Xu NRO`
- Xem `Xu NRO` la tien chung cua gameplay
- Mo 1 shop thuong truc moi cho nguoi choi cay cuoc
- Dat shop nay tai `Ong Gohan`

Ly do:
- Ong Gohan da la NPC hub ho tro
- khong phu thuoc event
- de them menu huong dan, qua tan thu, giftcode, doi Xu NRO cung mot cho

## 5. De xuat trien khai

### Phase A: audit va dong bang pham vi

- [x] Xac nhan `1705` dang duoc dung
- [x] Xac nhan item id trong dai hien tai khong trong
- [x] Chot huong giu `Xu NRO`

### Phase B: tao luong shop thuong truc

- [ ] Tao 1 `tagName` shop moi, vi du: `XU_NRO_GOHAN`
- [ ] Gan menu moi vao `Ong Gohan`
- [ ] Shop nay chi ban do phuc vu loop cay cuoc, khong ban item event hiem

### Phase C: can bang nguon roi

- [ ] Chon map early / mid game duoc roi `Xu NRO`
- [ ] Dat tan suat roi thap, deu, de co cam giac farm duoc
- [ ] Khong de roi qua nhieu o map dac biet

### Phase D: can bang dau ra

- [ ] Tach item nao giu cho event / tranh ngoc
- [ ] Item nao dua vao shop thuong truc cua Ong Gohan
- [ ] Dieu chinh gia de nguoi choi moi co muc tieu 1-3 ngay dau

## 6. Goi y noi dung shop thuong truc Ong Gohan

Khong nen dua do qua hiem. Nen uu tien:

- dau than
- bua nho, vat pham treo quai
- da nang cap co ban
- ngoc khoa / vat pham utility
- item ho tro early-mid game
- mot vai mon tich luy de tao muc tieu farm

Khong nen dua vao ngay:

- vat pham event version hiem
- do qua manh lam mat gia tri boss / pho ban
- item dang la core reward cua Tranh Ngoc

## 7. Huong code tiep theo

Buoc hop ly tiep theo:

1. Them menu `Doi Xu NRO` vao `Ong Gohan`
2. Tao shop `XU_NRO_GOHAN`
3. Chon danh sach qua ban dau
4. Sau do moi gan drop `Xu NRO` vao nhom quai early-mid game

Thu tu nay an toan hon vi:

- co noi tieu truoc
- de test kinh te
- de nhin gia tri token truoc khi mo nguon roi
