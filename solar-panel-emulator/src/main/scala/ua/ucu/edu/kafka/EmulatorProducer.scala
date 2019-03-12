package ua.ucu.edu.kafka

import java.util.Properties
import java.util.concurrent.Future

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.slf4j.LoggerFactory
import ua.ucu.edu.model.SensorRecord

trait Producer {
  def pushData(record: SensorRecord): Future[RecordMetadata]
}

object EmulatorProducer extends Producer {

  val logger = LoggerFactory.getLogger(getClass)

  val topic = "sensor-data"
  val props = Config.get_kafka_properties()

  logger.info("initializing producer")
  val producer = new KafkaProducer[String, String](props)

  def pushData(record: SensorRecord): Future[RecordMetadata] = {

    val msg = record.stingify()
    val data = new ProducerRecord[String, String](topic, msg)

    producer.send(data, (metadata: RecordMetadata, exception: Exception) => {
      logger.trace(metadata.toString, exception)
    })
  }

  object Config {
    def get_kafka_properties():Properties = {
      val props = new Properties()
      props.put("bootstrap.servers", System.getenv("KAFKA_BROKERS"))
      props.put("client.id", "solar-panel-emulator")
      props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
      props
    }
  }
}
