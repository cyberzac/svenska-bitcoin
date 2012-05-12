package models

import akka.actor.Actor
import org.slf4j.LoggerFactory

class UserActor(val userId: UserId) extends Actor {

  val log = LoggerFactory.getLogger(classOf[UserActor])

  def receive: Receive = {

    case trade@Trade(id: TradeId, amount: BTC, price: SEK, `userId`, `userId`, time: Long) => {
      log.info("Ignoring trade with myself " + trade)
    }
    case trade@Trade(id: TradeId, amount: BTC, price: SEK, seller, `userId`, time: Long) => {
      val user = User.findById(userId).get
      log.info(user.name + " bought " + amount + " at " + price + "/BTC")
    }

    case trade@Trade(id: TradeId, amount: BTC, price: SEK, `userId`, buyer, time: Long) => {
      val user = User.findById(userId).get
      log.info(user.name + " sold " + amount + " at " + price + "/BTC")
    }

    case GetUserMsg => {
      // Todo remove this
      val user = User.findById(userId).get
      log.debug("Returning user {}", user)
      sender ! user
    }

    case x => {
      log.warn("Failed handling message {}", x)
    }
  }


}