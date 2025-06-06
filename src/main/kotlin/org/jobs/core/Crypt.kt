package org.jobs.core

import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import kotlin.random.Random

sealed class CryptConstants {
    companion object {
        const val AES_ALGO = "AES"
        const val AES_TRANSFORMATION = "AES/CBC/PKCS5PADDING"
    }
}

fun generateSecretKey(): SecretKey {
    val keyGenerator = KeyGenerator.getInstance(CryptConstants.AES_ALGO)
    keyGenerator.init(256) // You can also use 128 or 192 bits
    return keyGenerator.generateKey()
}

fun generateIV(): IvParameterSpec {
    val iv = ByteArray(16)
    val secureRandom = SecureRandom()
    secureRandom.nextBytes(iv)
    return IvParameterSpec(iv)
}

fun getBase64SecretKey(key: SecretKey): String {
    // get base64 encoded version of the key
    return Base64.getEncoder().encodeToString(key.encoded)
}

fun getSecretKeyFromBase64(encodedSecretKey: String): SecretKey {
    // decode the base64 encoded string
    val decodedKey = Base64.getDecoder().decode(encodedSecretKey)
    // rebuild key using SecretKeySpec
    return SecretKeySpec(decodedKey, 0, decodedKey.size, CryptConstants.AES_ALGO)
}

fun getBase64IvParameterSpec(iv: IvParameterSpec): String {
    // get base64 encoded version of the key
    return Base64.getEncoder().encodeToString(iv.iv)
}

fun getIvParameterSpecFromBase64(ivParameter: String): IvParameterSpec {
    val decodedIv = Base64.getDecoder().decode(ivParameter)
    // get base64 encoded version of the key
    return IvParameterSpec(decodedIv)
}

fun decrypt(cipherText: String, key: SecretKey, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(CryptConstants.AES_TRANSFORMATION)
    cipher.init(Cipher.DECRYPT_MODE, key, iv)
    val plainText = cipher.doFinal(Base64.getDecoder().decode(cipherText))
    return String(plainText)
}

fun encrypt(inputText: String, key: SecretKey, iv: IvParameterSpec): String {
    val cipher = Cipher.getInstance(CryptConstants.AES_TRANSFORMATION)
    cipher.init(Cipher.ENCRYPT_MODE, key, iv)
    val cipherText = cipher.doFinal(inputText.toByteArray())
    return Base64.getEncoder().encodeToString(cipherText)
}

internal val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomStringByKotlinRandom(length:Int) = (1..length)
    .map { Random.nextInt(0, charPool.size).let { charPool[it] } }
    .joinToString("")

/*val inputText = "abcdefghigklmnopqrstuvwxyz0123456789"
val key = SecretKeySpec("1234567890123456".toByteArray(), "AES")
val iv = IvParameterSpec(ByteArray(16))

val cipherText = encrypt(inputText, key, iv)
val plainText = decrypt(cipherText, key, iv)*/

