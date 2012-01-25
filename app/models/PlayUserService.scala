package models

import org.bitcex.userservice.{InMemoryUserService, UserService}
import akka.actor.{ActorRef, TypedActor}
import akka.actor.Actor._
import org.bitcex.model._
import play.api.mvc.{Request, AnyContent}
import play.api.Play

object PlayUserService {

  val userServiceActor = TypedActor.newInstance(classOf[UserService], classOf[InMemoryUserService], 1000)

  //Play.configuration.get("user.1.name")
  userServiceActor.create(Name("Martin Zachrison"), Email("zac@cyberzac.se"), "secret", SEK(100), BTC(100))
  userServiceActor.create(Name("Mats Henricson"), Email("mats@henricson.se"), "secret", SEK(100), BTC(100))
  userServiceActor.create(Name("Jarl Fransson"), Email("jarl@acm.org"), "secret", SEK(100), BTC(100))
  userServiceActor.create(Name("Olle Kullberg"), Email("olle.kullberg@gmail.com"), "secret", SEK(100), BTC(100))

  // Todo: Make this a LRU cache
  var userActors = Map[Email, ActorRef]()

  def authenticate(email:Email,  password:String): Boolean = {
    getUserActor(email,password).isDefined
  }

  def getUserActor(email: Email, password: String): Option[ActorRef] = {
    val user = userServiceActor.findByEmail(email).getOrElse(return None)
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

  def getUserInSession(implicit request: Request[AnyContent]): Option[User] =  {
    val email = request.username.getOrElse(return None)
    userServiceActor.findByEmail(email)
  }

}