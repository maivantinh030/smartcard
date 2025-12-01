package org.example.project

import javax.smartcardio.Card
import javax.smartcardio.CardChannel
import javax.smartcardio.CardTerminals
import javax.smartcardio.CommandAPDU
import javax.smartcardio.TerminalFactory

class SmartCardManager {
    private var terminals: CardTerminals? = null
    private var card: Card? = null
    private var channel: CardChannel? = null

    init {
        try {
            val factory = TerminalFactory.getDefault()
            terminals = factory.terminals()
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

    fun connectToCard(readerName: String? = null): Boolean {
        return try {
            val terminal = if (readerName != null) {
                terminals?.list()?.find { it.name == readerName }
            } else {
                terminals?.list()?.firstOrNull()
            }

            if (terminal?.isCardPresent == true) {
                card = terminal.connect("*")
                channel = card?.basicChannel
                selectApplet()
            } else {
                println("No card present in reader: ${readerName ?: "default"}")
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
                byteArrayOf(0x11, 0x11, 0x11, 0x11, 0x11,0x00))
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
            println("Error disconnecting: ${e.message}")
        }
    }
    fun createPIN(pin: String): Boolean {
        return try {
            val pinBytes = pin.toByteArray()
            if (pinBytes.size < 4 || pinBytes.size > 8) {
                println("PIN must be 4-8 characters")
                return false
            }

            val cmd = byteArrayOf(0x80.toByte(), 0x40, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
            val response = sendCommand(cmd) ?: return false

            response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00))
        } catch (e: Exception) {
            println("Error creating PIN: ${e.message}")
            false
        }
    }

    fun verifyPIN(pin: String): Boolean {
        return try {
            val pinBytes = pin.toByteArray()
            val cmd = byteArrayOf(0x80.toByte(), 0x20, 0x00, 0x00, pinBytes.size.toByte()) + pinBytes
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
            println("Sending PIN Status Command: ${cmd.joinToString(" ") { String.format("%02X", it) }}")
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
    fun writeCustomerInfo(customerID:String,name:String, dateOfBirth:String,phoneNumber:String,cardType:String): Boolean {
        return try{
            val data = ByteArray(95)
            val customerIDBytes = customerID.toByteArray(Charsets.UTF_8)
            val nameBytes = name.toByteArray(Charsets.UTF_8)
            val dobBytes = dateOfBirth.toByteArray(Charsets.UTF_8)
            val phoneBytes = phoneNumber.toByteArray(Charsets.UTF_8)
            val cardTypeBytes = cardType.toByteArray(Charsets.UTF_8)

            customerIDBytes.copyInto(data, 0, 0, minOf(customerIDBytes.size, 15))
            nameBytes.copyInto(data, 15, 0, minOf(nameBytes.size, 50))
            dobBytes.copyInto(data, 65, 0, minOf(dobBytes.size, 10))
            phoneBytes.copyInto(data, 75, 0, minOf(phoneBytes.size, 10))
            cardTypeBytes.copyInto(data, 85, 0, minOf(cardTypeBytes.size, 10))
            val command = byteArrayOf(0x80.toByte(), 0x01, 0x00, 0x00,data.size.toByte()) + data
            val response = this.sendCommand(command)
            println(response)
            response?.takeLast(2)?.toByteArray()?.contentEquals(byteArrayOf(0x90.toByte(), 0x00)) ?: false
        }
        catch (e: Exception){
            println("Error writing data to card: ${e.message}")
            false  // ✅ Return false khi có lỗi
        }
    }
    fun startPhotoWrite():Boolean{

            val command = byteArrayOf(0x80.toByte(), 0x04, 0x00, 0x00,0x00)
            val response = this.sendCommand(command)?:return false
            println(response)
            return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }
    fun writePhotoChunk(photoChunk:ByteArray): Boolean{

            val command = byteArrayOf(0x80.toByte(), 0x02, 0x00, 0x00,photoChunk.size.toByte()) + photoChunk
            val response = this.sendCommand(command)?:return false
            println(response)
        return response.takeLast(2).toByteArray().contentEquals(byteArrayOf(0x90.toByte(), 0x00.toByte()))
    }
    fun writeCustomerImage(imageData: ByteArray): Boolean {
        if (!startPhotoWrite()) {
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
    fun readCustomerDataComplete(): Customer? {
        return try {
            // 1. Đọc customer info (97 bytes)
            val basicInfo = readCustomerBasicInfo()
            if (basicInfo == null) {
                println("Failed to read basic info")
                return null
            }
            // 2. Parse customer data
            val customer = parseCustomerBasicInfo(basicInfo)
            // 3. Đọc photo nếu có
            val photoData = readPhotoSimple()
            // 4. Combine
            customer.copy(anhKH = photoData)
        } catch (e: Exception) {
            println("Error reading complete data: ${e.message}")
            null
        }
    }

    private fun readCustomerBasicInfo(): ByteArray? {
        val readCmd = byteArrayOf(0x80.toByte(), 0x03, 0x00, 0x00, 0x61) // Read 97 bytes
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
                // Create command: INS=05, P1P2=offset, Le=chunkSize
                val p1 = (offset shr 8) and 0xFF
                val p2 = offset and 0xFF

                val chunkCmd = byteArrayOf(
                    0x80.toByte(), 0x05,           // CLA, INS
                    p1.toByte(), p2.toByte(),      // P1, P2 (offset)
                    chunkSize.toByte()             // Le (chunk size)
                )

                val response = sendCommand(chunkCmd)
                if (response == null) {
                    println("No response at offset $offset")
                    break
                }

                val sw = getStatusWord(response)
                if (sw != 0x9000) {
                    if (sw == 0x6A86) { // Incorrect P1P2 = beyond end
                        println("End of photo reached")
                    } else {
                        println("Error reading chunk: SW=${sw.toString(16)}")
                    }
                    break
                }

                val chunk = response.dropLast(2).toByteArray()

                // ✅ Nếu chunk rỗng = hết data
                if (chunk.isEmpty()) {
                    println("Empty chunk - end of photo")
                    break
                }

                // Add chunk to photo
                photoData.addAll(chunk.toList())

                // ✅ Nếu chunk nhỏ hơn yêu cầu = chunk cuối
                if (chunk.size < chunkSize) {
                    println("Last chunk: ${chunk.size} bytes")
                    break
                }

                offset += chunk.size
                println("Read chunk: ${chunk.size} bytes, total: ${photoData.size}")
            }

            return if (photoData.isNotEmpty()) {
                println("Photo read complete: ${photoData.size} bytes")
                photoData.toByteArray()
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

        // Photo length at pos 95-96 (for info only)
        val photoLen = if (data.size >= 97) {
            ((data[95].toInt() and 0xFF) shl 8) or (data[96].toInt() and 0xFF)
        } else 0

        println("Customer: '$maKH', '$hoTen', Photo length: $photoLen")

        return Customer(maKH, hoTen, ngaySinh, soDienThoai, loaiThe, null)
    }

    private fun getStatusWord(response: ByteArray): Int {
        return if (response.size >= 2) {
            val sw1 = response[response.size - 2].toInt() and 0xFF
            val sw2 = response[response.size - 1].toInt() and 0xFF
            (sw1 shl 8) or sw2
        } else -1
    }

}