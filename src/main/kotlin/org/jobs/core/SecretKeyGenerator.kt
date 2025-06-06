package org.jobs.core

import com.github.f4b6a3.uuid.UuidCreator
import com.github.f4b6a3.uuid.util.UuidUtil
import jakarta.enterprise.context.ApplicationScoped
import java.time.Instant
import java.util.UUID

sealed class KeysGenConstants {
    companion object {
        const val RND_API_KEY_LENGTH = 10
        const val RND_SEC_KEY_LENGTH = 20
        const val GUID_LENGTH = 36
        const val NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L
    }
}

interface ISecretKeyGenerator {
    fun genApiKey(): Pair<String,String>
    fun genSecretKey(): Pair<String,String>
    fun getNonce(): String
    fun genAppId(): String
}

@ApplicationScoped
class SecretKeyGenerator: ISecretKeyGenerator {
    override fun genApiKey(): Pair<String,String> {
        // Generate a random UUID
        val myUuid = UUID.randomUUID()
        val myUuidAsString = myUuid.toString()
        println("myUuidAsString: $myUuidAsString")
        val randomStr = randomStringByKotlinRandom(KeysGenConstants.RND_API_KEY_LENGTH)
        println("randomStr: $randomStr")
        val myUuidAndRandomStr = "$myUuidAsString-$randomStr"
        return Pair(myUuidAsString, myUuidAndRandomStr)
    }

    override fun genSecretKey(): Pair<String,String> {
        // Generate a random UUID
        val myUuid = UUID.randomUUID()
        val myUuidAsString = myUuid.toString()
        println("myUuidAsString: $myUuidAsString")
        val randomStr = randomStringByKotlinRandom(KeysGenConstants.RND_SEC_KEY_LENGTH)
        println("randomStr: $randomStr")
        val myUuidAndRandomStr = "$myUuidAsString-$randomStr"

        return Pair(myUuidAsString, myUuidAndRandomStr)
    }

    override fun getNonce(): String {
        val currentTimestamp = Instant.now().toEpochMilli() // UTC

        println("currentTimestamp: $currentTimestamp")
        val sumSign = "$currentTimestamp".toCharArray().map{ it.digitToInt() }.sumOf { it * 7 }

        return "$currentTimestamp-$sumSign"
    }

    override fun genAppId(): String {
        //val uuid7 = UuidCreator.getTimeOrderedEpoch()
        val uuid1 = UuidCreator.getTimeBased()
        //println("UUID Version 1: " + UuidCreator.getTimeBased())
        //println("UUID Version 6: " + UuidCreator.getTimeOrdered())
        //println("UUID Version 7: " + UuidCreator.getTimeOrderedEpoch())
        val sum = "$uuid1".toCharArray().filter { it.isDigit() }.map{ it.digitToInt() }.sumOf { it * 3 }

        //val uuid7 = UuidCreator.getTimeOrderedEpoch()
        val timestamp = UuidUtil.getTimestamp(uuid1)
        println("UUID Version 1 timestamp: $timestamp")
        val randomStr = randomStringByKotlinRandom(KeysGenConstants.RND_API_KEY_LENGTH)
        println("randomStr: $randomStr")

        return "$uuid1+$sum+$randomStr"
    }


}