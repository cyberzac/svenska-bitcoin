package models

import anorm._
import anorm.SqlParser._
import java.util.Date
import play.api.db._
import play.api.Play.current
import models.Transaction._
import org.joda.time.DateTime


case class User(id: Option[Pk[Long]], name: Name, email: Email, password: Password, balance: Balance = Balance(), time: Long = System.currentTimeMillis()) {
  def isAdmin: Boolean = true

  //Todo add to constructor
  def userId = UserId(id.getOrElse(throw new IllegalStateException("user not stored")).get)

  def dateTime: DateTime = new DateTime(time)

  def date: Date = dateTime.toDate
}

case class Email(value: String)

case class Name(value: String)

case class UserId(value: Long)

object UserId {
val svenskaBitcoin = UserId(0)
}

object User {

  // -- Parsers

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("user.id") ~
      get[String]("user.name") ~
      get[String]("user.email") ~
      get[String]("user.password") ~
      get[Date]("user.created_date") map {
      case id ~ name ~ email ~ digest ~ created => {
        User(
          id = Some(id),
          name = Name(name),
          email = Email(email),
          password = Password(PasswordDigest(digest)),
          time = created.getTime
        )
      }
    }
  }

  // -- Queries

  /**
   * Creates a new user
   */
  def create(name: Name, email: Email, password: String): User = create(User(None, name, email, Password(password)))

  def create(user: User): User = {
    DB.withConnection {
      implicit connection =>

      // Get the trans id
        val id: Long = SQL("select next value for user_seq").as(scalar[Long].single)

        SQL(
          """
            insert into user values (
              {id},
              {name},
              {email},
              {password},
              {created}
            )
          """
        ).on(
          'id -> id,
          'name -> user.name.value,
          'email -> user.email.value,
          'password -> user.password.digest.value,
          'created -> user.date
        ).executeUpdate()
        val u = user.copy(id = Some(Id(id)))
        log.info("Stored user {}", u)
        u
    }
  }


  /**
   * Finds an existing user
   */
  def findById(id: Pk[Long]): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from user where id = {id} ").on(
          'id -> id
        ).as(User.simple.singleOpt)
    }
  }

  def findById(userId: UserId): Option[User] = findById(Id(userId.value))

  implicit def userId2Id(userId: UserId): Pk[Long] = Id(userId.value)

  implicit def id2userId(id: Pk[Long]): UserId = UserId(id.get)

  /**
   * Finds a user by email
   */
  def findByEmail(email: Email): Option[User] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from user where email = {email} ").on(
          'email -> email.value
        ).as(User.simple.singleOpt)
    }
  }

  /**
   * Updates a user
   */
  def update(user: User): User = {
    if (user.id.isEmpty) throw new IllegalArgumentException("Trying to update user not stored in database")
    DB.withConnection {
      implicit connection =>
        SQL(
          """
          update user set
          name = {name},
          email = {email},
          password = {password},
          created_date = {created}
          where id = {id}
            """).on(
          'id -> user.id,
          'name -> user.name.value,
          'email -> user.email.value,
          'password -> user.password.digest.value,
          'created -> user.date
        ).executeUpdate()
    }
    return user
  }

  /**
   * Removes a user
   */
  def remove(user: User): Boolean = {
    return false
  }

}

