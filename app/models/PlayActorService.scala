package models

import play.api.mvc.{Request, AnyContent}
import akka.actor.{Props, ActorSystem, ActorRef}
import org.slf4j.LoggerFactory
import akka.util.Timeout
import akka.util.duration._

object PlayActorService {


  implicit val timeout = Timeout(30000)
  val duration = timeout.duration
  // Todo use play.akka
  val system = ActorSystem("SvenskaBitcoinSystem")
  val userService: UserService = InMemoryUserService()
  val tradeService = InMemoryTradeService[BTC, SEK]()
  val falloutActor = system.actorOf(Props[FalloutActor])
  val orderBookActor = system.actorOf(Props(new OrderBookActor[BTC, SEK](tradeService)), "orderBook")
  val log = LoggerFactory.getLogger(this.getClass)

  // Todo: Make this a LRU cache
  var userActors = Map[Email, ActorRef]()

  def authenticate(email: Email, password: String): Boolean = {
    userService.findByEmail(email).map { user =>
    user.password.equals(password)
    }.getOrElse(false)
  }

  def getUserByEmail(email: Email): Option[User] = {
    userService.findByEmail(email).map(updateBalance(_)).getOrElse(return None)
  }

  def getUserActor(userId: UserId): ActorRef = {
    val user = userService.findById(userId).getOrElse({
      return falloutActor
    })
    getUserActor(user)
  }


  def getUserActor(email: Email): ActorRef = {
    val user = userService.findByEmail(email).getOrElse(return falloutActor)
    getUserActor(user)
  }

  def getUserActor(user: User): ActorRef = {
    userActors.getOrElse(user.email, {
      val actorRef = system.actorOf(Props(new UserActor(user.userId, userService)), user.userId.value)
      userActors = userActors + (user.email -> actorRef)
      actorRef
    })
  }

  def updateBalance(user: User): Option[User] = {
    val balance = tradeService.sumByUser(user.userId)
    log.debug("Sum of trades are {}", balance)
    Some(user.copy(balance = balance))
  }

  // Todo remove
  def getUserInSession(implicit request: Request[AnyContent]): Option[User] = {
    val email = request.session.get("email").getOrElse(return None)
    val user = userService.findByEmail(Email(email)).getOrElse(return None)
    log.debug("Found user {} in session", user)
    updateBalance(user)
  }

  def getUserActorInSession(implicit request: Request[AnyContent]): Option[ActorRef] = {
    val email = request.session.get("email").getOrElse(return None)
    userActors.get(Email(email))
  }

  def create(name: Name, email: Email, password: String): User = {
    userService.create(name, email, password)
  }

  def getTrades(user: User): List[Trade[BTC, SEK]] = tradeService.getTrades(user.userId)

  def getUserTrades(user:User):List[UserTrade[BTC,SEK]] = getTrades(user) map (_.toUserTrade(user.userId))
}