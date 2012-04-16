package models

import org.joda.time.DateTime
import java.util.Date

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current
import org.slf4j.LoggerFactory

// Todo add debit, credit
/**
Transaktionstyper:

Insättning  2012-03-05 user1  (Från konto/bg/pg)        500 SEK   ref => kontonummer
Insättning  2012-03-05 user1  (Från bitcoinadress)               500 BTC    ref => btcadress
Uttag         2012-03-15 user1  (Till konto/bg/pg        -500 SEK    ref => kontonummer
Uttag         2012-03-15 user1  (Till bitcoinaddress)              -500 BTC   ref => btcadress
Köp            2012-03-15 user1  (50 BTC á 40 SEK)   -2000 reserved SEK,  50 BTC   ref  => tradeId
Sålt            2012-03-15 user1  (50 BTC á 40 SEK)    2000 SEK , -50 reserved BTC   ref  =>tradeId
Courtage     2012-03-15 user1  (0.5% av 2000 BTC)    -10 BTC    ref =>tradeId
ReservationKöp 2012-04-06 user1 (2000 SEK => Reserved SEK)  ref => OrderId
ReservationSälj 2012-04-06 user1 (50 BTC => Reserved BTC)  ref => OrderId

 Saldon:

 SEK
 reservedSEK
 BTC
 reservedBTC

 id, date, user, debit_account, debit,  credit_account, credit, type, note

 */
case class Transaction(id: Option[Pk[Long]], userId:UserId, debit: Debit, credit: Credit, note: String, time: Long = System.currentTimeMillis()) {
  def dateTime: DateTime = new DateTime(time)

  def date: Date = dateTime.toDate
}


object Transaction {

  /* def apply[A <: Currency[A], P <: Currency[P]](trade:Trade[A, P], userId:UserId):Transaction[A, P] = {
  trade match {
    case Trade(time, tid, amount:A, price:P, `userId`, buyer) => new Transaction(time, tid, -amount, price, userId)
    case Trade(time, tid, amount, price, seller, `userId`) => new Transaction(time, tid, amount, -price, userId)
  }
}  */

  val log = LoggerFactory.getLogger(this.getClass)
  val systemUserId = UserId("0")

  // -- Parsers

  /**
   * Parse a Transaction from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("trans.id") ~
      get[String]("trans.user_id") ~
      get[java.math.BigDecimal]("trans.credit_amount") ~
      get[Int]("trans.credit_account") ~
      get[java.math.BigDecimal]("trans.debit_amount") ~
      get[Int]("trans.debit_account") ~
      get[String]("trans.note") ~
      get[Date]("trans.created_date") map {
      case id ~ userId ~ creditAmount ~ creditAccount ~ debitAmount ~ debitAccount ~ note ~ created => {
        Transaction(
          Some(id),
          UserId(userId),
          Debit(debitAmount, Account(debitAccount)),
          Credit(creditAmount, Account(creditAccount)),
          note, created.getTime
        )
      }
    }
  }

  // -- Queries

  def findTransaction(id: Pk[Long]): Option[Transaction] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from trans where id = {id} ").on(
          'id -> id
        ).as(Transaction.simple.singleOpt)
    }

  }

  def findTransactions(userId: UserId): Seq[Transaction] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from trans where user_id = {userId}").on(
          'userId -> userId.value
        ).as(Transaction.simple *)
    }
  }

  case class BankReference(value: String)

  case class CourtageReference(value: String)

  case class BitcoinAddressReference(value: String)


  /**
   * Creates an fund transaction
   */
  def create(userId: UserId, sek: SEK, reference: BankReference, time: Long = System.currentTimeMillis()): Transaction = {
    val amount = sek.value
    val transaction = if (amount > 0) {
      Transaction(None, userId, Debit(amount, Bank), Credit(amount, UserSek), reference.value, time)
    } else {
      Transaction(None, userId, Debit(-amount, UserSek), Credit(-amount, Bank), reference.value, time)
    }
    create(transaction)
  }


  /**
   * Create a Transaction.
   */
  def create(trans: Transaction): Transaction = {
    DB.withConnection {
      implicit connection =>

      // Get the trans id
        val id: Long = SQL("select next value for trans_id_seq").as(scalar[Long].single)

        SQL(
          """
            insert into trans values (
              {id},
              {user_id},
              {credit_amount}, {credit_account},
              {debit_amount}, {debit_account},
              {note}, {trans_id}, {created}
            )
          """
        ).on(
          'id -> id,
          'user_id -> trans.userId.value,
          'credit_amount -> new java.math.BigDecimal(trans.credit.amount.toString()),
          'credit_account -> trans.credit.account.number,
          'debit_amount -> new java.math.BigDecimal(trans.debit.amount.toString()),
          'debit_account -> trans.debit.account.number,
          'trans_id -> trans.id,
          'created -> trans.date,
          'note -> trans.note
        ).executeUpdate()
        val t = trans.copy(id = Some(Id(id)))
        log.debug("Stored transaction {}", t)
        t
    }
  }

}

object TransactionId {
  var count: Long = 0

  def apply() = {
    count += 1
    new TransactionId(count.toString)
  }
}

case class TransactionId(value: String)


//case class SekAccount(value: SEK, userId: UserId) extends DebitCredit(1, value.value, userId)

//case class ReservedSekAccount(value: SEK, userId: UserId) extends DebitCredit(2, value.value, userId)

//case class BtcAccount(value: BTC, userId: UserId) extends DebitCredit(3, value.value, userId)

//case class ReservedBtcAccount(value: BTC, userId: UserId) extends DebitCredit(4, value.value, userId)


