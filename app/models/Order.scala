package models

import org.joda.time.DateTime

sealed abstract class Order[A <: Currency[A], P <: Currency[P]] {
  val amount: A
  val price: P
  val userId: UserId
  val timestamp: Long

  def total: P = price * amount.value

  def create(amount: A, timestamp: Long = System.currentTimeMillis()): Order[A, P]

  def dateTime: DateTime = new DateTime(timestamp)

}

// Todo remove the case sub classes?
abstract class AskOrder[A <: Currency[A], P <: Currency[P]] extends Order[A, P] {
  val sellerId = userId

  def create(amount: A, timestamp: Long = System.currentTimeMillis()): AskOrder[A, P]

}

abstract class BidOrder[A <: Currency[A], P <: Currency[P]] extends Order[A, P] {
  val buyerId = userId

  def create(amount: A, timestamp: Long = System.currentTimeMillis()): BidOrder[A, P]
}

case class AskOrderSEK(amount: BTC, price: SEK, userId: UserId, timestamp: Long = System.currentTimeMillis()) extends AskOrder[BTC, SEK] {
  def create(amount: BTC, timestamp: Long = System.currentTimeMillis()) = AskOrderSEK(amount, price, sellerId, timestamp)
}

case class BidOrderSEK(amount: BTC, price: SEK, userId: UserId, timestamp: Long = System.currentTimeMillis()) extends BidOrder[BTC, SEK] {
  def create(amount: BTC, timestamp: Long = System.currentTimeMillis()) = BidOrderSEK(amount, price, buyerId, timestamp)
}