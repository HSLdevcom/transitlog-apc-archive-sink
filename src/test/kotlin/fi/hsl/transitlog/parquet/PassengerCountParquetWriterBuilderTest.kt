package fi.hsl.transitlog.parquet

import fi.hsl.common.passengercount.proto.PassengerCount
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
import java.nio.file.Path
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertTrue

class PassengerCountParquetWriterBuilderTest {
    @field:TempDir
    lateinit var tempDir: Path

    private val testData = PassengerCount.Data.newBuilder()
        .setSchemaVersion(1)
        .setReceivedAt(Instant.now().toEpochMilli())
        .setTopic("/hfp/v2/journey/ongoing/apc/bus/0017/00015")
        .setPayload(PassengerCount.Payload.newBuilder()
            .setVeh(6)
            .setDesi("550")
            .setDir("1")
            .setJrn(6)
            .setLat(60.463)
            .setLoc("GPS")
            .setLine(4)
            .setLong(24.626)
            .setOday("2023-03-14")
            .setOdo(77743.0)
            .setOper(9)
            .setRoute("2550")
            .setStart("16:00")
            .setTsi(Instant.now().epochSecond)
            .setTst(Instant.now().toEpochMilli())
            .setStop(77457)
            .setVehicleCounts(PassengerCount.VehicleCounts.newBuilder()
                .setCountQuality("normal")
                .setVehicleLoad(54)
                .setVehicleLoadRatio(0.785)
                .addDoorCounts(PassengerCount.DoorCount.newBuilder()
                    .setDoor("door1")
                    .addCount(PassengerCount.Count.newBuilder()
                        .setClazz("adult")
                        .setIn(4)
                        .setOut(5)
                    )
                )
                .addDoorCounts(PassengerCount.DoorCount.newBuilder()
                    .setDoor("door2")
                    .addCount(PassengerCount.Count.newBuilder()
                        .setClazz("adult")
                        .setIn(0)
                        .setOut(15)
                    )
                )
            )
        )
        .build()

    private val testDataWithoutDoorCounts = testData.toBuilder().setPayload(testData.payload.toBuilder().setVehicleCounts(testData.payload.vehicleCounts.toBuilder().clearDoorCounts())).build()

    private fun testWritingData(passengerCount: PassengerCount.Data) {
        val file = tempDir.resolve("test.parquet")

        val passengerCountParquetWriter = PassengerCountParquetWriterBuilder(file).withCompressionCodec(CompressionCodecName.ZSTD).build()

        passengerCountParquetWriter.write(passengerCount)
        passengerCountParquetWriter.close()

        assertTrue { Files.size(file) > 0 }
    }

    @Test
    fun `Test writing passenger count data to Parquet file`() {
        testWritingData(testData)
    }

    @Test
    fun `Test writing without door counts`() {
        testWritingData(testDataWithoutDoorCounts)
    }
}