package models

import org.specs2.mutable.Specification

class AccountSpec extends Specification {

  "A BankSek asset" should {
    "Be created from number 1200" in {
      val account = Account(1200)
      account must_== BankSek
    }
    "Have the number 1200" in {
      BankSek.number must_== 1200
    }
  }

  "A BankBtc asset" should {
    "Be created from number 1100" in {
      val account = Account(1100)
      account must_== BankBtc
    }
    "Have the number 1100" in {
      BankBtc.number must_== 1100
    }
  }

  "An UserBtc liablibity" should {
    "Be created from number 2100" in {
      val account = Account(2100)
      account must_== UserBtc
    }
    "Have the number 2100" in {
      UserBtc.number must_== 2100
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

  "An UserReservedBtc liablibity" should {
    "Be created from number 2300" in {
      val account = Account(2300)
      account must_== UserReservedBtc
    }
    "Have the number 2100" in {
      UserReservedBtc.number must_== 2300
    }
  }

  "An UserReservedSek liablibity" should {
    "Be created from number 2400" in {
      val account = Account(2400)
      account must_== UserReservedSek
    }
    "Have the number 2400" in {
      UserReservedSek.number must_== 2400
    }
  }


}
