package controllers

import play.api.mvc._
import play.api.data._

import views._

object NotLoggedIn extends Controller {

  /**
   * Index page
   */
  def index = Action {
    Ok(html.index())
  }

  def about = Action {
    Ok(html.about())
  }

  def bitcoin = Action {
    Ok(html.bitcoin())
  }

// -- Authentication

  val loginForm = Form(
    of(
      "email" -> text,
      "password" -> text
    ) verifying("Invalid email or password", result => result match {
      case (email, password) => email == password
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
        user => Redirect(routes.Application.home).withSession("email" -> user._1)
      )
  }

  /**
   * Logout and clean the session.
   */
  def logout = Action {
    Redirect(routes.NotLoggedIn.login).withNewSession.flashing(
      "success" -> "You've been logged out"
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
  override def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.NotLoggedIn.login)

}



