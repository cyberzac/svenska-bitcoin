package models

import org.specs2.mutable.Specification

class TradeSpec extends Specification {

  val seller = UserId("seller")
  val buyer = UserId("buyer")
  val askOrder = AskOrderSEK(BTC(10), SEK(40), seller)
  val bidOrder = BidOrderSEK(BTC(6), SEK(50), buyer)

  "A Trade " should {

      val trade = Trade(askOrder, bidOrder, SEK(45))
    "Be created from orders with correct amount" in {
      trade.amount must_== BTC(6)
    }
    "Be created from orders with correct price" in {
      trade.price must_== SEK(45)
    }

   "Be created from orders with correct total" in {
      trade.total must_== SEK(6*45)
    }

  }

}
