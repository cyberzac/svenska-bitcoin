package models

sealed abstract class Currency[T] {
  // self: { def copy(amount:BigDecimal) : T } =>
  // def copy(amount:BigDecimal): T

  val value: BigDecimal

  def create(amount: BigDecimal): T //= this.copy(amount)

  def zero = create(0)

  def >(price: Currency[T]): Boolean = value > price.value

  def >=(price: Currency[T]): Boolean = value >= price.value

  def <(price: Currency[T]): Boolean = value < price.value

  def <=(price: Currency[T]): Boolean = value <= price.value

  def -(price: Currency[T]): T = create(value - price.value)

  def +(price: Currency[T]): T = create(value + price.value)

  def *(factor: BigDecimal): T = create(value * factor)

  def *(price: Currency[T]): T = price * value

  def /(factor: BigDecimal): T = create(value / factor)

  def /(price: Currency[T]): T = this / price.value

  def unary_-(): T = create(-value)

  def signum: Int = value.signum

  def min(price: Currency[T]) = create(value.min(price.value))

  def max(price: Currency[T]) = create(value.max(price.value))

  def rounded: String = "%.2f".format(value)

}


case class SEK(value: BigDecimal) extends Currency[SEK] {
  def create(amount: BigDecimal) = copy(amount);
}

case class EUR(value: BigDecimal) extends Currency[EUR] {
  def create(amount: BigDecimal) = copy(amount);
}

case class USD(value: BigDecimal) extends Currency[USD] {
  def create(amount: BigDecimal) = copy(amount);
}

case class BTC(value: BigDecimal) extends Currency[BTC] {
  def create(amount: BigDecimal) = copy(amount)
}

case class NMC(value: BigDecimal) extends Currency[NMC] {
  def create(amount: BigDecimal) = copy(amount)
}




