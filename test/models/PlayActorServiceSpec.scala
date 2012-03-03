package models

import org.specs2.mutable.Specification

class PlayActorServiceSpec extends Specification {

  val dut = PlayActorService
  val user = dut.create("name", "email", "password")

  "A PlayActorService" should {

    "Return an ActorRef for an userId of an existing ussr" in {
      dut.getUserActor(user.userId) must_!= dut.falloutActor
    }

  "Return the FalloutActor for an userId of a non-existing user" in {
       dut.getUserActor(UserId("void")) must_== dut.falloutActor
    }

    "'Return an ActorRef for an email of an existing user" in {
       dut.getUserActor(user.email) must_!= dut.falloutActor
    }

    "Return the FallOutActor for an email of a non-existing user" in {
      dut.getUserActor(Email("void")) must_== dut.falloutActor
    }



  }

}
