package models

import org.specs2.mutable.Specification
import anorm.Id
import play.api.test.FakeApplication

//import play.api.test._

import play.api.test.Helpers._

class AnormTransactionServiceSpec extends Specification {

  "A AnormTransactionServiceSpec" should {

    val dut = new AnormTransactionService()
    val userId = UserId("userId")
    val account = BankAccount("123455")

    val transaction = Transaction(Id(1000), userId, SEK(10), BTC(0), account.account)


    "Create and store a transaction" in {
      running(FakeApplication()) {
        dut.create(transaction)
        dut.findTransactions(userId) must_== Seq(transaction)
      }
    }
    /* "Create and store an addtion of funds" in {

        dut.create(userId, SEK(10), account)
        val transactions = dut.findTransactions(userId)
        transactions.size must_== 1
        val t = transactions.head
        t.userId must_== userId
        t.sek must_== SEK(10)
        t.btc must_== BTC(0)
        t.note must_== account.account
      }
    }*/


    //    dut.sumByUser(userId) must_== new Balance(BTC(0), SEK(10))
  }

}
