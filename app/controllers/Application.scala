package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.Logger

import views._
import java.util.UUID
import models._
import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.dispatch.Await

object Application extends Controller with Secured {

  val buyForm = Form(
    tuple(
      "amount" -> text,
      "address" -> text
    )
  )

  val sellForm = Form(
    tuple(
      "amount" -> text,
      "bank" -> text,
      "account" -> text
    )
  )

  // -- Actions

  /**
   * Home page
   */
  def home = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession(request)
      if (user.isDefined) {
        Ok(html.home(user.get))
      } else {
        BadRequest("Not logged in")
      }
  }

  /**
   * Direct buy
   * @return
   */
  def buy = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession(request)
      buyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.buy(user, formWithErrors)), {
        case (amount, address) => {
          val reference = UUID.randomUUID().hashCode().toHexString
          Logger.info("Buy %s: %s SEK -> %s, %s".format(user.get.email.value, amount, address, reference))
          Ok(html.pay(amount, address, reference))
        }
      }
      )
  }

  /**
   * Direct sell
   * @return
   */
  def sell = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession(request)
      sellForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.sell(user, formWithErrors)), {
        case (amount, bank, account) => {
          val address = "1x17"
          Logger.info("Sell %s: %s BTC -> %s, %s:%s".format(user.get.email.value, amount, address, bank, account))
          Ok(html.receive(amount, address, bank, account))
        }
      }
      )
  }


  val orderForm = Form(
    tuple(
      "amount" -> text,
      "price" -> text
    )
  )

  /**
   * PLace a bid order
   * @return
   */
  def bid = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession.get
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
  }

  /**
   * Place a ask order
   * @return
   */
  def ask = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession.get
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
  }

  /**
   * List all active orders for a user
   * @return
   */
  def userOrders = Action {
    implicit request =>
      val user = PlayActorService.getUserInSession
      if (user.isDefined) {
        val userId = user.get.userId
        implicit val timeout = PlayActorService.timeout
        val future = akka.pattern.ask(PlayActorService.orderBookActor, ListOrders(userId)).mapTo[Orders[BTC, SEK]]
        val orders = Await.result(future, PlayActorService.duration)
        Ok(html.userorders(user, orders.askOrders, orders.bidOrders))
      } else {
        Unauthorized
      }
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



