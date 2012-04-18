package models

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import java.util.Date
import anorm.Id
import play.api.test.FakeApplication
import play.api.test.Helpers._
import models.Transaction.{OrderReference, BankReference}

class TransactionSpec extends Specification {

  "A transaction" should {

    val userId = UserId("userId")
    val bankRef = BankReference("123455")
    val orderRef = OrderReference("123455")
    val time = System.currentTimeMillis()
    val amount = 125
    val sek = SEK(amount)

    "have a dateTime" in {
      val transaction = Transaction(Some(Id(1000)), userId, Debit(100, Bank), Credit(100, UserBitcoin), bankRef.value, time)
      transaction.dateTime must_== new DateTime(time)
    }

    "have a date" in {
      val transaction = Transaction(Some(Id(1000)), userId, Debit(100, Bank), Credit(100, UserBitcoin), bankRef.value, time)
      transaction.date must_== new Date(time)
    }

    "Create a add fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.fund(userId, sek, bankRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(amount, Bank), Credit(amount, UserSek), bankRef.value, time)
      }
    }

    "Create a subtract fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.fund(userId, -sek, bankRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(amount, UserSek), Credit(amount, Bank), bankRef.value, time)
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

    "Create reserve fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.reserve(userId, sek, orderRef, time)
        t must_== Transaction(Some(Id(1000)), userId, Debit(amount, UserSek), Credit(amount, UserReservedSek), bankRef.value, time)
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

    // Todo Throw an IllegalArgumentException if the transaction is not balanced.


    "Find all transactions by userId" in {
      running(FakeApplication()) {
        Transaction.fund(userId, sek, bankRef, time)
        Transaction.fund(userId, -sek, bankRef, time)
        val t1 = Transaction(Some(Id(1000)), userId, Debit(amount, Bank), Credit(amount, UserSek), bankRef.value, time)
        val t2 = Transaction(Some(Id(1001)), userId, Debit(amount, UserSek), Credit(amount, Bank), bankRef.value, time)
        Transaction.findTransactions(userId) must_== Seq(t1, t2)
      }
    }


  }

}
