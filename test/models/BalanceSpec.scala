package models

import org.specs2.mutable.Specification

class BalanceSpec extends Specification {

  "A Balance" should {

    val balance = Balance()

    "Be created with zeo balance" in {
      balance must_== Balance(BTC(0), SEK(0))
    }

    "Increase btc and decrease sek for a buy trade" in {
      balance.buy(BTC(10), SEK(40)) must_== Balance(BTC(10), SEK(-400))
    }

    "Descrease btc and increase sek for a sell trade" in {
      balance.sell(BTC(10), SEK(40)) must_== Balance(BTC(-10), SEK(400))
    }
  }

}
