package com.crop.recruital

import org.junit.Assert.assertFalse
import org.junit.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


internal class SuperMapTest {

    private val THREAD_POOL_SIZE = 5

    @Test
    fun testSimple() {
        val superMap = SuperMap<String, String>()
        superMap.set("h", "Hello")
        superMap.set("w", "World")
        println("${superMap.get("h")} ${superMap.get("w")}")
    }

    @Test
    @Throws(Exception::class)
    fun testThreadSafe() {
        val map = SuperMap<String, Int>()
        val sumList = parallelSum100(map, 100)
        val wrongResultCount = sumList
            .stream()
            .filter { num: Int? -> num != 100 }
            .count()

        // if the map is thread safe, all sumList items should be 100
        if (wrongResultCount > 0) {
            print("$wrongResultCount times are failed.")
        }
        assertFalse(wrongResultCount > 0)
    }

    @Test
    @Throws(Exception::class)
    fun testFixedThreadSafe() {
        val map = ThreadSafeSuperMap<String, Int>()
        val sumList = parallelSum100(map, 100)
        val wrongResultCount = sumList
            .stream()
            .filter { num: Int? -> num != 100 }
            .count()

        // if the map is thread safe, all sumList items should be 100
        if (wrongResultCount > 0) {
            print("$wrongResultCount times are failed.")
        }
        assertFalse(wrongResultCount > 0)
    }

    @Throws(InterruptedException::class)
    private fun parallelSum100(
        map: SuperMap<String, Int>,
        executionTimes: Int
    ): List<Int?> {
        val sumList: MutableList<Int?> = ArrayList(1000)
        for (i in 0 until executionTimes) {
            map.set("test", 0)
            val exServer = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
            for (j in 0..9) {
                exServer.execute {
                    for (i in 0..9) {
                        map.set("test") { _, value -> (value ?: 0) + 1 }
                    }
                }
            }
            exServer.shutdown()
            exServer.awaitTermination(5, TimeUnit.SECONDS)
            sumList.add(map.get("test"))
        }
        return sumList
    }
}