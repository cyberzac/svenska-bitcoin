package controllers

import org.bitcex.OrderBookActor
import org.bitcex.model._
import akka.actor.Actor._

import play.api.mvc._
import play.api.data._

import views._
import models.PlayUserService
import org.bitcex.messages.{Orders, ListOrders}

object PlayOrderBook extends Controller with Secured {

  val orderBookActor = actorOf[OrderBookActor[BTC, SEK]].start()

  val orderForm = Form(
    of(
      "amount" -> text,
      "price" -> text
    )
  )


  def bid = Action {
    implicit request =>
      val userRef = PlayUserService.getUserActorInSession(request).get
      val user = PlayUserService.getUserInSession.get
      orderForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.bid(user, formWithErrors)), {
        case (amountStr, priceStr) => {
          val amount = BigDecimal(amountStr)
          val price = BigDecimal(priceStr)
          val order = BidOrderSEK(BTC(amount), SEK(price), userRef)
          orderBookActor ! order
          Redirect("userOrders")
        }
      }
      )
  }

  def ask = Action {
    implicit request =>
      val userRef = PlayUserService.getUserActorInSession(request).get
      val user = PlayUserService.getUserInSession.get
      orderForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.ask(user, formWithErrors)), {
        case (amountStr, priceStr) => {
          val amount = BigDecimal(amountStr)
          val price = BigDecimal(priceStr)
          val order = AskOrderSEK(BTC(amount), SEK(price), userRef)
          orderBookActor ! order
          Redirect("userOrders")
        }
      }
      )
  }

  def userOrders = Action {
    implicit request =>
      val userRef = PlayUserService.getUserActorInSession(request).get
      val user = PlayUserService.getUserInSession.get
      val future = orderBookActor ? ListOrders(userRef)
      val orders = future.get.asInstanceOf[Orders[BTC, SEK]]
      Ok(html.userorders(user, orders.askOrders, orders.bidOrders))
  }

}