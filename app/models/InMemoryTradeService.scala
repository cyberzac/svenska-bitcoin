package models

import org.slf4j.LoggerFactory


object InMemoryTradeService {
  def apply[A <: Currency[A], P <: Currency[P]]() = {
    new InMemoryTradeService[A, P]()
  }
}

class InMemoryTradeService[A <: Currency[A], P <: Currency[P]] extends TradeService[A, P] {

  def getTrades(userId: UserId): List[Trade[A, P]] = trades filter { trade =>
    trade.sellerId == userId || trade.buyerId == userId
  }

  var trades = List[Trade[A, P]]()

  val log = LoggerFactory.getLogger(this.getClass)

  def store(trade: Trade[A, P]) {
    log.info("Stored trade {}", trade)
    trades = trade :: trades
  }

  def sumByUser(userId: UserId): Balance = {
    if (trades isEmpty) return Balance()
    val balance = trades.foldLeft(Balance()) {
      (b, trade) => {
        trade match {
          case Trade(time, tradeId, a: BTC, price: SEK, `userId`, `userId`) => b
          case Trade(time, tradeId, a: BTC, price: SEK, seller, `userId`) => b.buy(a, price)
          case Trade(time, tradeId, a: BTC, price: SEK, `userId`, buyer) => b.sell(a, price)
          case _ => b
        }
      }
    }
    log.debug("UserId {} balanace is {}", userId, balance)
    balance
  }
}


