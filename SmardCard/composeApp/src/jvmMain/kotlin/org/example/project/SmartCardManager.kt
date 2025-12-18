package org.example.project

import javax.smartcardio. Card
import javax.smartcardio.CardChannel
import javax. smartcardio.CardTerminals
import javax.smartcardio.CommandAPDU
import javax.smartcardio. TerminalFactory

class SmartCardManager {
    private var terminals: CardTerminals? = null
    private var card: Card? = null
    private var channel: CardChannel? = null

    init {
        try {
            val factory = TerminalFactory.getDefault()
            terminals = factory. terminals()
        } catch (e: Exception) {
            println("Error initializing SmartCard: ${e.message}")
        }
    }

    fun listReaders(): List<String> {
        return try {
            terminals?.list()?.map { it.name } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun connectToCard(readerName: String?  = null): Boolean {
        return try {
            val terminal = if (readerName != null) {
                terminals?.list()?.find { it.name == readerName }
            } else {
                terminals?.list()?.firstOrNull()
            }

            if (terminal?. isCardPresent == true) {
                card = terminal.connect("*")
                channel = card?.basicChannel
                selectApplet()
            } else {
                println("No card present in reader:  ${readerName ?: "default"}")
                false
            }
        } catch (e: Exception) {
            println("Error connecting to card: ${e.message}")
            false
        }
    }

    fun sendCommand(commandApdu: ByteArray): ByteArray? {
        return try {
            val command = CommandAPDU(commandApdu)
            val response = channel?.transmit(command)
            response?.bytes
        } catch (e: Exception) {
            null
        }
    }

    private fun selectApplet(): Boolean {
        return try {
            val selectCmd = CommandAPDU(0x00, 0xA4, 0x04, 0x00,
                byteArrayOf(0x11, 0x11, 0x11, 0x11, 0x11, 0x00))
            val response = channel?.transmit(selectCmd)
            response?.sw == 0x9000
        } catch (e: Exception) {
            println("Error selecting applet: ${e.message}")
            false
        }
    }

    fun disconnect() {
        try {
            card?.disconnect(true)
        } catch (e: Exception) {
            println("Error disconnecting:  ${e.message}")
        }
    }

    fun createPIN(pin: String): Boolean {
        return try {
            val pinBytes = pin.toByteArray()
            if (pinBytes.size < 4 || pinBytes.size > 8) {
                println("PIN must be 4-8 characters")
                return false
            }

            val cmd = byteArrayOf(0x80.toByte(), 0x01, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
            val response = sendCommand(cmd) ?: return false

            response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90. toByte(), 0x00))
        } catch (e: Exception) {
            println("Error creating PIN: ${e.message}")
            false
        }
    }

    fun verifyPIN(pin: String): Boolean {
        return try {
            val pinBytes = pin.toByteArray()
            val cmd = byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("PIN verified successfully")
                    true
                }
                0x6983 -> {
                    println("PIN blocked - too many wrong attempts")
                    false
                }
                0x6A80 -> {
                    println("Wrong PIN")
                    false
                }
                else -> {
                    println("PIN verification failed: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error verifying PIN: ${e.message}")
            false
        }
    }

    fun changePIN(oldPin: String, newPin: String): Boolean {
        return try {
            val oldPinBytes = oldPin.toByteArray()
            val newPinBytes = newPin.toByteArray()

            if (newPinBytes.size < 4 || newPinBytes.size > 8) {
                println("New PIN must be 4-8 characters")
                return false
            }

            val data = byteArrayOf(oldPinBytes.size.toByte()) +
                    oldPinBytes +
                    byteArrayOf(newPinBytes.size.toByte()) +
                    newPinBytes

            val cmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, data.size.toByte()) + data
            val response = sendCommand(cmd) ?: return false

            response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00))
        } catch (e: Exception) {
            println("Error changing PIN: ${e.message}")
            false
        }
    }

    fun getPINStatus(): Triple<Int, Boolean, Boolean> {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x04, 0x00, 0x00, 0x00)
            println("Sending PIN Status Command:  ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = this.sendCommand(cmd) ?: return Triple(-1, false, false)
            println("PIN Status Response: ${response.joinToString(" ") { String.format("%02X", it) }}")
            val sw = getStatusWord(response)
            if (sw == 0x9000 && response.size >= 5) {
                val data = response.dropLast(2).toByteArray()
                val triesLeft = data[0].toInt() and 0xFF
                val pinCreated = data[1].toInt() == 1
                val pinValidated = data[2].toInt() == 1

                Triple(triesLeft, pinCreated, pinValidated)
            } else {
                Triple(-1, false, false)
            }
        } catch (e: Exception) {
            println("Error getting PIN status: ${e.message}")
            Triple(-1, false, false)
        }
    }

    fun resetPinCounter(): Boolean {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x05, 0x00, 0x00, 0x00)
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("PIN counter reset successfully")
                    true
                }
                else -> {
                    println("Failed to reset PIN counter:  SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error resetting PIN counter: ${e.message}")
            false
        }
    }

    fun writeCustomerInfo( name: String, dateOfBirth: String, phoneNumber: String): Boolean {
        return try {
            // Padded field sizes (AES blocks) - KHÔNG gồm customerID nữa
            val LEN_NAME = 64
            val LEN_DOB = 16
            val LEN_PHONE = 16
            val data = ByteArray(LEN_NAME + LEN_DOB + LEN_PHONE) // 96 bytes (không có customerID 16 bytes)

            // Copy và để remaining bytes là 0
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            nameBytes.copyInto(data, 0, 0, minOf(nameBytes.size, LEN_NAME))

            val dobBytes = dateOfBirth.toByteArray(Charsets.UTF_8)
            dobBytes.copyInto(data, LEN_NAME, 0, minOf(dobBytes.size, LEN_DOB))

            val phoneBytes = phoneNumber.toByteArray(Charsets.UTF_8)
            phoneBytes.copyInto(data, LEN_NAME + LEN_DOB, 0, minOf(phoneBytes.size, LEN_PHONE))

            val command = byteArrayOf(0x80.toByte(), 0x07, 0x00, 0x00, data.size.toByte()) + data
            val response = this.sendCommand(command)
            response?.takeLast(2)?.toByteArray()?.contentEquals(byteArrayOf(0x90.toByte(), 0x00)) ?: false
        } catch (e: Exception) {
            println("Error writing data to card: ${e.message}")
            false
        }
    }

    fun startPhotoWrite(): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x08, 0x00, 0x00, 0x00)
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00. toByte()))
    }

    fun writePhotoChunk(photoChunk: ByteArray): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x09, 0x00, 0x00, photoChunk.size.toByte()) + photoChunk
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }

    fun finishPhotoWrite(): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x0A, 0x00, 0x00, 0x00)
        val response = this.sendCommand(command) ?: return false
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }

    fun writeCustomerImage(imageData: ByteArray): Boolean {
        println("🖼️ Bắt đầu ghi ảnh (${imageData.size} bytes)...")
        if (!startPhotoWrite()) {
            println("❌ Không thể khởi tạo ghi ảnh!")
            return false
        }
        val chunkSize = 200
        var offset = 0
        var chunkCount = 0
        while (offset < imageData.size) {
            val end = minOf(offset + chunkSize, imageData.size)
            val chunk = imageData.copyOfRange(offset, end)
            println("  📤 Ghi chunk ${++chunkCount}: offset=$offset, size=${chunk.size}")
            val ok = writePhotoChunk(chunk)
            if (!ok) {
                println("  ❌ Lỗi ghi chunk tại offset $offset")
                return false
            }
            offset = end
        }
        println("  ✅ Đã ghi xong tất cả chunks")
        val finishCmd = byteArrayOf(0x80.toByte(), 0x0A, 0x00, 0x00, 0x00)
        val finishResponse = sendCommand(finishCmd) ?: return false
        val finishOk = finishResponse.takeLast(2).toByteArray()
            .contentEquals(byteArrayOf(0x90.toByte(), 0x00))

        if (finishOk) {
            println("Photo upload completed successfully!")
            return true
        }
        return true
    }

    // ✅ HÀM MỚI: Đọc thông tin khách hàng
    fun readCustomerInfo(): Map<String, String> {
        return try {
            val LEN_ID = 15
            val LEN_NAME = 64
            val LEN_DOB = 16
            val LEN_PHONE = 16
            val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2 // +2 for photoLength

            println("📖 Đọc thông tin khách hàng (INFO_LEN=$INFO_LEN)...")
            val cmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte()) // expect 0x72 bytes
            val response = sendCommand(cmd) ?: run {
                println("❌ Lệnh đọc thông tin thất bại")
                return emptyMap()
            }

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("❌ Status word lỗi: 0x${sw.toString(16)}")
                return emptyMap()
            }

            val data = response.dropLast(2).toByteArray()
            if (data.size < INFO_LEN) {
                println("❌ Dữ liệu không đủ: ${data.size} < $INFO_LEN")
                return emptyMap()
            }

            var pos = 0
            val customerID = String(data, pos, LEN_ID, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_ID
            val name = String(data, pos, LEN_NAME, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_NAME
            val dateOfBirth = String(data, pos, LEN_DOB, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_DOB
            val phoneNumber = String(data, pos, LEN_PHONE, Charsets.UTF_8).trim('\u0000', ' ')
            pos += LEN_PHONE

            val photoLengthHigh = data[pos].toInt() and 0xFF
            val photoLengthLow = data[pos + 1].toInt() and 0xFF
            val photoLength = (photoLengthHigh shl 8) or photoLengthLow

            println("✅ Đã đọc thông tin: name='$name', photo=$photoLength bytes")

            mapOf(
                "customerID" to customerID,
                "name" to name,
                "dateOfBirth" to dateOfBirth,
                "phoneNumber" to phoneNumber,
                "photoLength" to photoLength.toString()
            )
        } catch (e: Exception) {
            println("❌ Lỗi đọc thông tin: ${e.message}")
            emptyMap()
        }
    }
    // ✅ HÀM DEBUG - Kiểm tra photoLength
    fun debugPhotoInfo() {
        try {
            val LEN_ID = 15; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
            val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2
            val cmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
            val response = sendCommand(cmd)

            if (response != null && response.size >= INFO_LEN + 2) {
                val data = response.dropLast(2).toByteArray()
                val photoLenHigh = data[LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE].toInt() and 0xFF
                val photoLenLow = data[LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 1].toInt() and 0xFF
                val photoLength = (photoLenHigh shl 8) or photoLenLow

                println("🔍 DEBUG INFO:")
                println("   Response size: ${response.size}")
                println("   Photo Length: $photoLength bytes")
                println("   Raw bytes [${INFO_LEN - 2}-${INFO_LEN - 1}]: ${data[INFO_LEN - 2].toString(16)}, ${data[INFO_LEN - 1].toString(16)}")
            } else {
                println("❌ Invalid response: ${response?.size ?: 0} bytes")
            }
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        }
    }
    // ✅ HÀM MỚI:  Đọc ảnh khách hàng
    fun readCustomerImage(): ByteArray? {
        return try {
            val LEN_ID = 15  // ✅ FIX: Match với readCustomerInfo()
            val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
            val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2

            println("📸 Đọc info để lấy photo length...")
            val infoCmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
            val infoResponse = sendCommand(infoCmd) ?: run {
                println("❌ Lệnh đọc info thất bại")
                return null
            }

            println("📦 Info response size: ${infoResponse.size}")
            if (infoResponse.size < INFO_LEN + 2) {
                println("❌ Response quá nhỏ: ${infoResponse.size} < ${INFO_LEN + 2}")
                return null
            }

            val data = infoResponse.dropLast(2).toByteArray()
            val pos = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE
            val photoLengthHigh = data[pos].toInt() and 0xFF
            val photoLengthLow = data[pos + 1].toInt() and 0xFF
            val photoLength = (photoLengthHigh shl 8) or photoLengthLow

            println("🖼️ Photo length từ card: $photoLength bytes (high=$photoLengthHigh, low=$photoLengthLow)")

            if (photoLength == 0) {
                println("⚠️ Không có ảnh trên thẻ")
                return null
            }
            if (photoLength > 8000) {
                println("⚠️ Kích thước ảnh quá lớn: $photoLength > 8000")
                return null
            }

            val photoData = mutableListOf<Byte>()
            var offset = 0
            val chunkSize = 200

            println("🔄 Bắt đầu đọc ảnh từ thẻ...")
            while (offset < photoLength) {
                val p1 = (offset shr 8).toByte()
                val p2 = (offset and 0xFF).toByte()
                val requestSize = minOf(chunkSize, photoLength - offset)

                println("  📥 Đọc chunk: offset=$offset, size=$requestSize")
                val readCmd = byteArrayOf(0x80.toByte(), 0x0C, p1, p2, requestSize.toByte())
                val response = sendCommand(readCmd)
                
                if (response == null) {
                    println("  ❌ Chunk response null")
                    break
                }

                val sw = getStatusWord(response)
                println("  Status word: 0x${sw.toString(16).padStart(4, '0')}")
                
                if (sw != 0x9000) {
                    println("  ❌ Lỗi đọc chunk")
                    break
                }

                val chunk = response.dropLast(2).toByteArray()
                println("  ✅ Đã đọc ${chunk.size} bytes")
                
                if (chunk.isEmpty()) {
                    println("  ❌ Chunk rỗng")
                    break
                }

                photoData.addAll(chunk.toList())
                offset += chunk.size
            }

            println("✅ Đã đọc xong ảnh: ${photoData.size} bytes")
            if (photoData.isEmpty()) null else photoData.toByteArray()
        } catch (e: Exception) {
            println("❌ Exception đọc ảnh: ${e.message}")
            e.printStackTrace()
            null
        }
    }

    fun readCustomerDataComplete(): Customer? {
        return try {
            val basicInfo = readCustomerBasicInfo()
            if (basicInfo == null) {
                println("Failed to read basic info")
                return null
            }
            val customer = parseCustomerBasicInfo(basicInfo)
            val photoData = readPhotoSimple()
            customer. copy(anhKH = photoData)
        } catch (e: Exception) {
            println("Error reading complete data: ${e.message}")
            null
        }
    }

    private fun readCustomerBasicInfo(): ByteArray? {
        val LEN_ID = 15; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
        val INFO_LEN = LEN_ID + LEN_NAME + LEN_DOB + LEN_PHONE + 2
        val readCmd = byteArrayOf(0x80.toByte(), 0x0B, 0x00, 0x00, INFO_LEN.toByte())
        val response = sendCommand(readCmd) ?: return null

        val sw = getStatusWord(response)
        return if (sw == 0x9000) {
            response.dropLast(2).toByteArray()
        } else {
            println("Failed to read basic info: SW=${sw.toString(16)}")
            null
        }
    }

    private fun readPhotoSimple(): ByteArray? {
        return try {
            val chunkSize = 200
            var offset = 0
            val photoData = mutableListOf<Byte>()

            println("Reading photo in chunks...")

            while (true) {
                val p1 = (offset shr 8) and 0xFF
                val p2 = offset and 0xFF

                val chunkCmd = byteArrayOf(
                    0x80.toByte(), 0x0C,
                    p1.toByte(), p2.toByte(),
                    chunkSize.toByte()
                )

                val response = sendCommand(chunkCmd)
                if (response == null) {
                    println("No response at offset $offset")
                    break
                }

                val sw = getStatusWord(response)
                if (sw != 0x9000) {
                    if (sw == 0x6A86) {
                        println("End of photo reached")
                    } else {
                        println("Error reading chunk:  SW=${sw.toString(16)}")
                    }
                    break
                }

                val chunk = response.dropLast(2).toByteArray()

                if (chunk.isEmpty()) {
                    println("Empty chunk - end of photo")
                    break
                }

                photoData.addAll(chunk.toList())

                if (chunk.size < chunkSize) {
                    println("Last chunk:  ${chunk.size} bytes")
                    break
                }

                offset += chunk.size
                println("Read chunk: ${chunk. size} bytes, total: ${photoData.size}")
            }

            return if (photoData.isNotEmpty()) {
                println("Photo read complete: ${photoData.size} bytes")
                photoData. toByteArray()
            } else {
                println("No photo data")
                null
            }

        } catch (e: Exception) {
            println("Error reading photo: ${e.message}")
            null
        }
    }
    private fun parseCustomerBasicInfo(data: ByteArray): Customer {
        val LEN_ID = 16; val LEN_NAME = 64; val LEN_DOB = 16; val LEN_PHONE = 16
        var pos = 0

        val maKH = String(data, pos, LEN_ID).trim('\u0000', ' ')
        pos += LEN_ID
        val hoTen = String(data, pos, LEN_NAME).trim('\u0000', ' ')
        pos += LEN_NAME
        val ngaySinh = String(data, pos, LEN_DOB).trim('\u0000', ' ')
        pos += LEN_DOB
        val soDienThoai = String(data, pos, LEN_PHONE).trim('\u0000', ' ')
        pos += LEN_PHONE

        val photoLen = if (data.size >= pos + 2) {
            ((data[pos].toInt() and 0xFF) shl 8) or (data[pos + 1].toInt() and 0xFF)
        } else 0

        val loaiThe = "" // removed from card model

        println("Customer: '$maKH', '$hoTen', Photo length: $photoLen")
        return Customer(maKH, hoTen, ngaySinh, soDienThoai, loaiThe, null)
    }

    private fun getStatusWord(response: ByteArray): Int {
        return if (response.size >= 2) {
            val sw1 = response[response.size - 2].toInt() and 0xFF
            val sw2 = response[response. size - 1].toInt() and 0xFF
            (sw1 shl 8) or sw2
        } else -1
    }

    // ==================== BALANCE MANAGEMENT ====================

    fun rechargeBalance(amount: Int): Boolean {
        return try {
            if (amount <= 0 || amount > 30000) {
                println("Invalid amount:  $amount")
                return false
            }

            val amountBytes = byteArrayOf(
                (amount shr 8).toByte(),
                (amount and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x0D, 0x00, 0x00, 0x02) + amountBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Recharge successful: $amount VNĐ")
                    true
                }
                else -> {
                    println("Recharge failed: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error recharging balance: ${e.message}")
            false
        }
    }

    fun checkBalance(): Int {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x0E, 0x00, 0x00, 0x02)
            val response = sendCommand(cmd) ?: return -1

            val sw = getStatusWord(response)
            if (sw == 0x9000 && response.size >= 4) {
                val data = response.dropLast(2).toByteArray()
                val balance = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)
                println("Current balance: $balance VNĐ")
                balance
            } else {
                println("Failed to check balance: SW=${sw. toString(16)}")
                -1
            }
        } catch (e: Exception) {
            println("Error checking balance: ${e.message}")
            -1
        }
    }

    fun makePayment(amount: Int): Boolean {
        return try {
            if (amount <= 0) {
                println("Invalid payment amount: $amount")
                return false
            }

            val amountBytes = byteArrayOf(
                (amount shr 8).toByte(),
                (amount and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x80.toByte(), 0x0F, 0x00, 0x00, 0x02) + amountBytes
            val response = sendCommand(cmd) ?: return false

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Payment successful: $amount VNĐ")
                    true
                }
                0x6901 -> {
                    println("Insufficient balance")
                    false
                }
                else -> {
                    println("Payment failed: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error making payment: ${e.message}")
            false
        }
    }

    // ==================== GAME MANAGEMENT ====================

    fun readGames(): List<GameEntry> {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x13, 0x00, 0x00, 0x00)
            val response = sendCommand(cmd) ?: return emptyList()

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("Failed to read games: SW=${sw.toString(16)}")
                return emptyList()
            }

            val data = response.dropLast(2).toByteArray()
            if (data.size < 2) {
                return emptyList()
            }

            val gameCount = ((data[0].toInt() and 0xFF) shl 8) or (data[1].toInt() and 0xFF)

            if (gameCount == 0) {
                println("No games found")
                return emptyList()
            }

            val games = mutableListOf<GameEntry>()
            var pos = 2

            for (i in 0 until gameCount) {
                if (pos + 3 > data.size) break

                val tickets = data[pos].toInt() and 0xFF
                val gameCodeHigh = data[pos + 1].toInt() and 0xFF
                val gameCodeLow = data[pos + 2]. toInt() and 0xFF
                val gameCode = (gameCodeHigh shl 8) or gameCodeLow

                games.add(GameEntry(gameCode, tickets))
                pos += 3
            }

            println("Read ${games.size} games")
            games

        } catch (e: Exception) {
            println("Error reading games: ${e.message}")
            emptyList()
        }
    }

    fun addOrIncreaseTickets(gameCode: Int, tickets: Int): Boolean {
        return try {
            if (tickets <= 0 || tickets > 255) {
                println("Invalid tickets amount: $tickets")
                return false
            }

            val data = byteArrayOf(
                tickets.toByte(),
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x00, 0x11, 0x00, 0x00, 0x03) + data
            println("APDU >> ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = sendCommand(cmd) ?: return false
            println("APDU << ${response.joinToString(" ") { String.format("%02X", it) }}")

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Added/Increased $tickets tickets for game $gameCode")
                    true
                }
                else -> {
                    println("Failed to add tickets: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error adding tickets: ${e.message}")
            false
        }
    }

    fun decreaseGameTickets(gameCode: Int, tickets: Int): Boolean {
        return try {
            if (tickets <= 0 || tickets > 255) {
                println("Invalid tickets amount: $tickets")
                return false
            }

            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte(),
                tickets.toByte()
            )

            val cmd = byteArrayOf(0x00, 0x12, 0x00, 0x00, 0x03) + data
            println("APDU >> ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = sendCommand(cmd) ?: return false
            println("APDU << ${response.joinToString(" ") { String.format("%02X", it) }}")

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Decreased $tickets tickets for game $gameCode")
                    true
                }
                0x6901 -> {
                    println("Insufficient tickets")
                    false
                }
                else -> {
                    println("Failed to decrease tickets: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error decreasing tickets: ${e. message}")
            false
        }
    }

    fun updateGameTickets(gameCode: Int, newTickets: Int): Boolean {
        return try {
            if (newTickets < 0 || newTickets > 255) {
                println("Invalid tickets amount: $newTickets")
                return false
            }

            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte(),
                newTickets.toByte()
            )

            val cmd = byteArrayOf(0x00, 0x14, 0x00, 0x00, 0x03) + data
            println("APDU >> ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = sendCommand(cmd) ?: return false
            println("APDU << ${response.joinToString(" ") { String.format("%02X", it) }}")

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Updated game $gameCode to $newTickets tickets")
                    true
                }
                else -> {
                    println("Failed to update tickets: SW=${sw. toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error updating tickets: ${e.message}")
            false
        }
    }

    fun findGame(gameCode: Int): GameEntry? {
        return try {
            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x00, 0x15, 0x00, 0x00, 0x02) + data
            println("APDU >> ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = sendCommand(cmd) ?: return null
            println("APDU << ${response.joinToString(" ") { String.format("%02X", it) }}")

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("Failed to find game: SW=${sw.toString(16)}")
                return null
            }

            val responseData = response.dropLast(2).toByteArray()
            if (responseData.isEmpty() || responseData[0].toInt() == 0) {
                println("Game $gameCode not found")
                return null
            }

            if (responseData.size >= 4) {
                val tickets = responseData[1].toInt() and 0xFF
                val foundGameCodeHigh = responseData[2]. toInt() and 0xFF
                val foundGameCodeLow = responseData[3].toInt() and 0xFF
                val foundGameCode = (foundGameCodeHigh shl 8) or foundGameCodeLow

                println("Found game $foundGameCode with $tickets tickets")
                GameEntry(foundGameCode, tickets)
            } else {
                null
            }
        } catch (e: Exception) {
            println("Error finding game: ${e. message}")
            null
        }
    }

    fun removeGame(gameCode: Int): Boolean {
        return try {
            val data = byteArrayOf(
                (gameCode shr 8).toByte(),
                (gameCode and 0xFF).toByte()
            )

            val cmd = byteArrayOf(0x00, 0x16, 0x00, 0x00, 0x02) + data
            println("APDU >> ${cmd.joinToString(" ") { String.format("%02X", it) }}")
            val response = sendCommand(cmd) ?: return false
            println("APDU << ${response.joinToString(" ") { String.format("%02X", it) }}")

            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Removed game $gameCode")
                    true
                }
                0x6A82 -> {
                    println("Game $gameCode not found")
                    false
                }
                else -> {
                    println("Failed to remove game: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error removing game: ${e.message}")
            false
        }
    }

    // ==================== RSA AUTHENTICATION ====================

    /**
     * Set Customer ID for RSA authentication (plain text, max 15 bytes)
     * INS: 0x90
     */
    fun setCustomerID(customerId: String): Boolean {
        return try {
            val idBytes = customerId.toByteArray().take(15).toByteArray()
            val cmd = byteArrayOf(0x80.toByte(), 0x17, 0x00, 0x00, idBytes.size.toByte()) + idBytes
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("Customer ID set successfully")
                    true
                }
                else -> {
                    println("Failed to set customer ID: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error setting customer ID: ${e.message}")
            false
        }
    }

    /**
     * Get Customer ID (no PIN required, plain text)
     * INS: 0x91
     */
    fun getCustomerIDRSA(): String? {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x18, 0x00, 0x00, 0x0F) // 15 bytes
            val response = sendCommand(cmd) ?: return null
            
            val sw = getStatusWord(response)
            if (sw == 0x9000) {
                val idBytes = response.dropLast(2).toByteArray()
                String(idBytes).trim('\u0000', ' ')
            } else {
                println("Failed to get customer ID: SW=${sw.toString(16)}")
                null
            }
        } catch (e: Exception) {
            println("Error getting customer ID: ${e.message}")
            null
        }
    }

    /**
     * Set RSA private key exponent (128 bytes for RSA-1024)
     * INS: 0x92
     */
    fun setRSAExponent(exponent: ByteArray): Boolean {
        return try {
            if (exponent.size != 128) {
                println("RSA exponent must be 128 bytes, got ${exponent.size}")
                return false
            }
            
            val cmd = byteArrayOf(0x80.toByte(), 0x19, 0x00, 0x00, 0x80.toByte()) + exponent
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("RSA exponent set successfully")
                    true
                }
                else -> {
                    println("Failed to set RSA exponent: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error setting RSA exponent: ${e.message}")
            false
        }
    }

    /**
     * Set RSA private key modulus (128 bytes for RSA-1024)
     * INS: 0x93
     */
    fun setRSAModulus(modulus: ByteArray): Boolean {
        return try {
            if (modulus.size != 128) {
                println("RSA modulus must be 128 bytes, got ${modulus.size}")
                return false
            }
            
            val cmd = byteArrayOf(0x80.toByte(), 0x1A, 0x00, 0x00, 0x80.toByte()) + modulus
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    println("RSA modulus set successfully")
                    true
                }
                else -> {
                    println("Failed to set RSA modulus: SW=${sw.toString(16)}")
                    false
                }
            }
        } catch (e: Exception) {
            println("Error setting RSA modulus: ${e.message}")
            false
        }
    }

    /**
     * Sign a challenge with RSA private key (SHA1withRSA)
     * INS: 0x94
     * Input: 32 bytes challenge
     * Output: 128 bytes signature (RSA-1024)
     */
    fun signChallenge(challenge: ByteArray): ByteArray? {
        return try {
            if (challenge.size != 32) {
                println("Challenge must be 32 bytes, got ${challenge.size}")
                return null
            }
            
            // Card expects exactly 32 bytes in the incoming buffer; no Le byte is needed.
            val cmd = byteArrayOf(0x80.toByte(), 0x1B, 0x00, 0x00, 0x20) + challenge
            val response = sendCommand(cmd) ?: return null
            
            val sw = getStatusWord(response)
            when (sw) {
                0x9000 -> {
                    val signature = response.dropLast(2).toByteArray()
                    println("Challenge signed successfully, signature size: ${signature.size}")
                    signature
                }
                0x6A88 -> {
                    println("RSA not ready (keys not set)")
                    null
                }
                else -> {
                    println("Failed to sign challenge: SW=${sw.toString(16)}")
                    null
                }
            }
        } catch (e: Exception) {
            println("Error signing challenge: ${e.message}")
            null
        }
    }

    /**
     * Get RSA status (check if keys are configured)
     * INS: 0x95
     * Returns: true if RSA ready, false otherwise
     */
    fun getRSAStatus(): Boolean {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x1C, 0x00, 0x00, 0x01)
            val response = sendCommand(cmd) ?: return false
            
            val sw = getStatusWord(response)
            if (sw == 0x9000) {
                val status = response.dropLast(2).firstOrNull() ?: 0x00
                status.toInt() == 0x01
            } else {
                println("Failed to get RSA status: SW=${sw.toString(16)}")
                false
            }
        } catch (e: Exception) {
            println("Error getting RSA status: ${e.message}")
            false
        }
    }

}