package ua.ucu.edu.device

trait Sensor {
  def sensorType: String
  def max: Double
  def min: Double
}

case class WindSensor (sensorType: String = "Wind", max: Double = 200.0, min: Double = 0.0) extends Sensor
case class IrradianceSensor (sensorType: String = "Irradiance", max: Double = 1700.0, min: Double = 0.0) extends Sensor
case class TemperatureSensor (sensorType: String = "Temperature", max: Double = 50.0, min: Double = -30.0) extends Sensor
