package models

import org.slf4j.LoggerFactory


object Balance {
  def apply() = {
     new Balance(BTC(0), SEK(0))
  }
}

case class Balance(btc: BTC, sek: SEK) {
  val log = LoggerFactory.getLogger(this.toString)

  def buy(amount:BTC,  price:SEK): Balance = {
    Balance(btc + amount, sek - price*amount.value)
  }
  def sell(amount:BTC,  price:SEK): Balance = {
    Balance(btc - amount, sek + price*amount.value)
  }
}