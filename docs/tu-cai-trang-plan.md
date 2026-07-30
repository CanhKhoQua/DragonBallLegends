# Tu Cai Trang Plan

## Muc tieu

Cho phep nguoi choi mac cai trang A de lay chi so, nhung hien thi ngoai hinh cai trang B da mo khoa trong bo suu tap.

Quy tac:
- Item cai trang dang mac van la nguon chi so duy nhat.
- Cai trang trong tu chi dung de hien thi ngoai hinh.
- Them cai trang vao tu la cat item that vao `itemsCostumeBox`, khong dung chung ruong suu tap thuong.
- Neu khong chon cai trang hien thi, nhan vat hien thi theo trang bi/cai trang dang mac nhu hien tai.

## Du lieu can co

Trong player can luu:
- Danh sach template id cai trang da mo khoa.
- Template id cai trang dang hien thi, mac dinh `-1`.

De xuat dang luu:

```json
{
  "collection": [123, 456, 789],
  "display": 456
}
```

Neu player data dang luu bang JSON/string phuc tap, uu tien them field rieng de tranh anh huong data cu.

## Flow NPC

NPC Nha tao mau mo panel ruong suu tap cai trang:
- "Mo tu"
- "Go hien thi"
- "Huong dan"

Them vao tu:
- Mo NPC Nha tao mau, chon Mo tu.
- Cat item cai trang vinh vien tu hanh trang/nguoi vao tu.
- Tu nay chi nhan item type 5 va khong co han su dung.

Chon hien thi:
- Cat cai trang vao tu cai trang rieng cua Nha tao mau.
- Bam vao cai trang trong panel Nha tao mau de set display id.
- Cai trang phai con nam trong tu cai trang thi moi co quyen hien thi.
- Ruong suu tap cu van dung de lay/cat do binh thuong.

Go hien thi:
- Set display id = -1.
- Gui lai body/ngoai hinh.

## Render ngoai hinh

Can tim cac diem dang lay ngoai hinh cai trang:
- Player body/head/body/leg/bag/aura/effect.
- Service gui update body.
- Packet nguoi choi vao map.
- Packet thay doi trang bi.

Quy tac ap dung:
- Neu `costumeDisplayId != -1`, lay template cai trang display de tinh ngoai hinh.
- Neu khong, dung logic cu.
- Display costume duoc uu tien truoc bien hinh/hop the; muon hien hop the thi go hien thi.
- Khong dua option cua display costume vao NPoint.

## Test checklist

- Them cai trang vao tu thanh cong va item nam trong `itemsCostumeBox`.
- Tu cai trang khong hien item cua ruong suu tap thuong.
- Khong them duoc item khong phai cai trang.
- Mac cai trang A, hien thi cai trang B: chi so cua A, ngoai hinh cua B.
- Thao cai trang A: mat chi so A, van co the hien thi B neu display dang bat.
- Go hien thi: ve logic hien thi cu.
- Logout/login giu collection va display id.
- Nguoi choi khac thay dung ngoai hinh.
- Doi map, chet/hoi sinh, thay trang bi khong mat display.

## Thu tu thuc hien

1. Tim chinh xac noi render ngoai hinh.
2. Tim noi load/save player data.
3. Them model data cho tu cai trang.
4. Them helper lay cai trang hien thi.
5. Sua render dung helper moi.
6. Them NPC Nha tao mau o nha.
7. Test cac flow co ban.

## Migration can chay

```sql
ALTER TABLE player ADD COLUMN costume_collection TEXT NULL DEFAULT NULL;
ALTER TABLE player ADD COLUMN items_costume_box TEXT NULL DEFAULT NULL;
```

Can them npc_template cho NPC moi:

```sql
INSERT INTO npc_template (id, NAME, head, body, leg, avatar)
VALUES (111, 'Nha tao mau', 7, 8, 9, 7);
```

Neu id 111 da co thi dung UPDATE lai template. Sau do them NPC id 111 vao cot `npcs`
cua map nha Trai Dat/Namek/Xayda, vi du dang `[111,x,y]` theo format map hien tai.
