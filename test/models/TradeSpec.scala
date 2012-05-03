package models

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TradeSpec extends Specification {

  val seller = UserId(1)
  val buyer = UserId(2)
  val askOrder = AskOrderSEK(BTC(10), SEK(40), seller)
  val bidOrder = BidOrderSEK(BTC(6), SEK(50), buyer)

  "A Trade " should {

    "Be created from orders with correct amount" in running(FakeApplication()) {
      val trade = Trade(askOrder, bidOrder, SEK(40))
      trade.amount must_== BTC(6)
    }
    "Be created from orders with correct price" in running(FakeApplication()) {
      val trade = Trade(askOrder, bidOrder, SEK(40))
      trade.price must_== SEK(40)
    }

    "Have a dateTime member" in running(FakeApplication()) {
      val trade = Trade(askOrder, bidOrder, SEK(40))
      trade.dateTime must_== new DateTime(trade.time)
    }

    "Have a total" in running(FakeApplication()) {
      val trade = Trade(askOrder, bidOrder, SEK(40))
      trade.total must_== SEK(240)
    }
  }
}
