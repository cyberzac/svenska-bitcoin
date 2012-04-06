package models

import org.joda.time.DateTime
import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

case class Transaction(id:Pk[Long], userId:UserId, sek:SEK, btc:BTC, note:String, time:Long = System.currentTimeMillis())  {
  def dateTime: DateTime = new DateTime(time)
  def date: Date = dateTime.toDate
}

/**
Transaktionstyper:

Insättning  2012-03-05 user1  (Från konto/bg/pg)        500 SEK   ref => från formulär
Insättning  2012-03-05 user1  (Från adress)               500 BTC    ref
Uttag         2012-03-15 user1  (Till konto/bg/pg        -500 SEK    ref?
Uttag         2012-03-15 user1  (Till address)              -500 BTC   ref?
Köp            2012-03-15 user1  (50 BTC á 40 SEK)   -2000 SEK   ref  => tradeId
Sålt            2012-03-15 user1  (50 BTC á 40 SEK)    2000 SEK   ref  =>tradeId
Courtage     2012-03-15 user1  (0.5% av 2000 BTC)    -10 BTC    ref =>tradeId
 */
object Transaction {
 /* def apply[A <: Currency[A], P <: Currency[P]](trade:Trade[A, P], userId:UserId):Transaction[A, P] = {
    trade match {
      case Trade(time, tid, amount:A, price:P, `userId`, buyer) => new Transaction(time, tid, -amount, price, userId)
      case Trade(time, tid, amount, price, seller, `userId`) => new Transaction(time, tid, amount, -price, userId)
    }
  }  */

  // -- Parsers

  /**
   * Parse a Transaction from a ResultSet
   */
  val simple = {
      get[Pk[Long]]("trans.id") ~
      get[String]("trans.user_id") ~
      get[Long]("trans.sek") ~          // Todo use BigDecimal
      get[Long]("trans.btc") ~          // Todo use BigDecimal
      get[String]("trans.note") ~
      get[Date]("trans.created_date") map {
      case id~userId~sek~btc~note~created => Transaction(
        id, UserId(userId), SEK(sek)/1000, BTC(btc)/(1000*1000*100), note, created.getTime
      )
    }
  }

}

 /*
case class sekFundTransaction(userId:UserId, sek:SEK, account:BankAccount, transactionId:TransactionId = TransactionId(), time:Long = System.currentTimeMillis()) extends Transaction {
  val btc = 0
  val note = account.toString
}
case class tradeTransaction(userId:UserId, sek:SEK, btc:BTC, tradeId:TradeId, transactionId:TransactionId = TransactionId(), time:Long = System.currentTimeMillis()) extends Transaction  {
  val note = tradeId.toString
}
case class courtageTransaction(userId:UserId, sek:SEK, btc:BTC, tradeId:TradeId, courtage:Courtage,transactionId:TransactionId = TransactionId(), time:Long = System.currentTimeMillis()) extends Transaction {
  val note = courtage.toString + ":" + tradeId.toString
}

case class btcFundTransaction(userId:UserId, btc:BTC, address:BitcoinAddress, transactionId:TransactionId = TransactionId(), time:Long = System.currentTimeMillis()) extends Transaction {
  val sek = 0
  val note = address.toString
}
*/




object TransactionId {
  var count:Long = 0
  def apply() = {
    count += 1
    new TransactionId(count.toString)
  }
}
case class TransactionId(value:String)
case class BitcoinAddress(address:String)
case class BankAccount(account:String)
case class Courtage(value:BigDecimal)

