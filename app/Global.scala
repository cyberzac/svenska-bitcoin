import akka.camel.CamelContextManager
import akka.camel.CamelServiceManager._
import akka.actor.Actor._
import org.bitcex.camel._
import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {
  override def onStart(app: Application) {
/*
    val tickerActor = actorOf[TickerActor]
    val tickerProducerActor = actorOf[TickerProducerActor]
    val tickerConsumerActor = actorOf(new TickerConsumerActor(tickerActor, tickerProducerActor))
    val mtGoxActor = actorOf(new MtGoxTickerActor(tickerActor))
    val mtGoxProducer = actorOf(new MtGoxTickerProducer((mtGoxActor)))
    val ecbCurrencyActor = actorOf(new EcbCurrencyActor(tickerActor))

    startCamelService
    CamelContextManager.init()
    val context = CamelContextManager.context.get
    context.addRoutes(BitcexRouteBuilder)
    CamelContextManager.start

    tickerActor.start()
    tickerProducerActor.start()
    tickerConsumerActor.start()
    mtGoxActor.start()
    mtGoxProducer.start()
    ecbCurrencyActor.start()
    */
  }
}