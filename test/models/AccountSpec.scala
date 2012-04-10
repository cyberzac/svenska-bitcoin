package models

import org.specs2.mutable.Specification

class AccountSpec extends Specification {

  "A Bank asset" should {
    "Be created from number 1000" in {
      val account = Account(1000)
      account must_== Bank
    }
    "Have the number 1000" in {
      Bank.number must_== 1000
    }
  }

  "An UserSek liablibity" should {
    "Be created from number 2200" in {
      val account = Account(2200)
      account must_== UserSek
    }
    "Have the number 2200" in {
      UserSek.number must_== 2200
    }
  }

}
