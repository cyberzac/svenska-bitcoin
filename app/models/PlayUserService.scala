package models

import org.bitcex.userservice.{InMemoryUserService, UserService}
import akka.actor.{ActorRef, TypedActor}
import akka.actor.Actor._
import org.bitcex.model.{UserActor, Email}

object PlayUserService {

  val userService = TypedActor.newInstance(classOf[UserService], classOf[InMemoryUserService], 1000)

  // Todo: Make this a LRU cache
  var userActors = Map[Email, ActorRef]()

  def authenticate(email:Email,  password:String): Boolean = {
    getUserActor(email,password).isDefined
  }

  def getUserActor(email: Email, password: String): Option[ActorRef] = {
    val user = userService.findByEmail(email).getOrElse(return None)
    if (!user.password.equals(password)) {
      return None
    }
    val userRef = userActors.get(email)
    if (userRef.isDefined) {
      return userRef
    }
    val actorRef = actorOf(new UserActor(user))
    userActors = userActors + (email -> actorRef)
    Some(actorRef)
  }

}