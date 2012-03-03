package models

import org.specs2.mutable.Specification
import org.specs2.specification.Scope

class InMemoryTradeServiceSpec extends Specification {

  trait setUp extends Scope {
    val dut = new InMemoryTradeService[BTC, SEK]()
    val trade1 = Trade(BTC(2), SEK(40), "userId1", "userId2")
    val trade2 = Trade(BTC(2), SEK(40), "userId3", "userId4")
    val trade3 = Trade(BTC(2), SEK(40), "userId1", "userId2")

    def setUpTrades() {
      dut.store(trade1)
      dut.store(trade2)
      dut.store(trade3)
    }
  }

  "A InMemoryTradeService" should {

    "Store a trade for a seller" in new setUp {
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId1") must_== Balance(BTC(-2), SEK(80))
    }

    "Store a trade for a buyer" in new setUp {
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId2") must_== Balance(BTC(2), SEK(-80))
    }

    "Sum trades for a seller" in new setUp {
      setUpTrades()
      dut.sumByUser("userId1") must_== Balance(BTC(-4), SEK(160))
    }

    "Sum a trades for a buyer" in new setUp {
      setUpTrades()
      dut.sumByUser("userId2") must_== Balance(BTC(4), SEK(-160))
    }

    "Sum trade to zero if no trades are done" in new setUp {
      dut.sumByUser("userId2") must_== Balance()
    }

    "Get all trades for a user" in new setUp {
      setUpTrades()
      val trades = dut.getTrades("userId1")
      trades must_== List(trade3, trade1)
    }
  }

}
