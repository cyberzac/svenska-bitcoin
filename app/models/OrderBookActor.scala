package models

import akka.actor.Actor


class OrderBookActor[A <: Currency[A], P <: Currency[P]]() extends Actor {
  val orderBook = new OrderBook[A, P]()


  protected def receive = {

    case askOrder: AskOrder[A, P] => {
      val trades = orderBook.matchOrder(askOrder)
    }

    case bidOrder: BidOrder[A, P] => {
      val trades = orderBook.matchOrder(bidOrder)
    }

    case ListOrders => {
      sender ! Orders(orderBook.askOrders, orderBook.bidOrders)
    }

    case ListOrders(userId) => {
      val asks = orderBook.askOrders.filter(_.sellerId == userId)
      val bids = orderBook.bidOrders.filter(_.buyerId == userId)
      sender ! Orders(asks, bids)
    }
  }
}