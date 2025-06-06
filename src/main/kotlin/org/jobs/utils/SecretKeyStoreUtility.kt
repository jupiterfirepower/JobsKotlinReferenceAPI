package org.jobs.utils

import jakarta.enterprise.context.ApplicationScoped
import org.jobs.core.ISecretKeyGenerator
import org.jobs.core.SecretsKeyStore
import org.jobs.core.ValResult
import org.jobs.core.decrypt
import org.jobs.core.encrypt
import org.jobs.core.generateIV
import org.jobs.core.generateSecretKey
import org.jobs.core.getBase64IvParameterSpec
import org.jobs.core.getBase64SecretKey
import org.jobs.core.getIvParameterSpecFromBase64
import org.jobs.core.getSecretKeyFromBase64

interface ISecretKeyStoreUtility {
    fun genAppIdCrypto()
    fun genGuidCrypto()
}

@ApplicationScoped
class SecretKeyStoreUtility(val generator: ISecretKeyGenerator) : ISecretKeyStoreUtility {
    override fun genAppIdCrypto() {
        val genAppId = generator.genAppId()
        println("${::SecretKeyStoreUtility.name} *genAppId: $genAppId")

        val keyStore = SecretsKeyStore()

        val secretKey = getSecretKeyFromBase64(keyStore.getEntry("secretKey").toString())
        val ivParameter = getIvParameterSpecFromBase64(keyStore.getEntry("ivParameter").toString())

        val encryptedAppId = encrypt(genAppId, secretKey, ivParameter)
        val decryptedAppId = decrypt(encryptedAppId, secretKey, ivParameter)

        println("${::SecretKeyStoreUtility.name} *AppIdEncrypted: $encryptedAppId")
        println("${::SecretKeyStoreUtility.name} *AppIdDecrypted: $decryptedAppId")

        val listValues = genAppId.split("+")

        keyStore.setEntry("ReferenceAppId", "${listValues[0]}+${listValues[1]}")
    }

    override fun genGuidCrypto() {
        val genApiKeyPair = generator.genApiKey()
        val genApiKey = genApiKeyPair.first
        println("*genApiKey: $genApiKey")

        val secretKey = generateSecretKey()
        val ivParameter = generateIV()
        val secretKeyBase64 = getBase64SecretKey(secretKey)
        val ivParameterBase64 = getBase64IvParameterSpec(ivParameter)

        val keyStore = SecretsKeyStore()
        keyStore.setEntry("secretKey", secretKeyBase64)
        keyStore.setEntry("ivParameter", ivParameterBase64)

        keyStore.setEntry("ReferenceApiKey", genApiKey)

        val encrypted = encrypt(genApiKeyPair.second, secretKey, ivParameter)
        val decrypted = decrypt(encrypted, secretKey, ivParameter)

        println("*secretKeyBase64: $secretKeyBase64")
        println("*ivParameterBase64: $ivParameterBase64")
        println("*ApiKeyEncrypted: $encrypted")
        println("*ApiKeyDecrypted: $decrypted")

        val genSecKeyPair = generator.genSecretKey()

        val genSecKey = genSecKeyPair.first
        println("*genSecKey: $genSecKey")
        val genSecKeySecond = genSecKeyPair.second

        val encryptedSecKey = encrypt(genSecKeySecond, secretKey, ivParameter)
        val decryptedSecKey = decrypt(encryptedSecKey, secretKey, ivParameter)

        println("*SecKeyEncrypted: $encryptedSecKey")
        println("*SecKeyDecrypted: $decryptedSecKey")

        keyStore.setEntry("ReferenceSecKey", genSecKey)

        val genNonce = generator.getNonce()
        println("*Nonce: $genNonce")

        val encryptedNonce = encrypt(genNonce, secretKey, ivParameter)
        val decryptedNonce = decrypt(encryptedSecKey, secretKey, ivParameter)

        println("*NonceEncrypted: $encryptedNonce")
        println("*NonceDecrypted: $decryptedNonce")

        val genAppId = generator.genAppId()
        println("*genAppId: $genAppId")

        val encryptedAppId = encrypt(genAppId, secretKey, ivParameter)
        val decryptedAppId = decrypt(encryptedAppId, secretKey, ivParameter)

        println("*AppIdEncrypted: $encryptedAppId")
        println("*AppIdDecrypted: $decryptedAppId")

        val listValues = genAppId.split("+")

        keyStore.setEntry("ReferenceAppId", "${listValues[0]}+${listValues[1]}")
    }
}