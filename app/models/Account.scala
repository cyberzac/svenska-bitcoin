package models

object Account {
  def apply(number: Int): Account = number match {
    case 1000 => Bank
    case 2100 => UserBitcoin
    case 2200 => UserSek
    case _ => throw new IllegalArgumentException("Unknown account")
  }

}

sealed abstract class Account(val number: Int)

trait Asset

trait Liability

trait Income

trait Expense

trait Equity

case object Bank extends Account(1000) with Asset

case object UserBitcoin extends Account(2100) with Liability

case object UserSek extends Account(2200) with Liability

abstract class DebitCredit(amount: BigDecimal, account: Account)

case class Credit(amount: BigDecimal, account: Account) extends DebitCredit(amount, account)

case class Debit(amount: BigDecimal, account: Account) extends DebitCredit(amount, account)





