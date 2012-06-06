package models

import org.joda.time.DateTime
import anorm.SqlParser._
import java.util.Date
import play.api.db._
import play.api.Play.current
import anorm._
import models.Transaction._
import scala.Predef._

case class Trade[A <: Currency[A], P <: Currency[P]](id: Option[Pk[Long]], amount: A, price: P, sellerId: UserId, buyerId: UserId, time: Long) {

  def tradeId = TradeId(id.getOrElse(throw new IllegalStateException("Trade not stored")).get)

  val dateTime = new DateTime(time)

  def date: Date = dateTime.toDate

  val total = price * amount.value
}

case class TradeId(value:Long)

object Trade {

  /**
   * Parse a User from a ResultSet
   */
  val simple = {
    get[Pk[Long]]("trade.id") ~
      get[java.math.BigDecimal]("trade.amount") ~
      get[java.math.BigDecimal]("trade.price") ~
      get[Long]("trade.seller_id") ~
      get[Long]("trade.buyer_id") ~
      get[Date]("trade.created_date") map {
      case id ~ amount ~ price ~ sellerId ~ buyerId ~ created => {
        Trade(
          id = Some(id),
          amount = BTC(amount),
          price = SEK(price),
          sellerId = sellerId,
          buyerId = buyerId,
          time = created.getTime
        )
      }
    }
  }

  def apply[A <: Currency[A], P <: Currency[P]](ask: AskOrder[A, P], bid: BidOrder[A, P], price: P): Trade[A, P] = {
    if (ask.price > bid.price) throw new IllegalArgumentException("Bid price must be equal or higher then ask")
    val amount = ask.amount.min(bid.amount)
    val time = System.currentTimeMillis()
    create(Trade(None, amount, price, ask.sellerId, bid.buyerId, time))
  }

  def apply[A <: Currency[A], P <: Currency[P]](amount: A, price: P, sellerId: UserId, buyerId: UserId, time: Long = System.currentTimeMillis()): Trade[A, P] = {
    create(Trade(None, amount, price, sellerId, buyerId, time))
  }

  def getTrades(userId: UserId): scala.List[Trade[BTC, SEK]] = {
    DB.withConnection {
      implicit connection =>
        SQL("select * from trans").as(Trade.simple *)
    }
  }


  def create[A <: Currency[A], P <: Currency[P]](trade: Trade[A, P]): Trade[A, P] = {
    log.info("Creating trade {}", trade)
    DB.withConnection {
      implicit connection =>

      // Get the trans id
        val id: Long = SQL("select next value for trade_seq").as(scalar[Long].single)

        SQL(
          """
            insert into trade values (
              {id},
              {amount},
              {price},
              {sellerId},
              {buyerId},
              {created}
            )
          """
        ).on(
          'id -> id,
          'amount -> new java.math.BigDecimal(trade.amount.value.toString()),
          'price -> new java.math.BigDecimal(trade.price.value.toString()),
          'sellerId -> trade.sellerId.value,
          'buyerId -> trade.buyerId.value,
          'created -> trade.date
        ).executeUpdate()
        val t: Trade[A, P] = trade.copy(id = Some(Id(id)))
        log.debug("Stored trade {}", t)
        t
    }
  }
}
