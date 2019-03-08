package ua.ucu.edu.device

import scala.util.Random

case class SensorGenerator(sensor: Sensor) extends SensorApi {

  def randBetween(start: Double, end: Double): Double = {
    start + (end - start)*Random.nextDouble
  }

  override def readCurrentValue: Double = {
    randBetween(sensor.min, sensor.max)
  }
}
