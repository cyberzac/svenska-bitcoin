package models

import org.slf4j.LoggerFactory


object Balance {
  def apply() = {
     new Balance(BTC(0), SEK(0), BTC(0), SEK(0))
  }
}

case class Balance(btc: BTC, sek: SEK, reservedBTC:BTC, reservedSEK:SEK)