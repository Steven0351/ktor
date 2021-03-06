@file:Suppress("RedundantModalityModifier", "FunctionName")

package io.ktor.utils.io.core

import io.ktor.utils.io.bits.*
import io.ktor.utils.io.core.internal.*
import io.ktor.utils.io.pool.*

/**
 * Read-only immutable byte packet. Could be consumed only once however it does support [copy] that doesn't copy every byte
 * but creates a new view instead. Once packet created it should be either completely read (consumed) or released
 * via [release].
 */
public class ByteReadPacket internal constructor(
    head: ChunkBuffer,
    remaining: Long,
    pool: ObjectPool<ChunkBuffer>
) :
    @Suppress("DEPRECATION_ERROR")
    ByteReadPacketPlatformBase(head, remaining, pool),
    Input {
    public constructor(head: ChunkBuffer, pool: ObjectPool<ChunkBuffer>) : this(head, head.remainingAll(), pool)

    @Suppress("DEPRECATION", "UNUSED")
    @Deprecated("Binary compatibility.", level = DeprecationLevel.HIDDEN)
    public constructor(head: IoBuffer, pool: ObjectPool<ChunkBuffer>) : this(head, head.remainingAll(), pool)

    init {
        markNoMoreChunksAvailable()
    }

    /**
     * Returns a copy of the packet. The original packet and the copy could be used concurrently. Both need to be
     * either completely consumed or released via [release]
     */
    public final fun copy(): ByteReadPacket = ByteReadPacket(head.copyAll(), remaining, pool)

    final override fun fill(): ChunkBuffer? = null

    final override fun fill(destination: Memory, offset: Int, length: Int): Int {
        return 0
    }

    final override fun closeSource() {
    }

    override fun toString(): String {
        return "ByteReadPacket($remaining bytes remaining)"
    }

    public companion object {
        public val Empty: ByteReadPacket = ByteReadPacket(ChunkBuffer.Empty, 0L, ChunkBuffer.EmptyPool)

        @DangerousInternalIoApi
        public val ReservedSize: Int
            get() = Buffer.ReservedSize
    }
}

@Suppress("DEPRECATION")
@DangerousInternalIoApi
@Deprecated(
    "Will be removed in future releases.",
    level = DeprecationLevel.ERROR,
    replaceWith = ReplaceWith("AbstractInput", "io.ktor.utils.io.core.AbstractInput")
)
public abstract class ByteReadPacketPlatformBase protected constructor(
    head: ChunkBuffer,
    remaining: Long,
    pool: ObjectPool<ChunkBuffer>
) : ByteReadPacketBase(head, remaining, pool) {
    @Deprecated("Binary compatibility.", level = DeprecationLevel.HIDDEN)
    public constructor(
        head: IoBuffer,
        remaining: Long,
        pool: ObjectPool<ChunkBuffer>
    ) : this(head as ChunkBuffer, remaining, pool)
}

public expect fun ByteReadPacket(
    array: ByteArray,
    offset: Int = 0,
    length: Int = array.size,
    block: (ByteArray) -> Unit
): ByteReadPacket

@Suppress("NOTHING_TO_INLINE")
public inline fun ByteReadPacket(array: ByteArray, offset: Int = 0, length: Int = array.size): ByteReadPacket {
    return ByteReadPacket(array, offset, length) {}
}
