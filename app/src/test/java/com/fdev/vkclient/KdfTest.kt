package com.fdev.vkclient

import com.fdev.vkclient.crypto.bytesToHex
import com.fdev.vkclient.crypto.cipher.Pbkdf2HmacSha1
import junit.framework.Assert.assertEquals
import org.junit.Test


class KdfTest {

    @Test
    fun kdf_sameHash() {
        val hash1 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        val hash2 = Pbkdf2HmacSha1.deriveFromKey(USER_KEY)
        assertEquals(bytesToHex(hash1), bytesToHex(hash2))
    }

    companion object {

        const val USER_KEY = "someUserKey"
    }
}