package ua.ucu.edu

import akka.actor._
import org.slf4j.LoggerFactory

import ua.ucu.edu.model.Location
import ua.ucu.edu.actor.PlantManagerActor
import ua.ucu.edu.kafka.EmulatorProducer

//import scala.collection.immutable
//import ua.ucu.edu.kafka.DummyDataProducer


object Main extends App {


  val logger = LoggerFactory.getLogger(getClass)

  // initialize actor System with 20 plant actors for various locations
  logger.debug("Initialize solar plants actors for diff. locations")
  val actorSys = ActorSystem("Solar-plants-actors")

  for (i <- 1 to 20) {
    val location = Location(i, i)
    val plantId = i

    actorSys.actorOf(Props(new PlantManagerActor(s"plant-$plantId", location, EmulatorProducer)))
  }
}