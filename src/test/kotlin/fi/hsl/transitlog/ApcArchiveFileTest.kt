package fi.hsl.transitlog

import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Base64
import java.util.zip.CRC32
import kotlin.test.Test
import kotlin.test.assertEquals

class ApcArchiveFileTest {
    @field:TempDir
    lateinit var tempDir: Path

    @Test
    fun `Test ApcArchiveFileDescriptorFactory creates descriptor with correct start and end times`() {
        val apcFileDescriptorFactory = ApcArchiveFile.ApcFileDescriptorFactory(tempDir, Duration.ofMinutes(15))
        
        val timestamp = ZonedDateTime.of(2025, 1, 1, 12, 37, 0, 0, ZoneId.of("Europe/Helsinki")).toInstant()

        val apcFileDescriptor = apcFileDescriptorFactory.createApcFileDescriptor(timestamp.toEpochMilli())

        assertEquals(
            ZonedDateTime.of(2025, 1, 1, 12, 30, 0, 0, ZoneId.of("Europe/Helsinki")).toInstant(),
            apcFileDescriptor.contentStart
        )
        assertEquals(
            ZonedDateTime.of(2025, 1, 1, 12, 45, 0, 0, ZoneId.of("Europe/Helsinki")).toInstant(),
            apcFileDescriptor.contentEnd
        )
    }


    @Test
    fun `metadata contains base64 encoded CRC32 of parquet file`() {
        val descriptor = ApcArchiveFile.ApcFileDescriptorFactory(tempDir, Duration.ofMinutes(15))
            .createApcFileDescriptor(Instant.parse("2025-01-01T12:37:00Z").toEpochMilli())
        val archiveFile = ApcArchiveFile(descriptor, fastUpload = false)
        archiveFile.close()

        val crc = CRC32().apply { update(Files.readAllBytes(archiveFile.path)) }.value
        val expectedCrc = byteArrayOf(
            (crc ushr 24).toByte(),
            (crc ushr 16).toByte(),
            (crc ushr 8).toByte(),
            crc.toByte()
        )

        assertEquals(Base64.getEncoder().encodeToString(expectedCrc), archiveFile.getMetadata()["parquet_crc"])
    }
}