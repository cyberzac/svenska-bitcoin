package controllers

import play.api.mvc._
import play.api.data._
import play.Logger

import views._

object Application extends Controller {

  val buyForm = Form(
    of (
      "amount" -> text,
      "address" -> text
    )
  )

  val sellForm = Form(
    of (
      "amount" -> text,
      "bank" -> text,
      "account" -> text
    )
  )


  // -- Actions

  /**
   * Home page
   */
  def index = Action {
    Ok(html.index())
  }

  def about = Action {
    Ok(html.about())
  }

  def buy = Action {
    implicit request =>
      buyForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.buy(formWithErrors)), {
        case (amount, address) => {
          val reference = "4711"
          Logger.info("Buy: %s SEK -> %s, %s".format(amount, address, reference))
          Ok(html.pay(amount, address, reference))
        }
      }
      )
  }

  def sell = Action {
    implicit request =>
      sellForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.sell(formWithErrors)), {
        case (amount, bank, account) => {
          val address = "1x17"
          Logger.info("Sell: %s BTC -> %s, %s:%s".format(amount, address, bank, account))
          Ok(html.receive(amount, address, bank, account))
        }
      }
      )
  }

}

