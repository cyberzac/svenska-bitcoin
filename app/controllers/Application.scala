package controllers

import play.api.mvc._
import play.api.data._
import play.Logger

import views._

object Application extends Controller {

  val registerForm = Form(
    of(
      "name" -> text,
      "email" -> email
  )
  )

  // -- Actions

  /**
   * Home page
   */
  def index = Action {
    Ok(html.index(registerForm))
  }

  /**
   * Handles the form submission.
   */
  def register = Action {
    implicit request =>
      registerForm.bindFromRequest.fold(
      formWithErrors => BadRequest(html.index(formWithErrors)), {
        case (name, email) => {
          Logger.info("%s, %s".format(name, email))
          Ok(html.registered(name))
        }
      }
      )
  }


}

