package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._
import akka.pattern.ask
import akka.dispatch.Await
import org.slf4j.LoggerFactory

object Application extends Controller with Secured {

 val log = LoggerFactory.getLogger(getClass)
  /**
   * Home page
   */
  def home = IsAuthenticated {
    username => _ =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          Ok(html.home(user))
      }.getOrElse(Unauthorized)
  }

  val orderForm = Form(
    tuple(
      "amount" -> text,
      "price" -> text
    )
  )

  /**
   * Display a bid order form
   */
  def bidOrder = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user => Ok(html.bid(user, orderForm))
      }.getOrElse(Unauthorized)
  }

  /**
   * Place a bid order
   * @return
   */
  def placeBidOrder = IsAuthenticated {
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
              Redirect("/order")
            }
          }
          )
      }.getOrElse(Unauthorized)
  }

  /**
   * Display  ask order form
   */
  def askOrder = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user => Ok(html.ask(user, orderForm))
      }.getOrElse(Unauthorized)
  }

  /**
   * Place a ask order
   * @return
   */
  def placeAskOrder = IsAuthenticated {
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
              Redirect("/order")
            }
          }
          )
      }.getOrElse(Unauthorized)
  }


  /**
   * List all trades
   */
  def trades = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user =>
          val trades = PlayActorService.getTrades(user)
          Ok(html.trades(Some(user), trades))
      }.getOrElse(Unauthorized)
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
      }.getOrElse(Unauthorized)
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



