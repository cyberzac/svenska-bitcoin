package models

import org.specs2.mutable.Specification

class PasswordSpec extends Specification {
  "A Password " should {
    val clear = "passwd"
    val password = Password(clear)

    "return true on compare with right password" in {
      password.equals(clear) must_== true
    }

    "return false on compare with the wrong password" in {
      password.equals("wrong") must_== false
    }

    "be constructed from a digest "in {
      Password(password.digest).equals(clear) must_== true }
  }
}