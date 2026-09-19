package com.netforge.app.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.Arrays
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Seal {

    private const val GCM_TAG_LENGTH_BITS = 128 // 16 bytes
    private const val ALGORITHM = "AES/GCM/NoPadding"

    fun gzipCompress(data: ByteArray): ByteArray {
        val bos = ByteArrayOutputStream()
        GZIPOutputStream(bos).use { gzip ->
            gzip.write(data)
            gzip.finish()
        }
        return bos.toByteArray()
    }

    fun gzipDecompress(compressed: ByteArray): ByteArray {
        val bis = ByteArrayInputStream(compressed)
        val bos = ByteArrayOutputStream()
        GZIPInputStream(bis).use { gzip ->
            val buffer = ByteArray(4096)
            var len: Int
            while (gzip.read(buffer).also { len = it } > 0) {
                bos.write(buffer, 0, len)
            }
        }
        return bos.toByteArray()
    }

    fun encryptAesGcm(
        plaintext: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec)
        cipher.updateAAD(aad)
        return cipher.doFinal(plaintext)
    }

    fun decryptAesGcm(
        ciphertextWithTag: ByteArray,
        key: ByteArray,
        nonce: ByteArray,
        aad: ByteArray
    ): ByteArray {
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(key, "AES")
        val spec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, spec)
        cipher.updateAAD(aad)
        return cipher.doFinal(ciphertextWithTag)
    }

    fun zeroOut(vararg arrays: ByteArray?) {
        for (arr in arrays) {
            if (arr != null) {
                Arrays.fill(arr, 0.toByte())
            }
        }
    }
}
