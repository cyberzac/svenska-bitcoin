package models

import java.lang.IllegalArgumentException
import org.slf4j.LoggerFactory

object InMemoryUserService {

  def apply(): InMemoryUserService = {
    new InMemoryUserService(0)
  }
}

class InMemoryUserService(var userId: Int) extends UserService {
  val log = LoggerFactory.getLogger(getClass)
  var users = Map[UserId, User]()

  def empty() {
    users = users.empty
  }

  def remove(user: User): Boolean = false

  def update(updatedUser: User): User = {
    val updatedEmail = updatedUser.email
    val previous = users.values.find(_.email == updatedEmail)
    if (previous.isDefined) {
      if (previous.get.userId != updatedUser.userId) throw new IllegalArgumentException("Email %s is already in use".format(updatedEmail))
    }
    users = users + (updatedUser.userId -> updatedUser)
    log.debug("User {} updated to {}", updatedUser.userId, updatedUser)
    updatedUser
  }

  def findByEmail(email: Email): Option[User] = {
    val user = users.values.find(_.email == email)
    log.debug("findByEmail: {}", if (user.isDefined) user else "No user with email %s".format(email))
    user
  }

  def findById(userId: UserId): Option[User] = {
    val user = users.get(userId)
    log.debug("findById: {}", if (user.isDefined) user else "No user with userId %s".format(userId))
    user
  }

  def create(name: Name, email: Email, clear: String): User = {
    if (findByEmail(email).isDefined) throw new IllegalArgumentException("%s is already in use".format(email))
    userId += 1
    val user = User(name, email, UserId(userId.toString), Password(clear))
    users = users + (user.userId -> user)
    log.info("Created user {}", user)
    user
  }

}