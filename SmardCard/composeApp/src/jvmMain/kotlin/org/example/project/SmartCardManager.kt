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

            val cmd = byteArrayOf(0x80.toByte(), 0x40, 0x00, 0x00, pinBytes.size. toByte()) + pinBytes
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
            val cmd = byteArrayOf(0x80.toByte(), 0x20, 0x00, 0x00, pinBytes.size. toByte()) + pinBytes
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

            val cmd = byteArrayOf(0x80.toByte(), 0x24, 0x00, 0x00, data.size.toByte()) + data
            val response = sendCommand(cmd) ?: return false

            response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00))
        } catch (e: Exception) {
            println("Error changing PIN: ${e.message}")
            false
        }
    }

    fun getPINStatus(): Triple<Int, Boolean, Boolean> {
        return try {
            val cmd = byteArrayOf(0x80.toByte(), 0x41, 0x00, 0x00, 0x00)
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
            val cmd = byteArrayOf(0x80.toByte(), 0x42, 0x00, 0x00, 0x00)
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

    fun writeCustomerInfo(customerID: String, name: String, dateOfBirth: String, phoneNumber: String, cardType: String): Boolean {
        return try {
            val data = ByteArray(95)
            val customerIDBytes = customerID.toByteArray(Charsets.UTF_8)
            val nameBytes = name.toByteArray(Charsets. UTF_8)
            val dobBytes = dateOfBirth.toByteArray(Charsets. UTF_8)
            val phoneBytes = phoneNumber.toByteArray(Charsets.UTF_8)
            val cardTypeBytes = cardType.toByteArray(Charsets.UTF_8)

            customerIDBytes.copyInto(data, 0, 0, minOf(customerIDBytes.size, 15))
            nameBytes.copyInto(data, 15, 0, minOf(nameBytes. size, 50))
            dobBytes.copyInto(data, 65, 0, minOf(dobBytes.size, 10))
            phoneBytes.copyInto(data, 75, 0, minOf(phoneBytes.size, 10))
            cardTypeBytes.copyInto(data, 85, 0, minOf(cardTypeBytes.size, 10))

            val command = byteArrayOf(0x80.toByte(), 0x01, 0x00, 0x00, data.size.toByte()) + data
            val response = this.sendCommand(command)
            println(response)
            response?. takeLast(2)?.toByteArray()?.contentEquals(byteArrayOf(0x90.toByte(), 0x00)) ?: false
        } catch (e: Exception) {
            println("Error writing data to card: ${e.message}")
            false
        }
    }

    fun startPhotoWrite(): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x04, 0x00, 0x00, 0x00)
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00. toByte()))
    }

    fun writePhotoChunk(photoChunk: ByteArray): Boolean {
        val command = byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00, photoChunk.size.toByte()) + photoChunk
        val response = this.sendCommand(command) ?: return false
        println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }

    fun writeCustomerImage(imageData: ByteArray): Boolean {
        if (! startPhotoWrite()) {
            println("Failed to start photo write!")
            return false
        }
        val chunkSize = 200
        var offset = 0
        while (offset < imageData.size) {
            val end = minOf(offset + chunkSize, imageData.size)
            val chunk = imageData.copyOfRange(offset, end)
            val ok = writePhotoChunk(chunk)
            if (!ok) {
                println("Failed writing chunk at offset $offset")
                return false
            }
            offset = end
        }
        val finishCmd = byteArrayOf(0x80.toByte(), 0x06, 0x00, 0x00, 0x00)
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
            val cmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, 0x61) // Read 97 bytes
            val response = sendCommand(cmd) ?: return emptyMap()

            val sw = getStatusWord(response)
            if (sw != 0x9000) {
                println("Failed to read customer info: SW=${sw.toString(16)}")
                return emptyMap()
            }

            val data = response.dropLast(2).toByteArray()
            if (data.size < 97) {
                println("Invalid data size:  ${data.size}")
                return emptyMap()
            }

            // Parse data
            var pos = 0
            val customerID = String(data, pos, 15, Charsets.UTF_8).trim('\u0000', ' ')
            pos += 15
            val name = String(data, pos, 50, Charsets.UTF_8).trim('\u0000', ' ')
            pos += 50
            val dateOfBirth = String(data, pos, 10, Charsets.UTF_8).trim('\u0000', ' ')
            pos += 10
            val phoneNumber = String(data, pos, 10, Charsets.UTF_8).trim('\u0000', ' ')
            pos += 10
            val cardType = String(data, pos, 10, Charsets.UTF_8).trim('\u0000', ' ')
            pos += 10

            // Photo length (2 bytes)
            val photoLengthHigh = data[95]. toInt() and 0xFF
            val photoLengthLow = data[96].toInt() and 0xFF
            val photoLength = (photoLengthHigh shl 8) or photoLengthLow

            println("✅ Read customer info:  ID=$customerID, Name=$name, PhotoLength=$photoLength")

            mapOf(
                "customerID" to customerID,
                "name" to name,
                "dateOfBirth" to dateOfBirth,
                "phoneNumber" to phoneNumber,
                "cardType" to cardType,
                "photoLength" to photoLength. toString()
            )

        } catch (e: Exception) {
            println("❌ Error reading customer info: ${e.message}")
            emptyMap()
        }
    }
    // ✅ HÀM DEBUG - Kiểm tra photoLength
    fun debugPhotoInfo() {
        try {
            val cmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, 0x61)
            val response = sendCommand(cmd)

            if (response != null && response.size >= 99) {
                val data = response.dropLast(2).toByteArray()

                // Parse photo length
                val photoLengthHigh = data[95].toInt() and 0xFF
                val photoLengthLow = data[96].toInt() and 0xFF
                val photoLength = (photoLengthHigh shl 8) or photoLengthLow

                println("🔍 DEBUG INFO:")
                println("   Response size: ${response.size}")
                println("   Data[95] (High): $photoLengthHigh")
                println("   Data[96] (Low): $photoLengthLow")
                println("   Photo Length: $photoLength bytes")
                println("   Raw bytes [95-96]: ${data[95]. toString(16)}, ${data[96].toString(16)}")
            } else {
                println("❌ Invalid response:  ${response?.size ?: 0} bytes")
            }
        } catch (e: Exception) {
            println("❌ Error:  ${e.message}")
        }
    }
    // ✅ HÀM MỚI:  Đọc ảnh khách hàng
    fun readCustomerImage(): ByteArray? {
        return try {
            println("📥 === START READ CUSTOMER IMAGE ===")

            // 1. Đọc thông tin để lấy photoLength
            val infoCmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, 0x61)
            val infoResponse = sendCommand(infoCmd)

            if (infoResponse == null) {
                println("❌ Failed to read info command")
                return null
            }

            println("✅ Info response size: ${infoResponse.size}")

            if (infoResponse.size < 99) {
                println("❌ Response too small: ${infoResponse. size} < 99")
                return null
            }

            val data = infoResponse.dropLast(2).toByteArray()

            // Parse photoLength (bytes at position 95-96)
            val photoLengthHigh = data[95].toInt() and 0xFF
            val photoLengthLow = data[96]. toInt() and 0xFF
            val photoLength = (photoLengthHigh shl 8) or photoLengthLow

            println("📊 Photo Length Info:")
            println("   Position 95 (High byte): $photoLengthHigh (0x${photoLengthHigh.toString(16)})")
            println("   Position 96 (Low byte): $photoLengthLow (0x${photoLengthLow. toString(16)})")
            println("   Calculated length: $photoLength bytes")

            if (photoLength == 0) {
                println("⚠️ Photo length is 0 - no photo on card")
                return null
            }

            if (photoLength > 8000) {
                println("⚠️ Photo length too large: $photoLength > 8000")
                return null
            }

            // 2. Đọc ảnh theo từng chunk
            println("📦 Starting to read photo chunks...")
            val photoData = mutableListOf<Byte>()
            var offset = 0
            val chunkSize = 200
            var chunkCount = 0

            while (offset < photoLength) {
                val p1 = (offset shr 8).toByte()
                val p2 = (offset and 0xFF).toByte()
                val remainingBytes = photoLength - offset
                val requestSize = minOf(chunkSize, remainingBytes)

                val readCmd = byteArrayOf(
                    0x80.toByte(), 0x05,     // CLA, INS
                    p1, p2,                  // P1, P2 (offset)
                    requestSize.toByte()      // Le (chunk size)
                )

                println("📤 Chunk $chunkCount: offset=$offset, request=$requestSize bytes")

                val response = sendCommand(readCmd)
                if (response == null) {
                    println("❌ No response for chunk $chunkCount at offset $offset")
                    break
                }

                val sw = getStatusWord(response)
                if (sw != 0x9000) {
                    println("❌ Error reading chunk $chunkCount: SW=0x${sw.toString(16)}")
                    if (sw == 0x6A86) {
                        println("   (Incorrect P1P2 - offset beyond data)")
                    }
                    break
                }

                val chunk = response.dropLast(2).toByteArray()
                if (chunk.isEmpty()) {
                    println("⚠️ Empty chunk at offset $offset")
                    break
                }

                photoData.addAll(chunk. toList())
                offset += chunk.size
                chunkCount++

                println("✅ Chunk $chunkCount: received ${chunk.size} bytes, total:  ${photoData.size}/$photoLength")
            }

            println("📊 FINAL RESULT:")
            println("   Expected:  $photoLength bytes")
            println("   Received: ${photoData.size} bytes")
            println("   Chunks read: $chunkCount")

            if (photoData. isEmpty()) {
                println("❌ No photo data received")
                return null
            }

            if (photoData.size != photoLength) {
                println("⚠️ Size mismatch but returning data anyway")
            } else {
                println("✅ Photo read successfully!")
            }

            println("📥 === END READ CUSTOMER IMAGE ===")
            photoData. toByteArray()

        } catch (e: Exception) {
            println("❌ Exception reading photo: ${e.message}")
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
        val readCmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, 0x61)
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
                    0x80.toByte(), 0x05,
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
        var pos = 0

        val maKH = String(data, pos, 15).trim('\u0000', ' ')
        pos += 15
        val hoTen = String(data, pos, 50).trim('\u0000', ' ')
        pos += 50
        val ngaySinh = String(data, pos, 10).trim('\u0000', ' ')
        pos += 10
        val soDienThoai = String(data, pos, 10).trim('\u0000', ' ')
        pos += 10
        val loaiThe = String(data, pos, 10).trim('\u0000', ' ')
        pos += 10

        val photoLen = if (data. size >= 97) {
            ((data[95].toInt() and 0xFF) shl 8) or (data[96].toInt() and 0xFF)
        } else 0

        println("Customer:  '$maKH', '$hoTen', Photo length: $photoLen")

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

            val cmd = byteArrayOf(0x80.toByte(), 0x50, 0x00, 0x00, 0x02) + amountBytes
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
            val cmd = byteArrayOf(0x80.toByte(), 0x51, 0x00, 0x00, 0x02)
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

            val cmd = byteArrayOf(0x80.toByte(), 0x52, 0x00, 0x00, 0x02) + amountBytes
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
            val cmd = byteArrayOf(0x80.toByte(), 0x82.toByte(), 0x00, 0x00, 0x00)
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

            val cmd = byteArrayOf(0x80.toByte(), 0x80.toByte(), 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

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

            val cmd = byteArrayOf(0x80.toByte(), 0x81.toByte(), 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

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

            val cmd = byteArrayOf(0x80.toByte(), 0x83.toByte(), 0x00, 0x00, 0x03) + data
            val response = sendCommand(cmd) ?: return false

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

            val cmd = byteArrayOf(0x80.toByte(), 0x84.toByte(), 0x00, 0x00, 0x02) + data
            val response = sendCommand(cmd) ?: return null

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

            val cmd = byteArrayOf(0x80.toByte(), 0x85.toByte(), 0x00, 0x00, 0x02) + data
            val response = sendCommand(cmd) ?: return false

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


}