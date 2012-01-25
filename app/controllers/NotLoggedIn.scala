package controllers

import play.api.mvc._
import play.api.data._
import play.Logger

import views._
import models.PlayUserService

object NotLoggedIn extends Controller {

  /**
   * Index page
   */
  def index = Action {
    request => {
      val user = PlayUserService.getUserInSession(request)
      Ok(html.index(user))
    }
  }

  def about = Action {
    request => {
      val user = PlayUserService.getUserInSession(request)
      Ok(html.about(user))
    }
  }

  def bitcoin = Action {
    request => {
      val user = PlayUserService.getUserInSession(request)
      Ok(html.bitcoin(user))
    }
  }


  val registerForm = Form(
    of(
      "email" -> text,
      "password" -> text,
      "password2" -> text
    ) verifying("L&ouml;senorden &auml;r olika", result => result match {
      case (email, password, password2) => password == password2
    })
  )

  // -- Authentication
  val loginForm = Form(
    of(
      "email" -> text,
      "password" -> text
    ) verifying("Felaktig inlogging", result => result match {
      case (email, password) => {
        Logger.debug("login %s:%s".format(email, password))
        PlayUserService.getUserActor(email, password).isDefined
      }
    })
  )

  /**
   * Login page.
   */
  def login = Action {
    implicit request =>
      Ok(html.login(loginForm))
  }

  /**
   * Handle login form submission.
   */
  def authenticate = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(html.login(formWithErrors)),
        form => {
          Redirect(routes.Application.home()).withSession("email" -> form._1)
        }
      )
  }

  /**
   * Registration page
   */
  def register = Action {
    implicit request =>
      Ok(html.register(registerForm))
  }

  /**
   * Handle login form submission.
   */
  def registerUser = Action {
    implicit request =>
      registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.register(formWithErrors)), {
        case (email, password, password2) =>
          Logger.info("Registering user %s, password %s".format(email, password))
          Redirect(routes.Application.home()).withSession("email" -> email)
      }
      )
  }
}

/**
 * Provide security features
 */
trait Secured extends Security.AllAuthenticated {

  /**
   * Retrieve the connected user email.
   */
  override def username(request: RequestHeader) = request.session.get("email")

  /**
   * Redirect to login if the use in not authorized.
   */
  override def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.NotLoggedIn.login())

}



