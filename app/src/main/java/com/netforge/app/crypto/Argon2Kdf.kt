package com.netforge.app.crypto

import org.bouncycastle.crypto.generators.Argon2BytesGenerator
import org.bouncycastle.crypto.params.Argon2Parameters

object Argon2Kdf {

    const val SALT_LENGTH = 16
    const val KEY_LENGTH = 32
    const val ITERATIONS = 3
    const val MEMORY_KB = 65536
    const val PARALLELISM = 2

    fun deriveKey(passphrase: CharArray, salt: ByteArray): ByteArray {
        val params = Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
            .withVersion(Argon2Parameters.ARGON2_VERSION_13)
            .withIterations(ITERATIONS)
            .withMemoryAsKB(MEMORY_KB)
            .withParallelism(PARALLELISM)
            .withSalt(salt)
            .build()

        val generator = Argon2BytesGenerator()
        generator.init(params)
        val result = ByteArray(KEY_LENGTH)
        generator.generateBytes(passphrase, result, 0, KEY_LENGTH)
        return result
    }

    fun deriveKey(passphrase: String, salt: ByteArray): ByteArray {
        return deriveKey(passphrase.toCharArray(), salt)
    }
}
