package ua.ucu.edu.model

import ua.ucu.edu.model.Location

/**
  * To be used as a message in device topic
  */
case class SensorRecord(panelId: String, location: Location, sensorType: String, measurement: Double)
