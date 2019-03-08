package ua.ucu.edu.actor

import akka.actor.Actor
import ua.ucu.edu.device.{SensorApi, SensorGenerator}
import ua.ucu.edu.model.{ReadMeasurement, RespondMeasurement}

import scala.language.postfixOps

class SensorActor(
  val deviceId: String,
  api: SensorApi
) extends Actor {

  override def receive: Receive = {
    case ReadMeasurement => {
      sender() ! RespondMeasurement(deviceId, api.sensor.sensorType, api.readCurrentValue)
    }
  }
}
