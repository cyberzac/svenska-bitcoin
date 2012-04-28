package models

import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test.FakeApplication

class PlayActorServiceSpec extends Specification {


  "A PlayActorService" should {
    val dut = PlayActorService

    "Return an ActorRef for an userId of an existing ussr" in running(FakeApplication()) {
      val user = dut.create("name", "email", "password")
      dut.getUserActor(user.userId) must_!= dut.falloutActor
    }

    "Return the FalloutActor for an userId of a non-existing user" in running(FakeApplication()) {
      dut.getUserActor(UserId(-1)) must_== dut.falloutActor
    }

    "'Return an ActorRef for an email of an existing user" in running(FakeApplication()) {
      val user = dut.create("name", "email", "password")
      dut.getUserActor(user.email) must_!= dut.falloutActor
    }

    "Return the FallOutActor for an email of a non-existing user" in running(FakeApplication()) {
      dut.getUserActor(Email("void")) must_== dut.falloutActor
    }


  }

}
