package models

import org.specs2.mutable.Specification

class InMemoryUserServiceSpec extends Specification {

  "inMemoryUserService" should {
    val dut = InMemoryUserService()

    "have a create users" in {
      dut.empty()
      val user = dut.create(Name("name1"), Email("mail1"), "password1")
      user.name must_== Name("name1")
      user.email must_== Email("mail1")
      user.password.equals("password1") must_== true
      user.balance must_== Balance()
    }

    "Throw execpetion if trying to create new user the the same email" in {
      dut.empty()
      dut.create("name1", "mail1", "password1")
      dut.create("name2", "mail1", "password2") must throwAn[IllegalArgumentException]
    }

    "Find a user by email" in {
      dut.empty()
      val created = dut.create("name1", "mail1", "password1")
      val found = dut.findByEmail(Email("mail1"))
      found.isDefined must_== true
      found.get must_== created
    }

    "Return None for an unknown mail" in {
      dut.empty()
      val found = dut.findByEmail(Email("mail1"))
      found.isDefined must_== false
    }

    "Find a user by id" in {
      dut.empty()
      val created = dut.create("name1", "mail1", "password1")
      val found = dut.findById(created.userId)
      found.isDefined must_== true
      found.get must_== created
    }

    "Return None for an unknown id" in {
      dut.empty()
      val found = dut.findById(UserId("not found"))
      found.isDefined must_== false
    }

    "Update an existing user" in {
      dut.empty()
      val created = dut.create("name1", "mail1", "password1")
      val changed = created.copy(email = Email("mail2"))
      val updated = dut.update(changed)
      updated must_== changed
      dut.findByEmail(Email("mail1")) must beNone
      dut.findByEmail(Email("mail2")) must beSome(updated)
    }

    "Update a non existing user" in {
      dut.empty()
      val user = User("name", "mail", "1", "password")
      dut.update(user)
      dut.findByEmail("mail") must beSome(user)
      dut.findById("1") must beSome(user)
    }

    "Throw an excpetion when trying to update the email to another users email" in {
      dut.empty()
      val created = dut.create("name1", "mail1", "password1")
      dut.create("name2", "mail2", "password2")
      val changed = created.copy(email = Email("mail2"))
      dut.update(changed) must throwAn[IllegalArgumentException]
    }

    "Throw an exception when trying to update the userId" in {
      dut.empty()
      val user = User("name", "mail", "1", "password")
      dut.update(user)
      val updated = user.copy(userId = "2")
      dut.update(updated) must throwAn[IllegalArgumentException]
    }

  }
}
