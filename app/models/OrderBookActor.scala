package models

import akka.actor.Actor


class OrderBookActor[A <: Currency[A], P <: Currency[P]](val tradeService:TradeService[A, P]) extends Actor {
  val orderBook = new OrderBook[A, P]()

      // Todo ignore trade with one self
  def sendTrades(trades: scala.List[Trade[A, P]]) {
    trades.foreach(trade => {
      tradeService.store(trade)
     // PlayActorService.getUserActor(trade.buyerId) ! trade
     // PlayActorService.getUserActor(trade.sellerId) ! trade
    })
  }

  protected def receive = {

    case askOrder: AskOrder[A, P] => {
      val trades = orderBook.matchOrder(askOrder)
      sendTrades(trades)
    }

    case bidOrder: BidOrder[A, P] => {
      val trades = orderBook.matchOrder(bidOrder)
      sendTrades(trades)
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