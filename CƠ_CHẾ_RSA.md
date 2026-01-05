# 🔐 CƠ CHẾ RSA TRONG HỆ THỐNG SMART CARD PARK

## 📋 TỔNG QUAN

Hệ thống sử dụng **RSA Challenge-Response Authentication** để xác thực thẻ với server. Mỗi thẻ có một cặp khóa RSA 1024-bit được tạo và lưu trữ trên thẻ.

---

## 🎯 MỤC ĐÍCH

1. **Xác thực thẻ**: Đảm bảo thẻ là hợp lệ và thuộc về khách hàng đã đăng ký
2. **Chống giả mạo**: Chỉ thẻ có private key mới có thể ký challenge
3. **Liên kết với server**: Server lưu public key để verify signature

---

## 🔑 CẤU TRÚC RSA KEYPAIR

### Thông số kỹ thuật:
- **Key size**: 1024 bits (RSA-1024)
- **Algorithm**: RSA
- **Signature Algorithm**: SHA1withRSA (PKCS#1 v1.5)
- **Modulus size**: 128 bytes
- **Exponent**: Thường là 3 bytes (0x010001 = 65537)
- **Signature size**: 128 bytes

### Lưu trữ:
- **Public Key**: 
  - Lưu trên **server** (database) dưới dạng PEM format
  - Có thể lấy từ thẻ bất cứ lúc nào (không cần PIN)
- **Private Key**: 
  - Lưu trên **thẻ** trong `RSAPrivateKey` object của Java Card
  - Được bảo vệ bởi Java Card framework (không thể đọc trực tiếp)
  - **KHÔNG** được mã hóa bằng Master Key (được bảo vệ bởi hệ thống Java Card)

---

## 🔄 LUỒNG HOẠT ĐỘNG RSA

### 1. TẠO RSA KEYPAIR (Lần đầu - Admin)

**Khi nào**: Khi admin ghi thông tin khách hàng lần đầu

**Luồng chi tiết**:

```
1. Admin ghi thông tin khách hàng:
   → Gửi lệnh INS_WRITE_INFO (0x03)
   → Thẻ mã hóa và lưu dữ liệu

2. Set Customer ID:
   → Gửi lệnh INS_SET_CUSTOMER_ID (0x90)
   → Thẻ lưu Customer ID (plaintext, tối đa 15 bytes)

3. Tạo RSA keypair trong thẻ:
   → Gửi lệnh INS_GENERATE_RSA_KEYPAIR (0x1D)
   → Thẻ:
     a. Gọi: rsaKeyPair.genKeyPair()
     b. Tạo cặp khóa RSA-1024 (mất vài giây)
     c. Lưu vào RSAPrivateKey và RSAPublicKey objects
     d. Set rsaKeyReady = true
   → Trả về: 0x9000 (success)

4. Lấy Public Key từ thẻ:
   → Gửi lệnh INS_GET_PUBLIC_KEY (0x1E) với component = 0x00 (modulus)
   → Thẻ trả về: modulus (128 bytes)
   → Gửi lệnh INS_GET_PUBLIC_KEY (0x1E) với component = 0x01 (exponent)
   → Thẻ trả về: exponent (3 bytes)

5. Tạo PEM format:
   → Terminal tạo X.509 SubjectPublicKeyInfo từ modulus + exponent
   → Encode Base64
   → Thêm PEM headers: "-----BEGIN PUBLIC KEY-----" và "-----END PUBLIC KEY-----"

6. Upload Public Key lên server:
   → POST /api/v1/rsa/register
   → Body: {
       customerId: "CUST001",
       publicKey: "-----BEGIN PUBLIC KEY-----\n...\n-----END PUBLIC KEY-----"
     }
   → Server:
     a. Parse PEM format
     b. Lưu vào database (CustomerKeys table)
     c. Set isActive = true
```

**Code trên thẻ**:
```java
// CardModel.java
public void generateRSAKeyPair() {
    rsaKeyPair.genKeyPair();  // Tạo keypair (mất vài giây)
    rsaKeyReady = true;
}
```

**Lưu ý**:
- Quá trình tạo keypair mất **vài giây** (2-5 giây tùy thẻ)
- Private key được lưu trong Java Card framework, không thể đọc trực tiếp
- Public key có thể lấy bất cứ lúc nào (không cần PIN)

---

### 2. XÁC THỰC RSA (Challenge-Response)

**Khi nào**: 
- User mode: Khi kết nối thẻ
- Game Play mode: Khi quét thẻ tại máy chơi
- Admin mode: Khi cần xác thực RSA

**Luồng chi tiết**:

#### Bước 1: Verify PIN (Unwrap Master Key)

```
→ Terminal gọi: verifyAdminPINEncrypted("9999") hoặc verifyPINEncrypted("1234")
→ Thẻ:
  a. Giải mã PIN bằng session key
  b. Verify PIN
  c. Derive PIN key từ PIN + Salt (PBKDF2)
  d. Unwrap Master Key bằng PIN key
  e. Set dataReady = true
```

**Lưu ý**: 
- Master Key được unwrap để có thể sử dụng (nhưng RSA private key không cần Master Key để sign)
- `dataReady = true` cho phép truy cập các chức năng của thẻ

#### Bước 2: Lấy Challenge từ Server

```
→ Terminal gọi: rsaApi.getChallenge()
→ Request: GET /api/v1/rsa/challenge
→ Server:
  a. Tạo challenge ngẫu nhiên: Random.nextBytes(32)
  b. Encode Base64
  c. Lưu challenge với thời gian expire (5 phút)
  d. Trả về: {
       challenge: "base64_encoded_32_bytes",
       expiresAt: timestamp
     }
```

**Code server**:
```kotlin
fun generateChallenge(): ChallengeResponse {
    val challengeBytes = Random.nextBytes(32)
    val challenge = Base64.getEncoder().encodeToString(challengeBytes)
    val expiresAt = System.currentTimeMillis() + 5 * 60 * 1000 // 5 phút
    
    challenges[challenge] = expiresAt  // Lưu vào memory
    
    return ChallengeResponse(
        challenge = challenge,
        expiresAt = expiresAt
    )
}
```

#### Bước 3: Ký Challenge trên Thẻ

```
→ Terminal:
  a. Decode challenge từ Base64 → 32 bytes
  b. Gửi lệnh INS_SIGN_CHALLENGE (0x94)
  c. Data: challenge bytes (32 bytes)

→ Thẻ:
  a. Kiểm tra: rsaKeyReady == true
  b. Init Signature với SHA1withRSA:
     Signature sig = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false)
  c. Init với private key:
     sig.init(privateKey, Signature.MODE_SIGN)
  d. Ký challenge:
     sig.sign(challenge, 0, 32, signature, 0)
  e. Trả về signature (128 bytes)

→ Terminal nhận signature (128 bytes)
```

**Code trên thẻ**:
```java
// CardModel.java
public short signChallenge(byte[] challenge, short chalOffset, short chalLen,
                          byte[] signature, short sigOffset) {
    if (!rsaKeyReady) {
        ISOException.throwIt(SW_RSA_NOT_READY);
    }
    
    // Init signature với SHA1withRSA
    Signature sig = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
    sig.init(privateKey, Signature.MODE_SIGN);
    
    // Sign challenge
    return sig.sign(challenge, chalOffset, chalLen, signature, sigOffset);
}
```

**Lưu ý**:
- Challenge phải đúng **32 bytes**
- Signature trả về là **128 bytes** (RSA-1024)
- Private key được dùng trực tiếp (không cần giải mã)

#### Bước 4: Verify Signature trên Server

```
→ Terminal:
  a. Encode signature thành Base64
  b. Lấy Customer ID từ thẻ: getCustomerIDRSA()
  c. Gọi: rsaApi.verifySignature(customerId, challenge, signatureBase64)

→ Request: POST /api/v1/rsa/verify
→ Body: {
     customerId: "CUST001",
     challenge: "base64_challenge",
     signature: "base64_signature"
   }

→ Server:
  a. Kiểm tra challenge có tồn tại và chưa expire:
     - Tìm challenge trong challenges map
     - Kiểm tra: currentTime < expiresAt
     - Nếu không hợp lệ: Trả về lỗi
  
  b. Lấy Public Key từ database:
     - Query: CustomerKeys WHERE customerId = "CUST001" AND isActive = true
     - Parse PEM format → PublicKey object
  
  c. Verify signature:
     - Signature sig = Signature.getInstance("SHA1withRSA")
     - sig.initVerify(publicKey)
     - Decode challenge từ Base64 → 32 bytes
     - sig.update(challengeBytes)
     - Decode signature từ Base64 → 128 bytes
     - isValid = sig.verify(signatureBytes)
  
  d. Nếu hợp lệ:
     - Xóa challenge khỏi map (chỉ dùng một lần)
     - Trả về: { success: true, message: "Xác thực thành công" }
  
  e. Nếu không hợp lệ:
     - Trả về: { success: false, message: "Chữ ký không hợp lệ" }
```

**Code server**:
```kotlin
fun verifySignature(request: RSAVerifyRequest): RSAVerifyResponse {
    // 1. Check challenge
    val challengeExpiry = challenges[request.challenge]
    if (challengeExpiry == null || System.currentTimeMillis() > challengeExpiry) {
        return RSAVerifyResponse(success = false, message = "Challenge không hợp lệ")
    }
    
    // 2. Lấy public key
    val customerKey = CustomerKeys.select {
        CustomerKeys.customerId eq request.customerId and (CustomerKeys.isActive eq true)
    }.singleOrNull()
    
    if (customerKey == null) {
        return RSAVerifyResponse(success = false, message = "Không tìm thấy customer")
    }
    
    // 3. Parse và verify
    val publicKey = parsePublicKey(customerKey[CustomerKeys.publicKey])
    val signature = Signature.getInstance("SHA1withRSA")
    signature.initVerify(publicKey)
    
    val challengeBytes = Base64.getDecoder().decode(request.challenge)
    signature.update(challengeBytes)
    
    val signatureBytes = Base64.getDecoder().decode(request.signature)
    val isValid = signature.verify(signatureBytes)
    
    if (isValid) {
        challenges.remove(request.challenge)  // Xóa challenge đã dùng
    }
    
    return RSAVerifyResponse(
        success = isValid,
        message = if (isValid) "Xác thực thành công" else "Chữ ký không hợp lệ"
    )
}
```

---

## 🔒 BẢO MẬT RSA

### 1. Private Key Protection

**Cơ chế**:
- Private key được lưu trong `RSAPrivateKey` object của Java Card framework
- Java Card framework bảo vệ private key:
  - Không thể đọc trực tiếp từ bên ngoài
  - Chỉ có thể sử dụng thông qua Signature API
  - Tự động xóa khi applet bị deselect (nếu dùng transient memory)

**Lưu ý**:
- Private key **KHÔNG** được mã hóa bằng Master Key
- Private key được bảo vệ bởi Java Card OS
- Không thể export private key ra ngoài thẻ

### 2. Challenge Protection

**Cơ chế**:
- Challenge được tạo ngẫu nhiên (32 bytes)
- Mỗi challenge chỉ dùng **một lần** (xóa sau khi verify thành công)
- Challenge có thời gian expire (5 phút)
- Challenge được lưu trên server (memory hoặc Redis)

**Chống replay attack**:
- Mỗi challenge chỉ dùng một lần
- Challenge có thời gian expire ngắn (5 phút)
- Server xóa challenge sau khi verify thành công

### 3. Signature Verification

**Cơ chế**:
- Server verify signature bằng public key
- Public key được lưu trong database khi đăng ký
- Chỉ public key của customer tương ứng mới verify được

**Chống giả mạo**:
- Chỉ thẻ có private key mới ký được challenge
- Server verify bằng public key đã đăng ký
- Không thể giả mạo signature nếu không có private key

---

## 📊 CÁC LỆNH APDU RSA

### 1. INS_GENERATE_RSA_KEYPAIR (0x1D)

**Mục đích**: Tạo RSA keypair trong thẻ

**Input**: Không có

**Output**: 
- 0x9000: Success
- 0x6A80: Lỗi khác

**Thời gian**: 2-5 giây (tùy thẻ)

**Code**:
```java
private void generateRSAKeyPair(APDU apdu) {
    model.generateRSAKeyPair();
    // Success - chỉ return SW_NO_ERROR
}
```

---

### 2. INS_GET_PUBLIC_KEY (0x1E)

**Mục đích**: Lấy public key component từ thẻ

**Input**: 
- [1 byte component]
  - 0x00 = Modulus (128 bytes)
  - 0x01 = Exponent (3 bytes)

**Output**: 
- [component bytes]
- 0x9000: Success
- 0x6A86: Wrong P1/P2 (component không hợp lệ)

**Code**:
```java
private void getPublicKey(APDU apdu) {
    byte[] buf = apdu.getBuffer();
    short lc = apdu.setIncomingAndReceive();
    
    if (lc != 1) {
        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    }
    
    byte component = buf[ISO7816.OFFSET_CDATA];
    short len = 0;
    
    if (component == 0x00) {
        // Get modulus (128 bytes)
        len = model.getPublicKeyModulus(buf, (short)0);
    } else if (component == 0x01) {
        // Get exponent (3 bytes)
        len = model.getPublicKeyExponent(buf, (short)0);
    } else {
        ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
    }
    
    apdu.setOutgoing();
    apdu.setOutgoingLength(len);
    apdu.sendBytesLong(buf, (short)0, len);
}
```

---

### 3. INS_SIGN_CHALLENGE (0x94)

**Mục đích**: Ký challenge bằng RSA private key

**Input**: 
- [32 bytes challenge]

**Output**: 
- [128 bytes signature]
- 0x9000: Success
- 0x6985: RSA key chưa ready

**Code**:
```java
private void signChallenge(APDU apdu) {
    if (!model.isRSAKeyReady()) {
        ISOException.throwIt(SW_RSA_NOT_READY);
    }
    
    byte[] buf = apdu.getBuffer();
    short lc = apdu.setIncomingAndReceive();
    
    if (lc != 32) {
        ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    }
    
    // Sign challenge
    short sigLen = model.signChallenge(
        buf, ISO7816.OFFSET_CDATA, (short)32,  // challenge input
        buf, (short)0                           // signature output
    );
    
    // Return signature
    apdu.setOutgoing();
    apdu.setOutgoingLength(sigLen);
    apdu.sendBytesLong(buf, (short)0, sigLen);
}
```

---

### 4. INS_GET_RSA_STATUS (0x1C)

**Mục đích**: Kiểm tra trạng thái RSA key

**Input**: Không có

**Output**: 
- [1 byte status]
  - 0x00 = RSA not ready
  - 0x01 = RSA ready
- 0x9000: Success

**Code**:
```java
private void getRSAStatus(APDU apdu) {
    byte[] buf = apdu.getBuffer();
    buf[0] = model.isRSAKeyReady() ? (byte)0x01 : (byte)0x00;
    
    apdu.setOutgoing();
    apdu.setOutgoingLength((short)1);
    apdu.sendBytes((short)0, (short)1);
}
```

---

## 🔄 LUỒNG XÁC THỰC RSA HOÀN CHỈNH

### User Mode (Kết nối thẻ):

```
1. User bấm "Connect"
   → Terminal: connectToCard()
   → Gửi session key (nếu chưa gửi)

2. Kiểm tra RSA status:
   → Terminal: getRSAStatus()
   → Nếu false: Hiển thị lỗi, yêu cầu setup RSA

3. Xác thực RSA:
   → Terminal: authenticateRSA("9999", rsaApi)
   → a. Verify Admin PIN đã mã hóa
   → b. Lấy Customer ID: getCustomerIDRSA()
   → c. Lấy challenge: rsaApi.getChallenge()
   → d. Ký challenge: signChallenge(challengeBytes)
   → e. Verify: rsaApi.verifySignature(customerId, challenge, signature)
   → Nếu thành công: Chuyển sang PIN_ENTRY
   → Nếu thất bại: Hiển thị lỗi

4. User nhập PIN:
   → Terminal: verifyPINEncrypted(pin)
   → Chuyển sang MAIN
```

### Game Play Mode (Quét thẻ tại máy):

```
1. User đưa thẻ vào máy
   → Terminal: connectToCard()
   → Tự động verify Admin PIN "9999"

2. Xác thực RSA:
   → Terminal: authenticateRSA("9999", rsaApi)
   → (Tương tự như User Mode)

3. Kiểm tra vé:
   → Terminal: findGame(gameCode)
   → Nếu có vé: Trừ vé và cho phép chơi
   → Nếu không có vé: Từ chối
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **RSA Keypair chỉ tạo một lần**: 
   - Được tạo khi admin ghi thông tin lần đầu
   - Không thể tạo lại (trừ khi reset thẻ)

2. **Private Key không thể export**:
   - Private key được bảo vệ bởi Java Card framework
   - Không thể đọc hoặc export ra ngoài

3. **Public Key có thể lấy bất cứ lúc nào**:
   - Không cần PIN để lấy public key
   - Có thể lấy để đăng ký lại trên server

4. **Challenge chỉ dùng một lần**:
   - Mỗi challenge chỉ dùng một lần
   - Challenge có thời gian expire (5 phút)

5. **Signature Algorithm**:
   - Dùng SHA1withRSA (compatible với Java Card 2.2.1)
   - Không dùng SHA256 (không support trên Java Card 2.2.1)

6. **Customer ID**:
   - Lưu plaintext trên thẻ (không mã hóa)
   - Dùng để identify customer khi verify signature

---

## 🎯 TÓM TẮT

**RSA trong hệ thống**:
- ✅ Tạo keypair trên thẻ (một lần)
- ✅ Public key upload lên server
- ✅ Private key bảo vệ bởi Java Card framework
- ✅ Challenge-Response authentication
- ✅ Signature: SHA1withRSA
- ✅ Key size: 1024 bits
- ✅ Signature size: 128 bytes

**Bảo mật**:
- ✅ Private key không thể export
- ✅ Challenge chỉ dùng một lần
- ✅ Challenge có expire time
- ✅ Server verify bằng public key đã đăng ký

---

**Tài liệu này mô tả chi tiết cơ chế RSA trong hệ thống Smart Card Park.**
