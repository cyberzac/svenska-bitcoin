package models

import org.specs2.mutable.Specification

class InMemoryTradeServiceSpec extends Specification {

  "A InMemoryTradeService" should {

    "Store a trade for a seller" in {
      val dut = new InMemoryTradeService[BTC, SEK]()
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId1")  must_== Balance(BTC(-2), SEK(80))
    }

    "Store a trade for a buyer" in {
      val dut = InMemoryTradeService[BTC, SEK]
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId2") must_== Balance(BTC(2), SEK(-80))
    }

    "Sum trades for a seller" in {
      val dut = InMemoryTradeService[BTC, SEK]
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.store(Trade(BTC(2), SEK(40), "userId3", "userId4"))
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId1") must_== Balance(BTC(-4), SEK(160))
    }

    "Sum a trades for a buyer" in {
      val dut = InMemoryTradeService[BTC, SEK]
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.store(Trade(BTC(2), SEK(50), "userId3", "userId2"))
      dut.store(Trade(BTC(2), SEK(40), "userId1", "userId2"))
      dut.sumByUser("userId2") must_== Balance(BTC(6), SEK(-260))
    }

    "Sum trade to zero if no trades are done" in {
      val dut = InMemoryTradeService[BTC, SEK]
      dut.sumByUser("userId2") must_== Balance()
    }
  }

}
