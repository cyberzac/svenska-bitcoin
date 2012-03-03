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

// Todo login does not work, you're password is disregarded
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
  def home = IsAuthenticated {
    username => _ =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          Ok(html.home(user))
      }.getOrElse(Forbidden)
  }

  /**
   * Direct buy
   * @return
   */
  def buy = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          buyForm.bindFromRequest.fold(
          // Todo change view  buy to use User instead of Option[User]
          formWithErrors => BadRequest(html.buy(Some(user), formWithErrors)), {
            case (amount, address) => {
              val reference = UUID.randomUUID().hashCode().toHexString
              Logger.info("Buy %s: %s SEK -> %s, %s".format(user.email.value, amount, address, reference))
              Ok(html.pay(amount, address, reference))
            }
          }
          )
      }.getOrElse(Forbidden)
  }

  /**
   * Direct sell
   * @return
   */
  def sell = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          sellForm.bindFromRequest.fold(
          // Todo change view  buy to use User instead of Option[User]
          formWithErrors => BadRequest(html.sell(Some(user), formWithErrors)), {
            case (amount, bank, account) => {
              val address = "1x17"
              Logger.info("Sell %s: %s BTC -> %s, %s:%s".format(user.email.value, amount, address, bank, account))
              Ok(html.receive(amount, address, bank, account))
            }
          }
          )
      }.getOrElse(Forbidden)
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



