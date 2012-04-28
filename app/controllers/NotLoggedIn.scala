package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.Logger

import views._
import models._

object NotLoggedIn extends Controller {

  /**
   * Index page
   */
  def index = Action {
    request => {
      val user = PlayActorService.getUserInSession(request)
      Ok(html.index(user))
    }
  }

  def about = Action {
    request => {
      val user = PlayActorService.getUserInSession(request)
      Ok(html.about(user))
    }
  }

  def bitcoin = Action {
    request => {
      val user = PlayActorService.getUserInSession(request)
      Ok(html.bitcoin(user))
    }
  }


  val registerForm = Form(
    tuple(
      "email" -> text,
      "password" -> text,
      "password2" -> text
    ) verifying("L&ouml;senorden &auml;r olika", result => result match {
      case (email, password, password2) => password == password2
    })
  )

  // -- Authentication
  val loginForm = Form(
    tuple(
      "email" -> text,
      "password" -> text
    ) verifying("Felaktig inlogging", result => result match {
      case (email, password) => {
        Logger.debug("login %s:%s".format(email, password))
        PlayActorService.authenticate(Email(email), password)
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
          Redirect(routes.Application.home).withSession("email" -> form._1)
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
   * Handle register form submission.
   */
  def registerUser = Action {
    implicit request =>
      registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.register(formWithErrors)), {
        case (email, password, password2) =>
          Logger.info("Registering user %s, password %s".format(email, password))
          Redirect(routes.Application.home).withSession("email" -> email)
      }
      )
  }
}

/**
 * Provide security features
 */
trait Secured {

  /**
   * Retrieve the connected user email.
   */
  def username(request: RequestHeader) = request.session.get("email")

  /**
   * Action for authenticated users.
   */
  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
    user =>
      Action(request => f(user)(request))
  }

  /**
   * Redirect to login if the use in not authorized.
   */
  def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.NotLoggedIn.login())

}



