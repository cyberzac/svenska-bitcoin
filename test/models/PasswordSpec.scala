package models

import org.specs2.mutable.Specification

class PasswordSpec extends Specification {
  "A Password " should {
    val clear = "passwd"
    val password = Password(clear)

    "hash the password" in {
      password.hashed must_!= clear
    }
    "add a salt" in {
      password.salt.size must_== Password.saltSize
    }
    "return true on compare with right password" in {
      password.equals(clear) must_== true
    }
    "return false on compare with the wrong password" in {
      password.equals("wrong") must_== false
    }
    "have a toHexString method" in {
      password.toHexString.isEmpty must_== false
    }
  }
}