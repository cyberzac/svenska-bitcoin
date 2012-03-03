package models

import org.specs2.mutable.Specification

class OrderSpec extends Specification {
  val userId = UserId("1")
  val askOrderSEK = AskOrderSEK(BTC(10), SEK(5), userId)

  "AskOrderSEK" should {
    "Have a total method" in {
      askOrderSEK.total must_== SEK(50)
    }

    "Have a create method" in {
      val newOrder = askOrderSEK.create(BTC(4))
      newOrder.amount must_== BTC(4)
      newOrder.price must_== SEK(5)
      newOrder.sellerId must_== userId
    }
  }
}