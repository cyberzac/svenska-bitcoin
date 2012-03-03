package models


object InMemoryTradeService {
  def apply[A <: Currency[A], P <: Currency[P]]() = {
    new InMemoryTradeService[A, P]()
  }
}

class InMemoryTradeService[A <: Currency[A], P <: Currency[P]] extends TradeService[A, P] {
  var trades = List[Trade[A, P]]()

  def store(trade: Trade[A, P]) = {
    trades = trade :: trades
  }

  def sumByUser(userId: UserId): Balance = {
    if (trades isEmpty) return Balance()
    trades.foldLeft(Balance()) {
      (b, trade) => {
        trade match {
          case Trade(time, tradeId, a: BTC, price: SEK, `userId`, `userId`) => b
          case Trade(time, tradeId, a: BTC, price: SEK, seller, `userId`) => b.buy(a, price)
          case Trade(time, tradeId, a: BTC, price: SEK, `userId`, buyer) => b.sell(a, price)
          case _ => b
        }
      }
    }
  }
}


