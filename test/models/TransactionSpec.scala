package models

import org.specs2.mutable.Specification
import org.joda.time.DateTime
import java.util.Date
import anorm.Id
import play.api.test.FakeApplication
import play.api.test.Helpers._
import models.Transaction.BankReference

class TransactionSpec extends Specification {

  "A transaction" should {

    val userId = UserId("userId")
    val reference = BankReference("123455")
    val time = System.currentTimeMillis()
    val amount = 125
    val sek = SEK(amount)

    "Create a add fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.create(userId, sek, reference, time)
        t must_== Transaction(Some(Id(1000)), Debit(amount, Bank, userId), Credit(amount, UserSek, userId), reference.value, time)
      }
    }

    "Create a subtract fund transaction" in {
      running(FakeApplication()) {
        val t = Transaction.create(userId, -sek, reference, time)
        t must_== Transaction(Some(Id(1000)), Debit(amount, UserSek, userId), Credit(amount, Bank, userId), reference.value, time)
      }
    }

    "Find a transaction by id" in {
      running(FakeApplication()) {
        val expected = Transaction.create(userId, sek, reference, time)
        val id = expected.id.get
        val actual = Transaction.findTransaction(id).get
        actual must_== expected
      }
    }

    // Todo Throw an IllegalArgumentException if the transaction is not balanced.


    "Find all transactions by userId" in {
      running(FakeApplication()) {
        Transaction.create(userId, sek, reference, time)
        Transaction.create(userId, -sek, reference, time)
        val t1 = Transaction(Some(Id(1000)), Debit(amount, Bank, userId), Credit(amount, UserSek, userId), reference.value, time)
        val t2 = Transaction(Some(Id(1001)), Debit(amount, UserSek, userId), Credit(amount, Bank, userId), reference.value, time)
        Transaction.findTransactions(userId) must_== Seq(t1, t2)
      }
    }

    "have a dateTime" in {
      val transaction = Transaction(Some(Id(1000)), Debit(100, Bank, userId), Credit(100, UserBitcoin, userId), reference.value, time)
      transaction.dateTime must_== new DateTime(time)
    }

    "have a date" in {
      val transaction = Transaction(Some(Id(1000)), Debit(100, Bank, userId), Credit(100, UserBitcoin, userId), reference.value, time)
      transaction.date must_== new Date(time)
    }

  }

}
