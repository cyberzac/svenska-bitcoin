package models

import org.joda.time.DateTime
import java.util.Date

import anorm._
import anorm.SqlParser._

import play.api.db._
import play.api.Play.current
import org.slf4j.LoggerFactory
import java.lang.RuntimeException

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
case class Transaction(id: Option[Pk[Long]], debit: Debit, credit: Credit, note: String, time: Long = System.currentTimeMillis()) {

  def transactionId = TransactionId(id.getOrElse(throw new IllegalStateException("Transaction not stored")).get)

  def dateTime: DateTime = new DateTime(time)

  def date: Date = dateTime.toDate
}


object Transaction {

  val log = LoggerFactory.getLogger(this.getClass)

  // -- Parsers

  /**
   * Parse a Transaction from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("trans.id") ~
      get[Long]("trans.credit_user_id") ~
      get[java.math.BigDecimal]("trans.credit_amount") ~
      get[Int]("trans.credit_account") ~
      get[Long]("trans.debit_user_id") ~
      get[java.math.BigDecimal]("trans.debit_amount") ~
      get[Int]("trans.debit_account") ~
      get[String]("trans.note") ~
      get[Date]("trans.created_date") map {
      case id ~ creditUserId ~ creditAmount ~ creditAccount ~ debitUserId ~ debitAmount ~ debitAccount ~ note ~ created => {
        Transaction(
          Some(id),
          Debit(debitAmount, Account(debitAccount), UserId(debitUserId)),
          Credit(creditAmount, Account(creditAccount), UserId(creditUserId)),
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


  def findAll: scala.List[Transaction] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from trans").as(Transaction.simple *)
    }
  }

  /**
   * Finds all transactions for a given userId
   * @param userId  the userId
   * @return all transactions belonging to this userId
   */
  def findTransactions(userId: UserId): Seq[Transaction] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from trans where credit_user_id = {userId} or debit_user_id = {userId}").on(
          'userId -> userId.value
        ).as(Transaction.simple *)
    }
  }

  def balance(userId: UserId) = Balance(balanceBTC(userId), balanceSEK(userId), balanceReservedBTC(userId), balanceReservedSEK(userId))

  def balanceSEK(userId: UserId): SEK = SEK(balance(userId, UserSek))

  def balanceReservedSEK(userId: UserId): SEK = SEK(balance(userId, UserReservedSek))

  def balanceBTC(userId: UserId): BTC = BTC(balance(userId, UserBtc))

  def balanceReservedBTC(userId: UserId): BTC = BTC(balance(userId, UserReservedBtc))

  def balance(userId: UserId, account: Account): BigDecimal = {

    def sumBalance(column: String): BigDecimal = {
      val balance = try {
        DB.withConnection {
          implicit connection =>
            SQL("select COALESCE(sum(" + column + "_amount),0) from trans where " + column + "_user_id = {userId} and " + column + "_account = {account} ").on(
              'userId -> userId.value,
              'account -> account.number
            ).as(scalar[java.math.BigDecimal].single)
        }
      }
      log.debug("Sum {} user {}  account {} =  {}", List(column, userId, account, balance))
      BigDecimal(balance)
    }

    val credit = sumBalance("credit")
    val debit = sumBalance("debit")
    credit - debit

  }

  /**
   * Creates a SEK fund transaction
   */
  def fund(userId: UserId, sek: SEK, reference: BankReference): Transaction = {
    fund(userId, sek, reference, System.currentTimeMillis())
  }

  /**
   * Creates a SEK fund transaction
   */
  def fund(userId: UserId, sek: SEK, reference: BankReference, time: Long): Transaction = {
    create(sek.value, BankSek, UserId.svenskaBitcoin, UserSek, userId, reference.value, time)
  }


  def create(amount: scala.BigDecimal, debitAccount: Account, debitUserId: UserId, creditAccount: Account, creditUserId: UserId, note: String, time: Long): Transaction = {
    if (amount > 0) {
      create(Debit(amount, debitAccount, debitUserId), Credit(amount, creditAccount, creditUserId), note, time)
    } else {
      create(Debit(-amount, creditAccount, creditUserId), Credit(-amount, debitAccount, debitUserId), note, time)
    }
  }

  /**
   * Creates a BTC fund transaction
   */
  def fund(userId: UserId, btc: BTC, reference: BtcAddressReference): Transaction = {
    fund(userId, btc, reference, System.currentTimeMillis())
  }

  /**
   * Creates a BTC fund transaction
   */
  def fund(userId: UserId, btc: BTC, reference: BtcAddressReference, time: Long): Transaction = {
    create(btc.value, BankBtc, UserId.svenskaBitcoin, UserBtc, userId, reference.value, time)
  }

  /**
   * Creates a reserve SEK transaction
   */
  def reserve(userId: UserId, sek: SEK, reference: OrderReference): Transaction = {
    reserve(userId, sek, reference, System.currentTimeMillis())
  }

  /**
   * Creates a reserve SEK transaction
   */
  def reserve(userId: UserId, sek: SEK, reference: OrderReference, time: Long): Transaction = {
    create(sek.value, UserSek, userId, UserReservedSek, userId, reference.value, time)
  }

  /**
   * Creates a reserve BTC transaction
   */
  def reserve(userId: UserId, btc: BTC, reference: OrderReference): Transaction = {
    reserve(userId, btc, reference, System.currentTimeMillis())
  }

  /**
   * Creates a reserve BTC transaction
   */
  def reserve(userId: UserId, btc: BTC, reference: OrderReference, time: Long): Transaction = {
    create(btc.value, UserBtc, userId, UserReservedBtc, userId, reference.value, time)
  }

  /**
   * Creates a BTC buy transaction
   */
  def trade(trade: Trade[BTC, SEK]): (Transaction, Transaction) = {
    val amount = trade.amount.value
    val price = trade.total.value
    val tradeReference = trade.tradeId.toString
    val time = trade.dateTime.getMillis
    (
      create(Debit(amount, UserReservedBtc, trade.sellerId), Credit(amount, UserBtc, trade.buyerId), tradeReference, time),
      create(Debit(price, UserReservedSek, trade.buyerId), Credit(price, UserSek, trade.sellerId), trade.tradeId.toString, time)
      )

  }

  /**
   * Creates a Transaction
   * @param debit
   * @param credit
   * @param note
   * @param time
   * @return
   */
  def create(debit: Debit, credit: Credit, note: String, time: Long): Transaction = create(Transaction(None, debit, credit, note, time))

  /**
   * Create a Transaction.
   */
  def create(trans: Transaction): Transaction = {
    DB.withConnection {
      implicit connection =>

      // Get the trans id
        val id: Long = SQL("select next value for trans_seq").as(scalar[Long].single)

        SQL(
          """
            insert into trans values (
              {id},
              {credit_user_id}, {credit_amount}, {credit_account},
              {debit_user_id}, {debit_amount}, {debit_account},
              {note}, {trans_id}, {created}
            )
          """
        ).on(
          'id -> id,
          'credit_user_id -> trans.credit.userId.value,
          'credit_amount -> new java.math.BigDecimal(trans.credit.amount.toString()),
          'credit_account -> trans.credit.account.number,
          'debit_user_id -> trans.debit.userId.value,
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

case class TransactionId(value: Long)



