package ua.ucu.edu

import java.util.Properties

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord, RecordMetadata}
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}

import scala.concurrent.{ExecutionContextExecutor, Future, duration}
import scala.language.postfixOps
import scala.util.{Failure, Success}

object Main extends App {

  val logger = LoggerFactory.getLogger(getClass)
  val APIKEY = "235343b13ac90be59884ca36d5325949"

  logger.info("======== Weather Provider App Init ========")

  implicit val system: ActorSystem = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  import duration._

  val BrokerList: String = System.getenv("KAFKA_BROKERS")
  val Topic = "weather_data"
  val props = new Properties()
  props.put("bootstrap.servers", BrokerList)
  props.put("client.id", "weather-provider")
  props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
  props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

  logger.info("initializing producer")

  val producer = new KafkaProducer[String, String](props)

  var curr = 0
  val div = 20

  system.scheduler.schedule(5 seconds, 1 seconds, new Runnable {
    override def run(): Unit = {
      logger.debug("weather request")

      curr = curr%div + 1
      val lat = curr
      val long = curr

      val responseFuture: Future[HttpResponse] = Http().singleRequest(
        HttpRequest(uri = s"https://api.openweathermap.org/data/2.5/weather?lat=$lat&lon=$long&&APPID=$APIKEY"))

      responseFuture
        .onComplete {
          case Success(res:HttpResponse) => {
            val message = lat + ";" + long + ";" + extract_data(res)
            val data = new ProducerRecord[String, String](Topic, message)
            producer.send(data, (metadata: RecordMetadata, exception: Exception) => {
              logger.trace(metadata.toString, exception)
            })
          }
          case Failure(_)   => sys.error("something wrong")
        }
    }
  })

  def extract_data(response: HttpResponse):String = {
    val str = response.entity.toString
    val temp_base = str.indexOf("temp")
    val temp_begin = str.indexOf(":", temp_base) + 1
    val temp_end = str.indexOf(",", temp_begin)
    val temp = str.substring(temp_begin, temp_end)

    val hum_base = str.indexOf("humidity")
    val hum_begin = str.indexOf(":", hum_base) + 1
    val hum_end = str.indexOf(",", hum_begin)
    val hum = str.substring(hum_begin, hum_end)

    temp + ";" + hum
  }
}
