package org.jobs.core

import arrow.core.Either
import com.github.f4b6a3.uuid.enums.UuidVersion
import com.github.f4b6a3.uuid.util.UuidUtil
import jakarta.enterprise.context.ApplicationScoped
import java.time.Duration
import java.time.Instant
import java.util.UUID
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

data class ValResult(val result: Boolean, val apiKeyValid: Boolean, val secKeyValid: Boolean, val nonceValid: Boolean, val appIdValid: Boolean)

interface ISecurityHelper {
    fun isNullOrEmpty(apiKey: String?, secKey: String?, nonce: String?, appId: String? ): Boolean
    fun isHeadersValid(apiKey: String, secKey: String, nonce: String, appId: String, devMode: Boolean = false ): ValResult
}

@ApplicationScoped
class SecurityHelper: ISecurityHelper {

    val String.Companion.empty: String
        get() = ""

    override fun isNullOrEmpty(apiKey: String?, secKey: String?, nonce: String?, appId: String? ): Boolean {
            return apiKey.isNullOrEmpty() || secKey.isNullOrEmpty() ||
                    nonce.isNullOrEmpty() || appId.isNullOrEmpty()
    }

    fun isApiKeyValid(apiKey: String, secretKey: SecretKey, ivParameter: IvParameterSpec): Either<Exception, Pair<Boolean, String>> {
        val keyStore = SecretsKeyStore()

        try{
            val decryptedApiKey = decrypt(apiKey, secretKey, ivParameter)
            val validApiKey = isValidApiKey(decryptedApiKey,keyStore.getEntry("ReferenceApiKey").toString())

            return Either.Right(Pair(validApiKey,decryptedApiKey))
        }
        catch(e: Exception) {
            println("isApiKeyValidFun ${e.message}")
            return Either.Left(e)
        }
    }

    private fun isApiKeyFormatValid(apiKey: String): Pair<Boolean, String> {
        val currentApiKey = apiKey.substring(0, KeysGenConstants.GUID_LENGTH)
        println("${::isApiKeyFormatValid.name} currentApiKey: $currentApiKey")
        val randomStr = apiKey.substring(37,47)
        println("${::isApiKeyFormatValid.name} randomStr: $randomStr")

        val digit = randomStr.any { it.isDigit() }
        val letter = randomStr.any { it.isLetter() }
        val parsedApiKey = UUID.fromString(currentApiKey)
        println("${::isApiKeyFormatValid.name} parsedApiKey: $parsedApiKey")

        return Pair(parsedApiKey != null && letter, currentApiKey)
    }

    private fun isValidApiKey(sessionApiKey: String, realApiKey: String): Boolean {
        val validFormatResult = isApiKeyFormatValid(sessionApiKey)
        return validFormatResult.first && realApiKey == validFormatResult.second
    }

    private fun isSecretKeyFormatValid(secKey: String): Pair<Boolean, String> {
        val currentSecKey = secKey.substring(0, KeysGenConstants.GUID_LENGTH)
        println("${::isSecretKeyFormatValid.name} currentSecKey: $currentSecKey")
        val randomStr = secKey.substring(KeysGenConstants.GUID_LENGTH + 1, KeysGenConstants.GUID_LENGTH + 1 + KeysGenConstants.RND_SEC_KEY_LENGTH)
        println("${::isSecretKeyFormatValid.name} randomStr: $randomStr")

        val digit = randomStr.any { it.isDigit() }
        val letter = randomStr.any { it.isLetter() }
        val parsedSecKey = UUID.fromString(currentSecKey)
        println("${::isSecretKeyFormatValid.name} parsedSecKey: $parsedSecKey")

        return Pair(parsedSecKey != null && letter, currentSecKey)
    }

    private fun isValidSecretKey(sessionSecKey: String, realSecKey: String): Boolean {
        val validFormatResult = isSecretKeyFormatValid(sessionSecKey)
        return validFormatResult.first && realSecKey == validFormatResult.second
    }

    fun isSecKeyValid(secKey: String, secretKey: SecretKey, ivParameter: IvParameterSpec): Either<Exception, Pair<Boolean, String>> {
        val keyStore = SecretsKeyStore()

        try{
            val decryptedSecKey = decrypt(secKey, secretKey, ivParameter)
            val validSecKey = isValidSecretKey(decryptedSecKey,keyStore.getEntry("ReferenceSecKey").toString())
            return Either.Right(Pair(validSecKey,decryptedSecKey))
        }
        catch(ex: Exception) {
            return Either.Left(ex)
        }
    }

    private fun isValidNonce(nonce: String): Boolean {
        println("${::isValidNonce.name} nonce: $nonce")
        val listValues = nonce.split("-")
        val currentNonce = listValues.first()
        println("${::isValidNonce.name} currentNonce: $currentNonce")
        val sumSign = listValues.last()
        println("${::isValidNonce.name} sumSign: $sumSign")

        val digitNonce = currentNonce.all { it.isDigit() }
        println("${::isValidNonce.name} digitNonce: $digitNonce")
        val digitSumSign = sumSign.all { it.isDigit() }
        println("${::isValidNonce.name} digitSumSign: $digitSumSign")

        val diff = (Instant.now().toEpochMilli() - currentNonce.toLong()) / 1000L
        println("${::isValidNonce.name} diff: $diff")

        val backToDate = Instant.ofEpochMilli(currentNonce.toLong())

        println("${::isValidNonce.name} backToDate: $backToDate")
        return digitNonce && digitSumSign && diff >= 0 && diff <= 5
    }

    fun isNonceValid(nonce: String, secretKey: SecretKey, ivParameter: IvParameterSpec): Either<Exception, Pair<Boolean, String>> {
        try{
            val decryptedNonce = decrypt(nonce, secretKey, ivParameter)
            val nonceValid = isValidNonce(decryptedNonce)
            return Either.Right(Pair(nonceValid, decryptedNonce))
        }
        catch(ex: Exception) {
            return Either.Left(ex)
        }
    }

    private fun isAppIdFormatValid(appId: String): Boolean {
        println("${::isAppIdFormatValid.name} appId: $appId")
        val listValues = appId.split("+")
        val currentAppId = listValues[0]
        println("${::isAppIdFormatValid.name} currentAppId: $currentAppId")
        val sumSign = listValues[1]
        println("${::isAppIdFormatValid.name} sumSign: $sumSign")

        val digitSumSign = sumSign.all { it.isDigit() }
        println("${::isAppIdFormatValid.name} digitSumSign: $digitSumSign")

        // creating UUID 1/6/7 version
        val appIdx = UUID.fromString(currentAppId)
        val version = UuidUtil.getVersion(appIdx)

        val timestamp = when(version) {
            UuidVersion.VERSION_TIME_BASED -> appIdx.timestamp() // UUID Version 1
            UuidVersion.VERSION_TIME_ORDERED -> UuidUtil.getTimestamp(appIdx) // UUID Version 6
            UuidVersion.VERSION_TIME_ORDERED_EPOCH -> UuidUtil.getTimestamp(appIdx) // UUID Version 7
            else -> 0
        }

        println("${::isAppIdFormatValid.name} appIdx: $appIdx")
        //val timestamp = appIdx.timestamp()
        println("${::isAppIdFormatValid.name} appIdx.timestamp(): $timestamp")

        val time = (timestamp - KeysGenConstants.NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000L
        val backToDate = Instant.ofEpochMilli(time)
        println("${::isAppIdFormatValid.name} backToDate: $backToDate")
        println("${::isAppIdFormatValid.name} Instant.now: ${Instant.now()}")

        val res = Duration.between(backToDate, Instant.now() )
        println("${::isAppIdFormatValid.name} diff:$res")
        println("${::isAppIdFormatValid.name} diff Hours:${res.toHours()}")

        return appIdx != null && res.toHours() < 24
    }

    private fun isValidAppId(sessionAppId: String, realAppId: String): Boolean {
        val listValues = sessionAppId.split("+")
        return isAppIdFormatValid(sessionAppId) && realAppId == "${listValues[0]}+${listValues[1]}"
    }

    fun isAppIdValid(appId: String, secretKey: SecretKey, ivParameter: IvParameterSpec): Either<Exception, Pair<Boolean, String>> {
        val keyStore = SecretsKeyStore()

        try{
            val decryptedAppId = decrypt(appId, secretKey, ivParameter)
            val appIdValid = isValidAppId(decryptedAppId, keyStore.getEntry("ReferenceAppId").toString())
            return Either.Right(Pair(appIdValid, decryptedAppId))
        }
        catch(ex: Exception) {
            return Either.Left(ex)
        }
    }

    private fun checkResult(either: Either<Exception, Pair<Boolean, String>>, entityName: String): Boolean
    {
        var validKey = false
        when(either) {
            is Either.Left -> println("${::isHeadersValid.name} exception: ${either.value.message}")
            is Either.Right -> {
                val result = either.value
                validKey = result.first
                println("${::isHeadersValid.name} valid: ${validKey}, $entityName Decrypted: ${result.second}")
            }
        }
        return validKey
    }

    override fun isHeadersValid(apiKey: String, secKey: String, nonce: String, appId: String, devMode: Boolean ): ValResult {
        var validApiKey = false
        var validSecKey = false
        var validNonce = false
        var validAppId = false

        try {
            val keyStore = SecretsKeyStore()

            val secretKey = getSecretKeyFromBase64(keyStore.getEntry("secretKey").toString())
            val ivParameter = getIvParameterSpecFromBase64(keyStore.getEntry("ivParameter").toString())

            val resultApiKey = isApiKeyValid(apiKey, secretKey, ivParameter)
            validApiKey = checkResult(resultApiKey, "apiKey")

            val resultSecKey = isSecKeyValid(secKey, secretKey, ivParameter)
            validSecKey = checkResult(resultSecKey, "secKey")

            val resultNonce = isNonceValid(nonce, secretKey, ivParameter)
            validNonce = checkResult(resultNonce, "nonce")

            val resultAppId = isAppIdValid(appId, secretKey, ivParameter)
            validAppId = checkResult(resultAppId, "appId")

            val result = if(!devMode) validApiKey && validSecKey && validNonce && validAppId
                         else validApiKey && validSecKey && validAppId

            return ValResult(result,validApiKey, validSecKey,
                validNonce, validAppId )
        }
        catch(_: Exception) {
            return ValResult(result = false,
                apiKeyValid = validApiKey,
                secKeyValid = validSecKey,
                nonceValid = validNonce,
                appIdValid = validAppId
            )
        }
    }
}



