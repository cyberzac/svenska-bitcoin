package controllers

import play.api.mvc._
import play.api.data._
import play.Logger

import views._
import models.PlayUserService
import java.util.UUID

object Application extends Controller with Secured {

  val buyForm = Form(
    of(
      "amount" -> text,
      "address" -> text
    )
  )

  val sellForm = Form(
    of(
      "amount" -> text,
      "bank" -> text,
      "account" -> text
    )
  )

  // -- Actions

  /**
   * Home page
   */
  def home = Action { request =>
    val user = PlayUserService.getUserInSession(request)
    Ok(html.home(user))
  }

  def buy = Action {
    implicit request =>
      val user = PlayUserService.getUserInSession(request)
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

  def sell = Action {
    implicit request =>
      val user = PlayUserService.getUserInSession(request)
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

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.NotLoggedIn.index()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

}



