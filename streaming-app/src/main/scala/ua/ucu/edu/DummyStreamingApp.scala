package ua.ucu.edu

import java.util.Properties
import java.util.concurrent.TimeUnit

import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala._
import org.apache.kafka.streams.{KafkaStreams, StreamsConfig}
import org.slf4j.LoggerFactory
import Serdes._
import kafka.server.QuotaType.Produce
import org.apache.kafka.streams.kstream.{Joined, Produced}
import org.apache.kafka.streams.scala.kstream.{KStream, KTable}

case class Location(longitude: Double, latitude: Double)
// Solar data
case class SolarData(panelId: Int, location: Location, sensorType: String, measurement: Double)
// Weather data
case class WeatherData(location: Location, temperature: Double, humidity: Double)

// dummy app for testing purposes
object DummyStreamingApp extends App {
  def toWeather(str: String): WeatherData = {
    try {
      val Array(latitude: String, longitude: String, temperature: String ,humidity: String) = str.split(";");
      return WeatherData(Location(longitude.toFloat, latitude.toFloat), temperature.toFloat, humidity.toFloat)
    } catch {
      case _ => null
    }
  }
  def toSolar(str: String): SolarData = {
    try {
      val Array(panelid: String, sensorType: String, measurement: String, latitude: String, longitude: String) = str.split(";");
      return SolarData(panelid.toInt, Location(longitude.toFloat, latitude.toFloat), sensorType, measurement.toDouble)
    } catch {
      case _ => null
    }
  }

  val logger = LoggerFactory.getLogger(getClass)

  val props = new Properties()
  props.put(StreamsConfig.APPLICATION_ID_CONFIG, "streaming_app")
  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, System.getenv(Config.KafkaBrokers))
  val builder = new StreamsBuilder

  val weatherTable: KTable[String, String] = builder
    .stream[String, String]("weather_data")
    .groupBy((_, weather) => {
      print("weather -> ")
      print(weather)
      val weatherData = toWeather(weather)
      s"${weatherData.location.latitude},${weatherData.location.longitude}"
    })
    .reduce((_, prev) => prev)

  val solarPanelStream: KStream[String, String] = builder
    .stream[String, String]("sensor-data")
    .map((_, solar) => {
      print("solar -> ")
      print(solar)
      val solarData = toSolar(solar)
      (s"${solarData.location.latitude},${solarData.location.longitude}", solar)
    })

  val resultsStream: KStream[String, String] = solarPanelStream.join(weatherTable)((solarStr, weatherStr) => {
    val weatherData = toWeather(weatherStr)
    val solarData = toSolar(solarStr)
    val result = s"weather=${weatherData}, solar=${solarData}"
    logger.info(s"message to result stream -> ${result}")
    result
  })(Joined.`with`(
    Serdes.String,
    Serdes.String,
    Serdes.String,
  ))

  resultsStream.to("result_data")

  val streams = new KafkaStreams(builder.build(), props)
  streams.cleanUp()
  streams.start()

  sys.addShutdownHook {
    streams.close(10, TimeUnit.SECONDS)
  }

  object Config {
    val KafkaBrokers = "KAFKA_BROKERS"
  }
}
