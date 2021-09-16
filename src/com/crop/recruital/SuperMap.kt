package com.crop.recruital

import java.util.concurrent.locks.ReadWriteLock
import java.util.concurrent.locks.ReentrantReadWriteLock
import java.util.function.BiFunction


open class SuperMap<K, V> {
    private val map: MutableMap<K, V> = mutableMapOf()
    open fun get(key: K) = map.get(key)
    open fun set(key: K, value: V) = map.set(key, value)
    open fun remove(key: K) = map.remove(key)

    open fun set(key: K, block: BiFunction<in K?, in V?, out V?>): V? {
        val value = block.apply(key, get(key))
        if (value != null) {
            set(key, value)
        } else {
            remove(key)
        }
        return value
    }
}

class ThreadSafeSuperMap<K, V>: SuperMap<K, V>() {
    private val map: MutableMap<K, V> = mutableMapOf()
    var lock: ReadWriteLock = ReentrantReadWriteLock()
    var writeLock = lock.writeLock()

    override fun get(key: K): V? {
        return map[key]
    }

    override fun set(key: K, value: V) {
        writeLock.lock()
        try {
            map[key] = value
        }
        finally {
            writeLock.unlock()
        }
    }

    override fun remove(key: K): V? {
        writeLock.lock()
        try {
            return map.remove(key)
        }
        finally {
            writeLock.unlock()
        }
    }

    override fun set(key: K, block: BiFunction<in K?, in V?, out V?>): V? {
        writeLock.lock()
        try {
            val value = block.apply(key, get(key))
            if (value != null) {
                map[key] = value
            } else {
                remove(key)
            }
            return value
        }
        finally {
            writeLock.unlock()
        }
    }

}
