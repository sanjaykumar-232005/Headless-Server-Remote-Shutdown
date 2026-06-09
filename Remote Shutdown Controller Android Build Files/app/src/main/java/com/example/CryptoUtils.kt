package com.example

import java.security.MessageDigest

object CryptoUtils {
    /**
     * Computes the SHA-256 message digest of the given input string.
     */
    fun hashSha256(input: String): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(input.toByteArray(Charsets.UTF_8))
    }

    /**
     * Converts a ByteArray to its lowercase hexadecimal string representation.
     */
    fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
