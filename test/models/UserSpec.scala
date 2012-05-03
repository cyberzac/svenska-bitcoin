package models

import org.specs2.mutable.Specification
import play.api.test.FakeApplication
import play.api.test.Helpers._

class UserSpec extends Specification {

  "A User " should {

    "have a create method" in running(FakeApplication()) {
      val user = User.create(Name("name1"), Email("mail1"), "password1")
      user.name must_== Name("name1")
      user.email must_== Email("mail1")
      user.password.equals("password1") must beTrue
      user.balance must_== Balance()
    }

    "Throw exception if trying to create new user the the same email" in running(FakeApplication()) {
      User.create(Name("name1"), Email("mail1"), "password1")
      User.create(Name("name2"), Email("mail1"), "password2") must throwAn[Exception]
    }

    "Find a user by email" in running(FakeApplication()) {
      val expected = User.create(Name("name1"), Email("mail1"), "password1")
      val actual = User.findByEmail(Email("mail1")).get
      verifyUser(Some(actual), expected)
    }

    "Return None for an unknown mail" in running(FakeApplication()) {
      val found = User.findByEmail(Email("mail1"))
      found must beNone
    }

    "Find a user by id" in running(FakeApplication()) {
      val created = User.create(Name("name1"), Email("mail1"), "password1")
      val found = User.findById(created.id.get)
      verifyUser(found, created)
    }

    "Return None for an unknown id" in running(FakeApplication()) {
      val found = User.findById(UserId(-1))
      found must beNone
    }

    "Update an existing user" in running(FakeApplication()) {
      val created = User.create("name1", "mail1", "password1")
      val changed = created.copy(email = Email("mail2"))
      val updated = User.update(changed)
      updated must_== changed
      User.findByEmail(Email("mail1")) must beNone
      User.findByEmail(Email("mail2")) must beSome(updated)
    }

    "Throw an IllegalArgumentException on update a non existing user" in running(FakeApplication()) {
      User.update(User(None, "name", "mail", "password", Balance())) must throwAn[IllegalArgumentException]
    }

      "Throw an exception when trying to update the email to another users email" in running(FakeApplication()){
        val created = User.create("name1", "mail1", "password1")
        User.create("name2", "mail2", "password2")
        val changed = created.copy(email = Email("mail2"))
        User.update(changed) must throwAn[Exception]
      }

      "Throw an exception when trying to update the userId" in running(FakeApplication()) {
        val user = User.create("name", "mail", "password")
        val user2 = User.create("name2", "mail2", "password2")
        val updated = user.copy(id = user2.id)
        User.update(updated) must throwAn[Exception]
      }

  }

  private def verifyUser(actual: Option[User], expected: User) = {
    actual must beSome[User]
    val user = actual.get
    user.id must_== expected.id
    user.name must_== expected.name
    user.email must_== expected.email
  }
}
