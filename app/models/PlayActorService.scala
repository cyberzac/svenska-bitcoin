package models

import play.api.mvc.{Request, AnyContent}
import akka.actor.{Props, ActorSystem, ActorRef}
import org.slf4j.LoggerFactory
import akka.util.Timeout

object PlayActorService {

  implicit val timeout = Timeout(30000)
  val duration = timeout.duration
  // Todo use play.akka
  val system = ActorSystem("SvenskaBitcoinSystem")
  val falloutActor = system.actorOf(Props[FalloutActor])
  val orderBookActor = system.actorOf(Props(new OrderBookActor[BTC, SEK]()), "orderBook")
  val log = LoggerFactory.getLogger(this.getClass)

  // Todo: Make this a LRU cache
  var userActors = Map[Email, ActorRef]()

  def authenticate(email: Email, password: String): Boolean = {
    User.findByEmail(email).map {
      user =>
        user.password.equals(password)
    }.getOrElse(false)
  }

  def getUserByEmail(email: Email): Option[User] = {
    User.findByEmail(email).map(updateBalance(_)).getOrElse(return None)
  }

  def getAdminByEmail(email: Email): Option[AdminUser] = {
    val user = User.findByEmail(email).getOrElse(return None)
    if (user.isAdmin) {
      return Some(AdminUser(user))
    }
    None
  }

  def getUserActor(userId: UserId): ActorRef = {
    val user = User.findById(userId).getOrElse(return falloutActor)
    getUserActor(user)
  }


  def getUserActor(email: Email): ActorRef = {
    val user = User.findByEmail(email).getOrElse(return falloutActor)
    getUserActor(user)
  }

  def getUserActor(user: User): ActorRef = {
    userActors.getOrElse(user.email, {
      val actorRef = system.actorOf(Props(new UserActor(user.userId)), user.userId.value.toString)
      userActors = userActors + (user.email -> actorRef)
      actorRef
    })
  }

  def updateBalance(user: User): Option[User] = {
    val balance = Transaction.balance(user.userId)
    log.debug("Sum of trades are {}", balance)
    Some(user.copy(balance = balance))
  }

  // Todo remove
  def getUserInSession(implicit request: Request[AnyContent]): Option[User] = {
    val email = request.session.get("email").getOrElse(return None)
    val user = User.findByEmail(Email(email)).getOrElse(return None)
    log.debug("Found user {} in session", user)
    updateBalance(user)
  }

  def getUserActorInSession(implicit request: Request[AnyContent]): Option[ActorRef] = {
    val email = request.session.get("email").getOrElse(return None)
    userActors.get(Email(email))
  }

  //Todo remove
  def create(name: Name, email: Email, password: String): User = {
    User.create(name, email, password)
  }

  def getTrades(user: User): List[Trade[BTC, SEK]] = Trade.getTrades(user.userId)

  def getUserTransactions(user: User): List[Transaction] = null
}