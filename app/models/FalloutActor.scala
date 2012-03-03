package models

import akka.actor.Actor
import org.slf4j.LoggerFactory

class FalloutActor extends Actor {

  val log = LoggerFactory.getLogger(getClass)

  protected def receive = {
    case m => log.warn("Failed handling message " + m)
  }
}
