# 📋 TÀI LIỆU LUỒNG HOẠT ĐỘNG HỆ THỐNG SMART CARD PARK

## 🎯 TỔNG QUAN HỆ THỐNG

Hệ thống Smart Card Park là một hệ thống quản lý vé chơi game tại công viên giải trí sử dụng thẻ thông minh (Smart Card) với các tính năng:
- Quản lý thông tin khách hàng trên thẻ
- Mua và quản lý vé chơi game
- Xác thực bảo mật bằng RSA và mã hóa PIN
- Quản lý số dư và giao dịch

---

## 🏗️ KIẾN TRÚC HỆ THỐNG

### Các thành phần chính:

1. **Smart Card Applet (Java Card)**: Ứng dụng chạy trên thẻ thông minh
   - Lưu trữ dữ liệu khách hàng (mã hóa)
   - Quản lý PIN (User PIN và Admin PIN)
   - Quản lý RSA keypair
   - Quản lý vé game
   - Xử lý các lệnh APDU

2. **Client Application (Kotlin/Compose)**: Ứng dụng trên máy tính
   - **MainAdmin**: Ứng dụng quản trị viên
   - **MainUser**: Ứng dụng người dùng
   - **MainGamePlay**: Ứng dụng quét thẻ tại máy chơi game

3. **Backend Server (Kotlin/Ktor)**: Server quản lý
   - API quản lý khách hàng
   - API quản lý game
   - API xác thực RSA
   - API quản lý giao dịch

---

## 🔐 HỆ THỐNG BẢO MẬT

### 1. Session Key (Khóa phiên)

**Mục đích**: Mã hóa PIN trước khi gửi xuống thẻ, tránh gửi PIN dạng plaintext.

**Cơ chế**:
- Session key là một key cố định: `0x01 0x02 0x03 ... 0x10` (16 bytes)
- Được lưu trong `SessionKeyStore` (singleton object)
- Chỉ gửi xuống thẻ **một lần** khi kết nối lần đầu
- Thẻ lưu session key trong `CardModel` (transient memory)

**Luồng**:
```
1. Terminal khởi tạo SessionKeyStore với key cố định
2. Khi connect lần đầu:
   - Terminal kiểm tra: SessionKeyStore.isKeySentToCard() == false
   - Gửi session key xuống thẻ (INS_SET_SESSION_KEY = 0x23)
   - Thẻ lưu session key vào CardModel
   - Terminal đánh dấu: SessionKeyStore.markKeySentToCard()
3. Các lần sau:
   - Terminal dùng session key để mã hóa PIN
   - Thẻ dùng session key đã lưu để giải mã PIN
```

### 2. Master Key (Khóa chính)

**Mục đích**: Mã hóa RSA private key và dữ liệu khách hàng trên thẻ.

**Cơ chế**:
- Master key được tạo ngẫu nhiên khi admin verify PIN lần đầu
- Được wrap (bọc) bằng cả Admin PIN key và User PIN key
- Lưu trong `CardModel` dưới dạng wrapped (đã mã hóa)

**Luồng tạo Master Key**:
```
1. Admin verify PIN lần đầu
2. Derive Admin PIN key từ Admin PIN + Salt (PBKDF2)
3. Tạo Master Key ngẫu nhiên (16 bytes)
4. Wrap Master Key bằng Admin PIN key → lưu wrappedMasterKeyAdmin
5. Tự động wrap Master Key bằng User PIN key (mặc định "1234") → lưu wrappedMasterKeyUser
```

**Luồng Unwrap Master Key**:
```
1. User/Admin verify PIN
2. Derive PIN key từ PIN + Salt (PBKDF2)
3. Unwrap Master Key bằng PIN key tương ứng
4. Master Key được load vào RAM (transient memory)
5. Dùng Master Key để giải mã RSA private key và dữ liệu
```

### 3. RSA Authentication (Xác thực RSA)

**Mục đích**: Xác thực thẻ với server để đảm bảo thẻ hợp lệ.

**Luồng Challenge-Response**:
```
1. Terminal gửi yêu cầu challenge đến server
   → GET /api/v1/rsa/challenge
   ← Server trả về: { challenge: "base64_32_bytes", expiresAt: timestamp }

2. Terminal gửi challenge xuống thẻ
   → INS_SIGN_CHALLENGE (0x91) + challenge bytes (32 bytes)
   ← Thẻ ký challenge bằng RSA private key → signature (128 bytes)

3. Terminal gửi signature lên server để verify
   → POST /api/v1/rsa/verify
   → Body: { customerId: "...", challenge: "...", signature: "base64..." }
   ← Server verify signature bằng RSA public key
   ← Trả về: { success: true/false, message: "..." }
```

**Lưu ý**:
- RSA private key được mã hóa bằng Master Key
- Cần verify PIN trước để unwrap Master Key → giải mã RSA private key
- Server lưu RSA public key trong database khi đăng ký

---

## 👨‍💼 LUỒNG HOẠT ĐỘNG ADMIN

### 1. Đăng nhập Admin

**Màn hình**: `AdminLoginScreen`

**Luồng**:
```
1. Admin nhập username và password
2. Terminal gửi request đến server:
   → POST /api/v1/admin/login
   → Body: { username: "...", password: "..." }
   ← Server verify password hash
   ← Trả về: { success: true, token: "jwt_token", admin: {...} }
3. Lưu session vào AdminSession
4. Chuyển sang màn hình CONNECT
```

### 2. Kết nối thẻ (Admin Mode)

**Màn hình**: `ConnectScreen` với `requireRSAAuth = false`

**Luồng chi tiết**:
```
1. Admin bấm nút "Bắt đầu vui chơi nào!"
2. Terminal gọi: smartCardManager.connectToCard()
   → Tìm thẻ qua PC/SC
   → Kết nối với thẻ
   → Select applet AID

3. Kiểm tra và gửi Session Key (nếu chưa gửi):
   → Nếu SessionKeyStore.isKeySentToCard() == false:
     → Gửi session key xuống thẻ (INS_SET_SESSION_KEY = 0x23)
     → Thẻ lưu session key vào CardModel
     → SessionKeyStore.markKeySentToCard()

4. Tạo PIN mặc định (nếu chưa có):
   → Kiểm tra User PIN: getPINStatus()
   → Nếu chưa có: createPIN("1234")
   → Kiểm tra Admin PIN: getAdminPINStatus()
   → Nếu chưa có: createAdminPIN("9999")

5. Tự động verify Admin PIN đã mã hóa:
   → Lấy session key từ SessionKeyStore
   → Pad PIN "9999" thành 16 bytes: "9999" + 0x00...0x00
   → Mã hóa PIN bằng session key (AES/ECB/NoPadding)
   → Gửi encrypted PIN xuống thẻ (INS_VERIFY_ADMIN_PIN_ENCRYPTED = 0x24)
   → Thẻ:
     a. Kiểm tra session key đã set chưa
     b. Giải mã PIN bằng session key
     c. Verify Admin PIN
     d. Derive Admin PIN key từ PIN + Salt (PBKDF2)
     e. Unwrap Master Key bằng Admin PIN key
     f. Set dataReady = true
   → Nếu thành công: Chuyển sang màn hình MAIN
   → Nếu thất bại: Hiển thị lỗi
```

**Lưu ý**:
- Admin không cần nhập PIN thủ công
- PIN được mã hóa trước khi gửi xuống thẻ
- Không xác thực RSA ở đây vì có thể chưa có RSA keypair

### 3. Màn hình chính Admin

**Màn hình**: `AdminMainMenuScreen`

**Các chức năng**:
- ✍️ Ghi thông tin khách hàng
- 👁️ Xem thông tin khách hàng
- 💰 Nạp tiền
- 🎮 Quản lý game
- 📊 Doanh thu
- 🔐 Xác thực RSA
- ⚙️ Cài đặt
- 🔄 Reset User PIN

### 4. Ghi thông tin khách hàng

**Màn hình**: `AdminWriteInfoScreen`

**Luồng chi tiết**:
```
1. Admin nhập thông tin:
   - Customer ID (mã khách hàng)
   - Họ tên
   - Ngày sinh
   - Số điện thoại
   - Địa chỉ
   - Ảnh đại diện (tùy chọn)

2. Khi bấm "Ghi thông tin":
   → Terminal gọi: smartCardManager.writeCustomerInfo(...)
   → Thẻ đã authenticated (dataReady = true, master key đã unwrap)
   → Gửi lệnh INS_WRITE_INFO (0x03)
   → Thẻ:
     a. Kiểm tra authenticated
     b. Mã hóa dữ liệu bằng Master Key (AES/CBC)
     c. Lưu vào CardModel
     d. Set dataEncrypted = true

3. Tạo RSA keypair (nếu chưa có):
   → Gửi lệnh INS_GENERATE_RSA_KEYPAIR (0x92)
   → Thẻ tạo RSA 1024-bit keypair
   → Lưu private key (đã mã hóa bằng Master Key)
   → Trả về public key

4. Upload public key lên server:
   → POST /api/v1/rsa/register
   → Body: { customerId: "...", publicKey: "PEM_format" }
   → Server lưu public key vào database

5. Set Customer ID cho RSA:
   → Gửi lệnh INS_SET_CUSTOMER_ID (0x90)
   → Thẻ lưu Customer ID (plaintext, để dùng khi xác thực RSA)
```

### 5. Nạp tiền

**Màn hình**: `AdminRechargeScreen`

**Luồng**:
```
1. Admin nhập số tiền nạp
2. Gửi lệnh INS_RECHARGE_BALANCE (0x07)
3. Thẻ:
   → Kiểm tra authenticated
   → Giải mã balance hiện tại
   → Cộng thêm số tiền
   → Mã hóa lại balance
   → Lưu vào CardModel
```

### 6. Quản lý Game

**Màn hình**: `AdminGameManagementScreen`

**Chức năng**:
- Xem danh sách game từ server
- Thêm game mới
- Sửa thông tin game
- Bật/tắt game (isActive)
- Xem danh sách game trên thẻ

**Luồng thêm game vào thẻ**:
```
1. Admin chọn game từ danh sách server
2. Nhập số lượng vé muốn thêm
3. Gửi lệnh INS_ADD_OR_INCREASE_TICKETS (0x11)
   → Thẻ:
     a. Tìm game trong danh sách (theo gameCode)
     b. Nếu chưa có: Tạo mới với số vé = số nhập
     c. Nếu đã có: Cộng thêm số vé
     d. Lưu vào GameManager
```

### 7. Xác thực RSA

**Màn hình**: `AdminRSAAuthScreen`

**Luồng**:
```
1. Admin bấm "Xác thực RSA"
2. Terminal gọi: smartCardManager.authenticateRSA("9999", rsaApi)
   → Verify Admin PIN đã mã hóa
   → Lấy Customer ID từ thẻ
   → Lấy challenge từ server
   → Ký challenge bằng RSA private key
   → Verify signature với server
3. Hiển thị kết quả
```

---

## 👤 LUỒNG HOẠT ĐỘNG USER

### 1. Kết nối thẻ (User Mode)

**Màn hình**: `ConnectScreen` với `requireRSAAuth = true`

**Luồng chi tiết**:
```
1. User bấm nút "Bắt đầu vui chơi nào!"
2. Terminal gọi: smartCardManager.connectToCard()
   → Tìm và kết nối thẻ
   → Gửi session key (nếu chưa gửi)

3. Kiểm tra RSA keypair:
   → Gọi: getRSAStatus()
   → Nếu chưa có: Hiển thị lỗi, yêu cầu vào Admin RSA để setup

4. Xác thực RSA (Challenge-Response):
   → Lấy Customer ID từ thẻ: getCustomerIDRSA()
   → Lấy challenge từ server: GET /api/v1/rsa/challenge
   → Ký challenge: signChallenge(challengeBytes)
   → Verify với server: POST /api/v1/rsa/verify
   → Nếu thành công: Chuyển sang màn hình PIN_ENTRY
   → Nếu thất bại: Hiển thị lỗi, yêu cầu setup RSA
```

### 2. Nhập PIN User

**Màn hình**: `PinEntryScreen`

**Luồng**:
```
1. User nhập PIN (4-8 ký tự)
2. Khi bấm "Xác thực PIN":
   → Terminal gọi: smartCardManager.verifyPINEncrypted(pin)
   → Lấy session key từ SessionKeyStore
   → Pad PIN thành 16 bytes
   → Mã hóa PIN bằng session key (AES/ECB/NoPadding)
   → Gửi encrypted PIN xuống thẻ (INS_VERIFY_PIN_ENCRYPTED = 0x25)
   → Thẻ:
     a. Giải mã PIN bằng session key
     b. Verify User PIN
     c. Derive User PIN key từ PIN + Salt (PBKDF2)
     d. Unwrap Master Key bằng User PIN key
     e. Set dataReady = true
   → Nếu thành công: Chuyển sang màn hình MAIN
   → Nếu thất bại: Giảm số lần thử còn lại, hiển thị lỗi
```

### 3. Màn hình chính User

**Màn hình**: `UserMainMenuScreen`

**Các chức năng**:
- 👁️ Xem thông tin
- ✏️ Cập nhật thông tin
- 🎫 Mua vé
- 🎮 Danh sách game
- 🔄 Đổi PIN
- 📜 Lịch sử giao dịch

### 4. Mua vé

**Màn hình**: `UserBuyTicketsScreen`

**Luồng**:
```
1. Terminal lấy danh sách game từ server (chỉ game isActive = true)
2. User chọn game và số lượng vé muốn mua
3. Kiểm tra số dư:
   → Gọi: checkBalance()
   → Tính tổng tiền = số vé × giá vé
   → So sánh với số dư
4. Nếu đủ tiền:
   → Gửi lệnh INS_MAKE_PAYMENT (0x0F)
   → Thẻ:
     a. Kiểm tra authenticated
     b. Giải mã balance
     c. Trừ tiền
     d. Mã hóa lại balance
   → Gửi lệnh INS_ADD_OR_INCREASE_TICKETS (0x11)
   → Thẻ thêm vé vào game
   → Gửi thông tin giao dịch lên server để lưu lịch sử
```

### 5. Xem thông tin

**Màn hình**: `UserViewInfoScreen`

**Luồng**:
```
1. Gọi: smartCardManager.readCustomerDataComplete()
2. Thẻ:
   → Kiểm tra authenticated
   → Giải mã dữ liệu bằng Master Key
   → Trả về dữ liệu đã giải mã
3. Hiển thị thông tin khách hàng
```

---

## 🎮 LUỒNG HOẠT ĐỘNG GAME PLAY

### 1. Chọn game

**Màn hình**: `GameSelectionScreen`

**Luồng**:
```
1. Terminal lấy danh sách game từ server
2. Lọc chỉ hiển thị game có isActive = true
3. User chọn game muốn chơi
4. Chuyển sang màn hình CONNECT
```

### 2. Kết nối thẻ tại máy chơi

**Màn hình**: `ConnectScreen` với `requireRSAAuth = false`

**Luồng**:
```
1. User đưa thẻ vào máy đọc
2. Terminal kết nối thẻ
3. Gửi session key (nếu chưa gửi)
4. Tạo PIN mặc định (nếu chưa có)
5. Tự động verify Admin PIN "9999" đã mã hóa
6. Chuyển sang màn hình PLAYING
```

### 3. Xử lý quét thẻ

**Màn hình**: `GamePlayScreen`

**Luồng chi tiết**:
```
1. Tự động xử lý khi vào màn hình:
   → Gọi: processCard()

2. Xác thực RSA:
   → Gọi: smartCardManager.authenticateRSA("9999", rsaApi)
   → Verify Admin PIN đã mã hóa
   → Lấy challenge từ server
   → Ký challenge
   → Verify với server
   → Nếu thất bại: Dừng, hiển thị lỗi

3. Kiểm tra vé:
   → Gọi: smartCardManager.findGame(gameCode)
   → Tìm game trong danh sách trên thẻ
   → Kiểm tra số vé > 0
   → Nếu không có vé: Dừng, hiển thị "Không có lượt"

4. Trừ vé:
   → Gọi: smartCardManager.decreaseGameTickets(gameCode, 1)
   → Gửi lệnh INS_DECREASE_GAME_TICKETS (0x12)
   → Thẻ:
     a. Tìm game theo gameCode
     b. Giảm số vé đi 1
     c. Lưu lại
   → Hiển thị "Thành công! Còn X lượt"

5. Ngắt kết nối và hoàn tất:
   → smartCardManager.disconnect()
   → Quay về màn hình chọn game
```

---

## 🔄 LUỒNG QUẢN LÝ PIN

### 1. Tạo PIN

**User PIN**:
```
→ INS_CREATE_PIN (0x01)
→ Thẻ:
  - Kiểm tra chưa có PIN
  - Lưu PIN vào OwnerPIN
  - Set pinCreated = true
```

**Admin PIN**:
```
→ INS_CREATE_ADMIN_PIN (0x1E)
→ Thẻ:
  - Kiểm tra chưa có Admin PIN
  - Lưu Admin PIN vào OwnerPIN
  - Set adminPinCreated = true
```

### 2. Verify PIN

**Luồng verify PIN đã mã hóa**:
```
1. Terminal:
   → Lấy session key từ SessionKeyStore
   → Pad PIN thành 16 bytes
   → Mã hóa PIN bằng session key (AES/ECB/NoPadding)

2. Gửi xuống thẻ:
   → INS_VERIFY_PIN_ENCRYPTED (0x25) hoặc INS_VERIFY_ADMIN_PIN_ENCRYPTED (0x24)
   → Data: encrypted PIN (16 bytes)

3. Thẻ:
   → Kiểm tra session key đã set
   → Giải mã PIN bằng session key
   → Tìm độ dài thực của PIN (loại bỏ padding)
   → Verify PIN với OwnerPIN
   → Nếu thành công:
     a. Derive PIN key từ PIN + Salt (PBKDF2)
     b. Unwrap Master Key bằng PIN key
     c. Set dataReady = true
   → Nếu thất bại:
     a. Giảm số lần thử còn lại
     b. Nếu hết lần thử: Block PIN
```

### 3. Đổi PIN

**Luồng**:
```
1. User phải verify PIN trước (dataReady = true)
2. Gửi lệnh INS_CHANGE_PIN (0x03)
3. Thẻ:
   → Kiểm tra authenticated
   → Nhận PIN mới
   → Update OwnerPIN
   → Derive PIN key mới từ PIN mới + Salt
   → Wrap Master Key bằng PIN key mới
   → Lưu wrappedMasterKeyUser mới
   → Giữ nguyên authentication state
```

### 4. Reset User PIN (Admin only)

**Luồng**:
```
1. Admin phải verify Admin PIN trước
2. Gửi lệnh INS_RESET_USER_PIN (0x20)
3. Thẻ:
   → Kiểm tra Admin PIN đã verified
   → Nhận User PIN mới
   → Update User PIN
   → Derive User PIN key mới từ PIN mới + Salt
   → Wrap Master Key (đang trong RAM) bằng User PIN key mới
   → Lưu wrappedMasterKeyUser mới
```

---

## 💾 LUỒNG QUẢN LÝ DỮ LIỆU

### 1. Ghi dữ liệu

**Luồng ghi thông tin khách hàng**:
```
1. Terminal chuẩn bị dữ liệu:
   - Customer ID (plaintext)
   - Họ tên, ngày sinh, SĐT, địa chỉ (plaintext)
   - Ảnh đại diện (nếu có)

2. Gửi lệnh INS_WRITE_INFO (0x03):
   → Thẻ:
     a. Kiểm tra authenticated (dataReady = true)
     b. Mã hóa dữ liệu bằng Master Key (AES/CBC)
     c. Lưu vào CardModel
     d. Set dataEncrypted = true
```

**Luồng ghi ảnh**:
```
1. Bắt đầu ghi ảnh:
   → INS_START_PHOTO_WRITE (0x04)
   → Thẻ khởi tạo buffer tạm

2. Ghi từng chunk:
   → INS_WRITE_PHOTO_CHUNK (0x05)
   → Data: chunk bytes (tối đa 200 bytes/chunk)
   → Thẻ lưu vào buffer tạm

3. Kết thúc ghi ảnh:
   → INS_FINISH_PHOTO_WRITE (0x06)
   → Thẻ:
     a. Mã hóa toàn bộ ảnh bằng Master Key
     b. Lưu vào CardModel
     c. Xóa buffer tạm
```

### 2. Đọc dữ liệu

**Luồng**:
```
1. Gửi lệnh INS_READ_INFO (0x08)
2. Thẻ:
   → Kiểm tra authenticated
   → Giải mã dữ liệu bằng Master Key
   → Trả về dữ liệu đã giải mã
```

**Luồng đọc ảnh**:
```
1. Gửi lệnh INS_READ_PHOTO_CHUNK (0x09)
2. Thẻ:
   → Giải mã ảnh
   → Trả về chunk (tối đa 200 bytes)
3. Terminal đọc từng chunk cho đến hết
```

---

## 🎫 LUỒNG QUẢN LÝ VÉ GAME

### 1. Thêm vé

**Luồng**:
```
1. Gửi lệnh INS_ADD_OR_INCREASE_TICKETS (0x11)
2. Data: [gameCode (2 bytes)][tickets (2 bytes)]
3. Thẻ:
   → Tìm game trong danh sách (theo gameCode)
   → Nếu chưa có:
     a. Tạo game mới
     b. Set số vé = tickets
   → Nếu đã có:
     a. Cộng thêm tickets vào số vé hiện tại
   → Lưu vào GameManager
```

### 2. Trừ vé

**Luồng**:
```
1. Gửi lệnh INS_DECREASE_GAME_TICKETS (0x12)
2. Data: [gameCode (2 bytes)][tickets (2 bytes)]
3. Thẻ:
   → Tìm game trong danh sách
   → Kiểm tra số vé >= tickets
   → Trừ số vé đi tickets
   → Lưu lại
```

### 3. Đọc danh sách game

**Luồng**:
```
1. Gửi lệnh INS_READ_GAMES (0x13)
2. Thẻ:
   → Đọc tất cả game từ GameManager
   → Trả về danh sách game (mỗi game: gameCode + tickets)
```

### 4. Tìm game

**Luồng**:
```
1. Gửi lệnh INS_FIND_GAME (0x15)
2. Data: [gameCode (2 bytes)]
3. Thẻ:
   → Tìm game theo gameCode
   → Trả về: gameCode + tickets (nếu tìm thấy)
   → Hoặc trả về lỗi nếu không tìm thấy
```

---

## 🔐 CHI TIẾT MÃ HÓA VÀ BẢO MẬT

### 1. PBKDF2 Key Derivation

**Mục đích**: Derive PIN key từ PIN và Salt.

**Tham số**:
- Algorithm: PBKDF2 với HMAC-SHA1
- Iterations: 1000
- Key length: 16 bytes (128 bits)
- Salt: 16 bytes, lưu trong CardModel

**Luồng**:
```
1. Thẻ có Salt cố định (tạo khi khởi tạo CardModel)
2. Khi verify PIN:
   → Input: PIN (4-8 bytes) + Salt (16 bytes)
   → PBKDF2(PIN, Salt, 1000 iterations) → PIN Key (16 bytes)
3. Dùng PIN Key để wrap/unwrap Master Key
```

### 2. AES Encryption

**Mã hóa dữ liệu khách hàng**:
- Algorithm: AES/CBC/PKCS5Padding
- Key: Master Key (16 bytes)
- IV: 16 bytes, lưu trong CardModel

**Mã hóa PIN (Session Key)**:
- Algorithm: AES/ECB/NoPadding
- Key: Session Key (16 bytes)
- PIN được pad thành 16 bytes trước khi mã hóa

**Mã hóa RSA Private Key**:
- Algorithm: AES/CBC/PKCS5Padding
- Key: Master Key (16 bytes)
- IV: Cùng IV với dữ liệu khách hàng

### 3. RSA Keypair

**Thông số**:
- Key size: 1024 bits
- Algorithm: RSA
- Signature: SHA1withRSA (compatible với Java Card 2.2.1)

**Lưu trữ**:
- Public Key: Lưu trên server (PEM format)
- Private Key: Lưu trên thẻ (đã mã hóa bằng Master Key)

---

## 📊 LUỒNG GIAO DỊCH

### 1. Nạp tiền

**Luồng**:
```
1. Admin nhập số tiền
2. Gửi lệnh INS_RECHARGE_BALANCE (0x07)
3. Data: [amount bytes] (số tiền dạng BCD hoặc binary)
4. Thẻ:
   → Giải mã balance hiện tại
   → Cộng thêm amount
   → Mã hóa lại balance
   → Lưu vào CardModel
```

### 2. Thanh toán

**Luồng**:
```
1. User chọn game và số lượng vé
2. Tính tổng tiền = số vé × giá vé
3. Gửi lệnh INS_MAKE_PAYMENT (0x0F)
4. Data: [amount bytes]
5. Thẻ:
   → Giải mã balance
   → Kiểm tra balance >= amount
   → Trừ balance đi amount
   → Mã hóa lại balance
   → Lưu vào CardModel
6. Thêm vé vào game (INS_ADD_OR_INCREASE_TICKETS)
7. Gửi thông tin giao dịch lên server để lưu lịch sử
```

### 3. Kiểm tra số dư

**Luồng**:
```
1. Gửi lệnh INS_CHECK_BALANCE (0x10)
2. Thẻ:
   → Giải mã balance
   → Trả về balance (đã giải mã)
```

---

## 🛡️ BẢO MẬT VÀ BẢO VỆ

### 1. PIN Protection

- User PIN: Tối đa 3 lần thử sai → Block
- Admin PIN: Tối đa 5 lần thử sai → Block
- PIN được mã hóa trước khi gửi xuống thẻ
- PIN key được derive bằng PBKDF2 (chống brute force)

### 2. Master Key Protection

- Master Key không bao giờ lưu plaintext
- Luôn được wrap bằng PIN key
- Chỉ unwrap khi verify PIN thành công
- Tự động xóa khỏi RAM khi deselect applet

### 3. RSA Key Protection

- RSA private key được mã hóa bằng Master Key
- Chỉ giải mã khi cần ký challenge
- Public key được lưu trên server để verify

### 4. Session Key Protection

- Session key được gửi một lần khi kết nối đầu tiên
- Lưu trong transient memory (tự động xóa khi deselect)
- Dùng để mã hóa PIN, không dùng cho dữ liệu nhạy cảm khác

### 5. Data Encryption

- Tất cả dữ liệu khách hàng được mã hóa bằng Master Key
- Ảnh đại diện cũng được mã hóa
- Balance được mã hóa riêng

---

## 🔄 CÁC TRẠNG THÁI VÀ FLAGS

### CardModel Flags:

- `dataReady`: Đã verify PIN và unwrap Master Key
- `dataEncrypted`: Dữ liệu đã được mã hóa
- `sessionKeySet`: Session key đã được set
- `pinCreated`: User PIN đã được tạo
- `adminPinCreated`: Admin PIN đã được tạo
- `masterKeyWrappedAdmin`: Master Key đã được wrap bằng Admin PIN key
- `masterKeyWrappedUser`: Master Key đã được wrap bằng User PIN key

### Transient Memory:

- Master Key: CLEAR_ON_DESELECT
- Session Key: CLEAR_ON_DESELECT
- Decrypted PIN: CLEAR_ON_DESELECT
- Decrypted data: CLEAR_ON_DESELECT

---

## 📝 GHI CHÚ QUAN TRỌNG

1. **Session Key**: Chỉ gửi một lần khi kết nối đầu tiên, sau đó dùng lại để mã hóa PIN.

2. **Master Key**: Được tạo khi admin verify PIN lần đầu, sau đó được wrap bằng cả Admin và User PIN key.

3. **RSA Authentication**: Chỉ thực hiện khi thẻ đã có RSA keypair (sau khi ghi thông tin).

4. **PIN Encryption**: Tất cả PIN đều được mã hóa bằng session key trước khi gửi xuống thẻ.

5. **Data Encryption**: Tất cả dữ liệu nhạy cảm đều được mã hóa bằng Master Key.

6. **Game Filtering**: Chỉ hiển thị game có `isActive = true` cho user.

7. **Transient Memory**: Tất cả key và dữ liệu nhạy cảm trong RAM sẽ tự động xóa khi deselect applet.

---

## 🎯 TÓM TẮT LUỒNG CHÍNH

### Admin Flow:
```
Login → Connect → Auto Verify Admin PIN → Main Menu → [Các chức năng]
```

### User Flow:
```
Connect → RSA Auth → PIN Entry → Main Menu → [Các chức năng]
```

### Game Play Flow:
```
Select Game → Connect → Auto Verify Admin PIN → RSA Auth → Check Tickets → Decrease Tickets → Complete
```

---

**Tài liệu này mô tả chi tiết toàn bộ luồng hoạt động của hệ thống Smart Card Park.**
