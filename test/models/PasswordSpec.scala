package models

import org.specs2.mutable.Specification

class PasswordSpec extends Specification {
  "A Password " should {
    val clear = "passwd"
    val password = Password(clear)

    "return true on compare with right password" in {
      password.matches(clear) must beTrue
    }

    "return false on compare with the wrong password" in {
      password.matches("wrong") must beFalse
    }

    "have a digest" in {
      password.digest.value must not beEmpty
    }

    "be constructed from a digest" in {
      Password(password.digest).matches(clear) must beTrue
    }

    "return false when compared with null" in {
      password.matches(null) must beFalse
    }

    "return true when comparing an empty password with empty" in {
      val emptyPassword = Password("")
      emptyPassword.matches("") must beTrue
    }
  }
}