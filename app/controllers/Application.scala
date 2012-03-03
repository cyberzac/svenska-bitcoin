package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.Logger

import views._
import java.util.UUID
import models._
import akka.pattern.ask
import akka.dispatch.Await

object Application extends Controller with Secured {

  /**
   * Home page
   */
  def home = IsAuthenticated {
    username => _ =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          Ok(html.home(user))
      }.getOrElse(Forbidden)
  }

  val orderForm = Form(
    tuple(
      "amount" -> text,
      "price" -> text
    )
  )

  /**
   * Place a bid order
   * @return
   */
  def bid = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          orderForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.bid(user, formWithErrors)), {
            case (amountStr, priceStr) => {
              val amount = BigDecimal(amountStr)
              val price = BigDecimal(priceStr)
              val order = BidOrderSEK(BTC(amount), SEK(price), user.userId)
              PlayActorService.orderBookActor ! order
              Redirect("userOrders")
            }
          }
          )
      }.getOrElse(Forbidden)
  }

  /**
   * Place a ask order
   * @return
   */
  def ask = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          orderForm.bindFromRequest.fold(
          formWithErrors => BadRequest(html.ask(user, formWithErrors)), {
            case (amountStr, priceStr) => {
              val amount = BigDecimal(amountStr)
              val price = BigDecimal(priceStr)
              val order = AskOrderSEK(BTC(amount), SEK(price), user.userId)
              PlayActorService.orderBookActor ! order
              Redirect("userOrders")
            }
          }
          )
      }.getOrElse(Forbidden)
  }


  /**
   * List all trades
   */
  def trades = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          val trades = PlayActorService.getUserTrades(user)
          Ok(html.trades(Some(user), trades))
      }.getOrElse(Forbidden)
  }

/**
   * List all active orders for a user
   * @return
   */
  def userOrders = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          val userId = user.userId
          implicit val timeout = PlayActorService.timeout
          val future = akka.pattern.ask(PlayActorService.orderBookActor, ListOrders(userId)).mapTo[Orders[BTC, SEK]]
          val orders = Await.result(future, PlayActorService.duration)
          Ok(html.userorders(Some(user), orders.askOrders, orders.bidOrders))
      }.getOrElse(Forbidden)
  }

  /**
   * List all active orders in the order book.
   * @return
   */
  def orders = Action {
    implicit request =>
      implicit val timeout = PlayActorService.timeout
      val future = (PlayActorService.orderBookActor ? ListOrders).mapTo[Orders[BTC, SEK]]
      val orders = Await.result(future, PlayActorService.duration)
      val user = PlayActorService.getUserInSession
      Ok(html.userorders(user, orders.askOrders, orders.bidOrders))
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.NotLoggedIn.index()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

}



