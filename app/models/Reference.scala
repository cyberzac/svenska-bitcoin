package models

abstract class Reference() {
  val value: String
}

case class BankReference(value: String) extends Reference

case class OrderReference(value: String) extends Reference

// Todo use orderId

case class CourtageReference(value: String) extends Reference

case class BtcAddressReference(value: String) extends Reference
