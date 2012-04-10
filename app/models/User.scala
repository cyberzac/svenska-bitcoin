package models

case class User(name: Name, email: Email, userId: UserId, password: Password, balance:Balance = Balance())

case class Email(value: String)

case class Name(value: String)

// Todo change to Int
case class UserId(value: String)

