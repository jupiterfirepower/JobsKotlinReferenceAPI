package org.jobs.core

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyStore
import java.util.UUID
import javax.crypto.spec.SecretKeySpec

class SecretsKeyStore(private val keyStorePath: Path = Paths.get(System.getProperty("user.home"), ".secrets", "key.store"),
                      private val passwordPath: Path = Paths.get(System.getProperty("user.home"), ".secrets", "key.password")) {

    private val keyStoreFile: File
        get() {
            val dirPath = keyStorePath.parent

            if (!dirPath.toFile().exists()) {
                dirPath.toFile().mkdirs()
            }

            return keyStorePath.toFile()
        }

    private val keyStore: KeyStore by lazy {
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())

        if (keyStoreFile.exists()) {
            val stream = FileInputStream(keyStoreFile)
            try {
                keyStore.load(stream, password)
            } catch (ex: IOException) {
                //throw CachedTokenException(keyStorePath)
            }
        } else {
            keyStore.load(null, password)
        }

        keyStore
    }

    private val password: CharArray by lazy {
        val dirPath = passwordPath.parent
        if (!dirPath.toFile().exists()) {
            dirPath.toFile().mkdirs()
        }

        val path = passwordPath.toFile()

        if (path.exists()) {
            path.readText().toCharArray()
        } else {
            val uuid = UUID.randomUUID().toString()
            path.writeText(uuid)
            uuid.toCharArray()
        }
    }

    fun getEntry(entry: String): String? {
        if (!keyStoreFile.exists()) {
            return null
        }

        val protection = KeyStore.PasswordProtection(password)
        val secret = keyStore.getEntry(entry, protection)

        return if (secret != null) {
            String((secret as KeyStore.SecretKeyEntry).secretKey.encoded)
        } else {
            null
        }
    }

    fun setEntry(entry: String, value: String) {
        val protection = KeyStore.PasswordProtection(password)
        val encoded = SecretKeySpec(value.toByteArray(), "AES")

        keyStore.setEntry(entry, KeyStore.SecretKeyEntry(encoded), protection)

        val stream = FileOutputStream(keyStoreFile)
        stream.use {
            keyStore.store(it, password)
        }
    }

    private fun destroy() {
        Files.deleteIfExists(keyStorePath)
        Files.deleteIfExists(passwordPath)
    }
}