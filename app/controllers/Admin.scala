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

object Admin extends Controller with Secured {

  /**
   * Home page
   */
  def index = IsAuthenticated {
    username => _ =>
      PlayActorService.getAdminByEmail(Email(username)).map {
        admin =>
          Ok(html.admin.index(admin))
      }.getOrElse(Forbidden)
  }

  /**
   * All Transactions
   */
  def transactions = IsAuthenticated {
    username => _ =>
      PlayActorService.getAdminByEmail(Email(username)).map {
        admin =>
          Ok(html.admin.transactions(admin, Transaction.findAll))
      }.getOrElse(Forbidden)
  }

  /**
   * All users
   */
  def users = TODO

  /**
   * User not allowed to admin
   */
  def forbidden = Action {
    Forbidden
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



