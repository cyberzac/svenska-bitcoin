package models


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

// Todo need state for confirmations? How to ensure that there is funds to cover this for seller and buyer
case class Trade[A <: Currency[A], P <: Currency[P]](time: Long, id: TradeId, amount: A, price: P, sellerId: UserId, buyerId: UserId) {
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