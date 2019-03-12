package ua.ucu.edu.actor

import akka.actor.{Actor, ActorRef, Props}
import org.slf4j.LoggerFactory
import ua.ucu.edu.model.{CriticalState, ReadMeasurement, RespondMeasurement, SensorRecord, Location}
import ua.ucu.edu.device.{IrradianceSensor, SensorGenerator, TemperatureSensor, WindSensor}
import ua.ucu.edu.kafka.{ Producer }


import scala.collection.mutable
import scala.concurrent.duration._
import scala.language.postfixOps

/**
  * Keeps a list of device sensor actors, schedules sensor reads and pushes updates into sensor data topic
  */
class SolarPanelActor(
  val panelId: String,
  val location: Location,
  val producer: Producer
) extends Actor {

  val logger = LoggerFactory.getLogger(getClass)

  logger.debug("Initialized solar panel")

  val deviceToActorRef: mutable.Map[String, ActorRef] = {
    mutable.Map[String, ActorRef](
      "windSensor" -> context.actorOf(Props(new SensorActor(s"$panelId-windSensor", SensorGenerator(WindSensor()))), "windSensor"),
      "irradSensor" -> context.actorOf(Props(new SensorActor(s"$panelId-irradSensor", SensorGenerator(IrradianceSensor()))), "irradSensor"),
      "tempSensor" -> context.actorOf(Props(new SensorActor(s"$panelId-tempSensor", SensorGenerator(TemperatureSensor()))), "tempSensor")
    )
  }

  override def preStart(): Unit = {
    super.preStart()

    // todo - schedule measurement reads
    context.system.scheduler.schedule(5 second, 5 seconds, self, ReadMeasurement)(
      context.dispatcher, self)
  }

  override def receive: Receive = {
    case ReadMeasurement => {
      for ((deviceId, actor) <- deviceToActorRef) {
        actor ! ReadMeasurement
      }
    }
    case  RespondMeasurement(deviceId, sensorType, value) => {
      logger.info(s"Panel actor received record: $deviceId, $sensorType, $value")
      val record = SensorRecord(panelId, location, sensorType, value)
      producer.pushData(record)
    }
    case CriticalState => {
      context.parent ! CriticalState
    }
  }

}
