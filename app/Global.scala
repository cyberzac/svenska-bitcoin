import play.api.{Application, GlobalSettings}

object Global extends GlobalSettings {
   //Todo alias models
 // type SEK = org.bitcex.model.SEK

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