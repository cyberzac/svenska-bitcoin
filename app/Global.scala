import akka.actor.ActorSystem._
import akka.actor.{Props, ActorSystem}
import models._
import models.{PlayActorService, SEK, BTC, OrderBookActor}
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {

  override def onStart(app: Application) {
    //Todo remove hard cording
    PlayActorService.create(Name("Martin Zachrison"), Email("zac@cyberzac.se"), "secret")
    PlayActorService.create(Name("Mats Henricson"), Email("mats@henricson.se"), "secret")
    PlayActorService.create(Name("Jarl Fransson"), Email("jarl@acm.org"), "secret")
    PlayActorService.create(Name("Olle Kullberg"), Email("olle.kullberg@gmail.com"), "secret")

  }
}