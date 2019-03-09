package ua.ucu.edu

import scala.io.Source
import akka.actor._
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.databind.ObjectMapper
import ua.ucu.edu.model.Location
import ua.ucu.edu.actor.PlantManagerActor

//import scala.collection.immutable
//import ua.ucu.edu.kafka.DummyDataProducer


object Main extends App {
//  implicit val system: ActorSystem = ActorSystem()
//  system.actorOf(Props(classOf[PlantManagerActor], "plant1", Location(0, 0)), "plant1-manager")
//
//  DummyDataProducer.pushTestData()

  // get data from json file with locations
  val locationsFile = Source.fromFile("locations.json").getLines.mkString

  val mapper = new ObjectMapper()
  mapper.registerModule(DefaultScalaModule)

  val locations: List[Map[String, Int]] = mapper.readValue(locationsFile, classOf[List[Map[String, Int]]])

  // initialize actor System
  val actorSys = ActorSystem("Solar-plants-actors")


  for (locData <- locations) {
    val location = Location(locData("latitude").toDouble, locData("longitude").toDouble)
    val plantId = locData("id")

    actorSys.actorOf(Props(new PlantManagerActor(s"plant-$plantId", location)))
  }
}