package models

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import java.util.Date
import anorm.Id
import play.api.test.FakeApplication
import play.api.test.Helpers._

class TransactionSpec extends Specification {

  "A transaction" should {

    val userId = UserId("userId")
    val bankRef = BankReference("123455")
    val orderRef = OrderReference("123455")
    val time = System.currentTimeMillis()
    val sekAmount = 125
    val sek = SEK(sekAmount)
    val btcAmount = 3.14
    val btc = BTC(btcAmount)
    val bitcoinAddressReference = BtcAddressReference("1Fxxx")

    "have a dateTime" in {
      val transaction = Transaction(Some(Id(1000)), userId, Debit(100, BankSek), Credit(100, UserBtc), bankRef.value, time)
      transaction.dateTime must_== new DateTime(time)
    }

    "have a date" in {
      val transaction = Transaction(Some(Id(1000)), userId, Debit(100, BankSek), Credit(100, UserBtc), bankRef.value, time)
      transaction.date must_== new Date(time)
    }

    "Provide a add SEK fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.fund(userId, sek, bankRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(sekAmount, BankSek), Credit(sekAmount, UserSek), bankRef.value, time)
      }
    }

    "Provide a subtract SEK fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.fund(userId, -sek, bankRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(sekAmount, UserSek), Credit(sekAmount, BankSek), bankRef.value, time)
      }
    }

    "Find a transaction by id" in {
      running(FakeApplication()) {
        val expected = Transaction.fund(userId, sek, bankRef, time)
        val id = expected.id.get
        val actual = Transaction.findTransaction(id).get
        actual must_== expected
      }
    }

    "Provide a balanceSek method yielding zero if there are no transactions" in {
      running(FakeApplication()) {
        Transaction.balanceSEK(userId) must_== SEK(0)
      }
    }

    "Provide a balanceSek method yielding the sum of all UserSek transactions" in {
      running(FakeApplication()) {
        Transaction.fund(userId, SEK(10), bankRef)
        Transaction.fund(userId, SEK(20), bankRef)
        Transaction.fund(userId, SEK(-40), bankRef)
        Transaction.balanceSEK(userId) must_== SEK(-10)
      }
    }

    "Provide a reserve SEK fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.reserve(userId, sek, orderRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(sekAmount, UserSek), Credit(sekAmount, UserReservedSek), bankRef.value, time)
      }
    }

    "Provide a balanceReservedSek method yielding the sum of all UserReservedSek transactions" in {
      running(FakeApplication()) {
        Transaction.fund(userId, SEK(10), bankRef)
        Transaction.fund(userId, SEK(20), bankRef)
        Transaction.reserve(userId, SEK(20), orderRef)
        Transaction.balanceReservedSEK(userId) must_== SEK(20)
      }
    }

    "Provide a add BTC fund transaction" in {
      running(FakeApplication()) {
        Transaction.fund(userId, btc, bitcoinAddressReference, time)
        Transaction.fund(userId, btc, bitcoinAddressReference)
        Transaction.balanceBTC(userId) must_== btc * 2
      }
    }

    "Provide a reserve BTC fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.reserve(userId, btc, orderRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(btcAmount, UserBtc), Credit(btcAmount, UserReservedBtc), bankRef.value, time)
      }
    }

    // Todo Throw an IllegalArgumentException if the transaction is not balanced.


    "Find all transactions by userId" in {
      running(FakeApplication()) {
        Transaction.fund(userId, sek, bankRef, time)
        Transaction.fund(userId, -sek, bankRef, time)
        val t1 = Transaction(Some(Id(1000)), userId, Debit(sekAmount, BankSek), Credit(sekAmount, UserSek), bankRef.value, time)
        val t2 = Transaction(Some(Id(1001)), userId, Debit(sekAmount, UserSek), Credit(sekAmount, BankSek), bankRef.value, time)
        Transaction.findTransactions(userId) must_== Seq(t1, t2)
      }
    }


  }

}
