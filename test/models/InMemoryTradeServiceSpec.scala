package models

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class InMemoryTradeServiceSpec extends Specification {

  trait setUp extends Scope {
    val dut = new InMemoryTradeService[BTC, SEK]()
    val user1 = UserId(1)
    val user2 = UserId(2)
    val user3 = UserId(3)
    val user4 = UserId(4)
    val trade1 = Trade(BTC(2), SEK(40), user1, user2)
    val trade2 = Trade(BTC(2), SEK(40), user3, user4)
    val trade3 = Trade(BTC(2), SEK(40), user1, user2)

    def setUpTrades() {
      dut.store(trade1)
      dut.store(trade2)
      dut.store(trade3)
    }
  }

  "A InMemoryTradeService" should {

    "Store a trade for a seller" in new setUp {
      dut.store(Trade(BTC(2), SEK(40), user1, user2))
      dut.sumByUser(user1) must_== Balance(BTC(-2), SEK(80))
    }

    "Store a trade for a buyer" in new setUp {
      dut.store(Trade(BTC(2), SEK(40), user1, user2))
      dut.sumByUser(user2) must_== Balance(BTC(2), SEK(-80))
    }

    "Sum trades for a seller" in new setUp {
      setUpTrades()
      dut.sumByUser(user1) must_== Balance(BTC(-4), SEK(160))
    }

    "Sum a trades for a buyer" in new setUp {
      setUpTrades()
      dut.sumByUser(user2) must_== Balance(BTC(4), SEK(-160))
    }

    "Sum trade to zero if no trades are done" in new setUp {
      dut.sumByUser(user2) must_== Balance()
    }

    "Get all trades for a user" in new setUp {
      setUpTrades()
      val trades = dut.getTrades(user1)
      trades must_== List(trade3, trade1)
    }
  }

}
