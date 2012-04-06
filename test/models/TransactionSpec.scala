package models

import org.specs2.mutable.Specification
import org.specs2.execute.Pending
import org.joda.time.DateTime
import anorm.Id

class TransactionSpec extends Specification {

  "A transaction" should {
    "have a dateTime" in {
      val time = System.currentTimeMillis()
      val transaction = Transaction(Id(0), UserId("userId"), SEK(10), BTC(0), "note", time)
       transaction.dateTime must_== new DateTime(time)
    }

  }

}
