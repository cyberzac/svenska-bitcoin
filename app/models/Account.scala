package models

object Account {
  def apply(number: Int): Account = number match {
    case 1100 => BankBtc
    case 1200 => BankSek
    case 2100 => UserBtc
    case 2200 => UserSek
    case 2300 => UserReservedBtc
    case 2400 => UserReservedSek
    case _ => throw new IllegalArgumentException("Unknown account")
  }

}

sealed abstract class Account(val number: Int)

trait Asset

trait Liability

trait Income

trait Expense

trait Equity

case object BankBtc extends Account(1100) with Asset

case object BankSek extends Account(1200) with Asset

case object UserBtc extends Account(2100) with Liability

case object UserSek extends Account(2200) with Liability

case object UserReservedBtc extends Account(2300) with Liability

case object UserReservedSek extends Account(2400) with Liability

abstract class DebitCredit(amount: BigDecimal, account: Account)

case class Credit(amount: BigDecimal, account: Account) extends DebitCredit(amount, account)

case class Debit(amount: BigDecimal, account: Account) extends DebitCredit(amount, account)





