package models

trait TradeService[A <: Currency[A], P <: Currency[P]] {


  /**
   * Stores a trade
   */
  def store(trade:Trade[A,  P])


  /**
   *Sums all trades for a user
   */
  def sumByUser(userId:UserId): Balance

}

