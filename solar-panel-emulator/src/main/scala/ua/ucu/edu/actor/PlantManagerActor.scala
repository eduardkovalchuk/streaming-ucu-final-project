package ua.ucu.edu.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import ua.ucu.edu.model.{CriticalState, Location}
import ua.ucu.edu.kafka.{ Producer }

import scala.collection.mutable
import java.util.UUID.randomUUID


/**
  * This actor manages solar plant, holds a list of panels and knows about its location
  * todo - the main purpose right now to initialize panel actors
  */
class PlantManagerActor(
  plantName: String,
  location: Location,
  producer: Producer
) extends Actor with ActorLogging {

  lazy val panelToActorRef: mutable.Map[String, ActorRef] = {

    val panels = mutable.Map[String, ActorRef]()

    for (i <- 1 to 50) {
      val panelId = s"$plantName-$randomUUID"
      panels(panelId) = context.actorOf(Props(new SolarPanelActor(panelId, location, producer)), panelId)
    }
    panels
  }

  override def preStart(): Unit = {
    log.info(s"========== Solar Plant Manager starting ===========")
    super.preStart()
    val panels = panelToActorRef
  }

  override def receive: Receive = {
    case CriticalState => {
      context.stop(self)
    }
  }
}
