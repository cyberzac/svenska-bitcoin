package models

import akka.actor.Actor
import org.slf4j.LoggerFactory


class OrderBookActor[A <: Currency[A], P <: Currency[P]]() extends Actor {
  val orderBook = new OrderBook[A, P]()

  val log = LoggerFactory.getLogger(this.getClass)

  def createTransactions(trades: List[Trade[A, P]]) = {
    log.info("Trades done: {}", trades)
    trades.foreach {
      (trade: Trade[A, P]) => trade match {
        case t: Trade[BTC, SEK] => Transaction.trade(t)
      }
    }
  }

  protected def receive = {

    case askOrder: AskOrder[A, P] => {
      val trades = orderBook.matchOrder(askOrder)
      createTransactions(trades)
    }

    case bidOrder: BidOrder[A, P] => {
      val trades = orderBook.matchOrder(bidOrder)
      createTransactions(trades)
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