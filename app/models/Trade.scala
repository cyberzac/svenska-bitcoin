package models

import org.joda.time.DateTime


object Trade {

  def apply[A <: Currency[A], P <: Currency[P]](ask: AskOrder[A, P], bid: BidOrder[A, P], price: P): Trade[A, P] = {
    if (ask.price > bid.price) throw new IllegalArgumentException("Bid price must be equal or higher then ask")
    val amount = ask.amount.min(bid.amount)
    Trade(amount, price, ask.sellerId, bid.buyerId)
  }

  def apply[A <: Currency[A], P <: Currency[P]](amount: A, price: P, sellerId: UserId, buyerId: UserId): Trade[A, P] = {
    val tradeId = TradeId()
    val time = System.currentTimeMillis()
    Trade(time, tradeId, amount, price, sellerId, buyerId)
  }
}

case class Trade[A <: Currency[A], P <: Currency[P]](time: Long, id: TradeId, amount: A, price: P, sellerId: UserId, buyerId: UserId) {
  def toUserTrade(userId:UserId):UserTrade[A, P]  = UserTrade[A, P](this, userId)
}

object UserTrade {
  def apply[A <: Currency[A], P <: Currency[P]](trade:Trade[A, P], userId:UserId):UserTrade[A, P] = {
      trade match {
        case Trade(time, tid, amount, price, `userId`, buyer) => new UserTrade(time, tid, -amount, price, userId)
        case Trade(time, tid, amount, price, seller, `userId`) => new UserTrade(time, tid, amount, -price, userId)
      }
  }
}

case class UserTrade[A <: Currency[A], P <: Currency[P]](time: Long, id: TradeId, amount: A, price: P, userId:UserId) {
  def dateTime: DateTime = new DateTime(time)
  val total = price * amount.value
}

object TradeId {
  // Todo use uuid,  db-sequence...
  var count = 0L

  def apply(): TradeId = {
    count = count + 1
    TradeId(count)
  }
}

case class TradeId(id: Long)