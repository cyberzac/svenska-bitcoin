package models

import akka.actor.Actor
import org.slf4j.LoggerFactory

class UserActor(val userId: UserId) extends Actor {

  val log = LoggerFactory.getLogger(classOf[UserActor])

  def receive: Receive = {

    case trade@Trade(time: Long, id: TradeId, amount: BTC, price: SEK, `userId`, `userId`) => {
      log.info("Ignoring trade with myself " + trade)
    }
    case trade@Trade(time: Long, id: TradeId, amount: BTC, price: SEK, seller, `userId`) => {
     // Todo replace with sum of matching trades in TradeService
      val user = User.findById(userId).get
      log.info(user.name + " bought " + amount + " at " + price + "/BTC")
    }

    case trade@Trade(time: Long, id: TradeId, amount: BTC, price: SEK, `userId`, buyer) => {
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