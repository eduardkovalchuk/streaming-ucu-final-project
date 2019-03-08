package ua.ucu.edu.device

trait SensorApi {
  def readCurrentValue: Double
  def sensor: Sensor
}
