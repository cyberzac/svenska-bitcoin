package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

import views._
import models._
import akka.pattern.ask
import akka.dispatch.Await

object Funds extends Controller with Secured {

  /**
   * General funds info
   */
  def index = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user => Ok(html.funds.index(user))
      }.getOrElse(Forbidden)
  }

  /**
   * Display a add funds form
   */
  def add = IsAuthenticated {
    username => implicit request =>
      PlayActorService.getUserByEmail(Email(username)).map {
        user => Ok(html.funds.addSEK(user))
      }.getOrElse(Forbidden)
  }

  /**
   * Order add funds
   * @return
   */
  def orderAdd = TODO

  /**
   * Display withdraw fund page
   */
  def withdraw = TODO

  /**
   * Order withdraq fund
   */
  def orderWithdraw = TODO
}



