package com.netforge.app.data.file

object NfgFormat {
    val MAGIC = byteArrayOf('N'.code.toByte(), 'F'.code.toByte(), 'O'.code.toByte(), 'R'.code.toByte())
    const val VERSION_1: Byte = 0x01
    const val KDF_ARGON2ID: Byte = 0x01
    const val CIPHER_AES256GCM: Byte = 0x01
    const val RESERVED: Byte = 0x00

    const val HEADER_SIZE = 8
    const val SALT_SIZE = 16
    const val NONCE_SIZE = 12
    const val TAG_SIZE = 16

    fun buildHeader(): ByteArray {
        return byteArrayOf(
            MAGIC[0], MAGIC[1], MAGIC[2], MAGIC[3],
            VERSION_1,
            KDF_ARGON2ID,
            CIPHER_AES256GCM,
            RESERVED
        )
    }
}
