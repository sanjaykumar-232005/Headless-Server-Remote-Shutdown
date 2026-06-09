package com.example

import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testSha256Hashing() {
    val input = "HelloWorld"
    val hashBytes = CryptoUtils.hashSha256(input)
    assertEquals(32, hashBytes.size)

    // Convert bytes to hex format to match the standard signature of "HelloWorld"
    val hexString = CryptoUtils.bytesToHex(hashBytes)
    assertEquals("872e4e50ce9990d8b041330c47c9ddd11bec6b503ae9386a99da8584e9bb12c4", hexString)
  }
}
