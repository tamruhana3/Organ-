package com.netforge.app

import com.netforge.app.crypto.Argon2Kdf
import com.netforge.app.data.file.NfgReader
import com.netforge.app.data.file.NfgWriter
import com.netforge.app.domain.model.*
import com.netforge.app.domain.payload.PayloadContext
import com.netforge.app.domain.payload.PayloadEngine
import org.junit.Assert.*
import org.junit.Test

class NetForgeCoreTest {

    @Test
    fun testArgon2KeyDerivation() {
        val salt = ByteArray(16) { 0x42 }
        val key = Argon2Kdf.deriveKey("test-passphrase", salt)
        assertNotNull(key)
        assertEquals(32, key.size)
    }

    @Test
    fun testNfgSealAndUnsealRoundtrip() {
        val original = Profile(
            name = "Test Tunnel",
            author = "Axiom",
            note = "Roundtrip test note",
            host = "gateway.example.com",
            port = 443,
            mode = Mode.Wrapped,
            sshUser = "testuser",
            sshPass = "secretpass",
            sni = "sni.example.com",
            dnsPrimary = "1.1.1.1",
            dnsSecondary = "1.0.0.1",
            mtu = 1400,
            keepalive = 20,
            bannerInfo = BannerInfo(
                enabled = true,
                message = "Welcome to test profile"
            )
        )

        val passphrase = "test-encryption-key-123"
        val sealedBytes = NfgWriter.seal(original, passphrase)
        assertNotNull(sealedBytes)
        assertTrue(sealedBytes.size > 34)

        val unsealed = NfgReader.unseal(sealedBytes, passphrase)
        assertEquals(original.name, unsealed.name)
        assertEquals(original.author, unsealed.author)
        assertEquals(original.host, unsealed.host)
        assertEquals(original.port, unsealed.port)
        assertEquals(original.mode, unsealed.mode)
        assertEquals(original.sshUser, unsealed.sshUser)
        assertEquals(original.sshPass, unsealed.sshPass)
        assertEquals(original.sni, unsealed.sni)
        assertEquals(original.mtu, unsealed.mtu)
        assertEquals(original.bannerInfo.message, unsealed.bannerInfo.message)
    }

    @Test
    fun testPayloadEngineReplacements() {
        val template = "CONNECT [host_port] HTTP/1.1[crlf]Host: [host][crlf][crlf]"
        val context = PayloadContext(
            host = "api.example.com",
            port = 443,
            frontHost = "cdn.example.com"
        )
        val rendered = PayloadEngine.render(template, context)
        assertEquals("CONNECT api.example.com:443 HTTP/1.1\r\nHost: api.example.com\r\n\r\n", rendered)
    }
}
