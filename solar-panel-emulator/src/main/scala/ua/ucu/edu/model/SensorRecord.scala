package ua.ucu.edu.model

/**
  * To be used as a message in device topic
  */
case class SensorRecord(panelId: String, location: Location, sensorType: String, measurement: Double) {
  def stingify() = {
    val lat = location.latitude
    val long = location.longitude
    s"$panelId;$sensorType;$measurement;$lat;$long"
  }
}
