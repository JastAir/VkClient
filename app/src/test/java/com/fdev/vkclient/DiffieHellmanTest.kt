package com.fdev.vkclient

import com.fdev.vkclient.crypto.dh.DhData
import com.fdev.vkclient.crypto.dh.DiffieHellman
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.math.BigInteger

class DiffieHellmanTest {

    @Test
    fun exchange_invalidateSharedKey() {
        val dhA = DiffieHellman(PRIME)
        val dhData = dhA.getDhData()

        val dhB = DiffieHellman(dhData)
        dhA.publicOther = dhB.publicOwn

        assertEquals(dhA.key.toString(), dhB.key.toString())
    }

    @Test
    fun dhData_serializing() {
        val dh = DiffieHellman(PRIME)
        val dhData = dh.getDhData()
        val serialized = DhData.serialize(dhData)
        val deserialized = DhData.deserialize(serialized)
        assertEquals(dhData, deserialized)
    }

    companion object {
        private val PRIME = BigInteger("429960845873088536599738146849398890197656281978746052260302" +
                "647466290912363305996665498753182126120318295110244403964643426486915779918338905631403" +
                "871028184935255084712703423613529366297760543627384218281702559557613931230981025214701" +
                "436292843374882547050410898749274017561380961864641986822934974094546625703373934105581" +
                "804151000069136171694933799628596746360744089987859632744266134003621159571422862980144" +
                "043377717793249604329463406248840895370685965329721512935512704815510998868368597285047" +
                "022910731679048010756286396108128504010975404344672924191827643103234869173733652744217" +
                "51734483875743936086632531541480503")
    }
}