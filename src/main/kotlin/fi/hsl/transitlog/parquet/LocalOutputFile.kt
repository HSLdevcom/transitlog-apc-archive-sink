package fi.hsl.transitlog.parquet

import org.apache.parquet.io.OutputFile
import org.apache.parquet.io.PositionOutputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Path

class LocalOutputFile(private val path: Path) : OutputFile {
    override fun create(blockSizeHint: Long): PositionOutputStream =
        LocalPositionOutputStream(Files.newOutputStream(path))

    override fun createOrOverwrite(blockSizeHint: Long): PositionOutputStream =
        LocalPositionOutputStream(Files.newOutputStream(path))

    override fun supportsBlockSize(): Boolean = false

    override fun defaultBlockSize(): Long = 0L
}

private class LocalPositionOutputStream(
    private val outputStream: OutputStream
) : PositionOutputStream() {
    private var position = 0L

    override fun write(b: Int) {
        outputStream.write(b)
        position++
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        outputStream.write(b, off, len)
        position += len
    }

    override fun getPos(): Long = position

    override fun flush() {
        outputStream.flush()
    }

    override fun close() {
        outputStream.close()
    }
}