package models

import org.specs2.mutable.Specification
import org.specs2.execute.Success

class OrderBookSpec extends Specification {

  val sellerId = UserId(1L)
  val buyerId = UserId(2L)

  "An OrderBook" should {

    "Que an AskOrder if no match is possible" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 10)).isEmpty must_== true
    }

    "Que AskOrders in rising order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 10)).isEmpty must_== true
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      dut.matchOrder(ask(5, 11)).isEmpty must_== true
      verifyOrders(dut.askOrders, ask(5, 9), ask(5, 10), ask(5, 11))
      dut.bidOrders.isEmpty must_== true
    }

    "Que an BidOrder if no asks are present" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(bid(3, 10)).isEmpty must_== true
      dut.askOrders.isEmpty must_== true
      verifyOrders(dut.bidOrders, bid(3, 10))
    }

    "Que an BidOrder if no match is possible" in {

      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 11)).isEmpty must_== true
      dut.matchOrder(bid(3, 10)).isEmpty must_== true
      verifyOrders(dut.askOrders, ask(5, 11))
      verifyOrders(dut.bidOrders, bid(3, 10))
    }

    "Que BidOrders in falling order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(bid(3, 10)).isEmpty must_== true
      dut.matchOrder(bid(5, 9)).isEmpty must_== true
      dut.matchOrder(bid(5, 11)).isEmpty must_== true
      verifyOrders(dut.bidOrders, bid(5, 11), bid(3, 10), bid(5, 9))
      dut.askOrders.isEmpty must_== true
    }

    "Return a trade if ask and bid are equal for an bid order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      val trade = dut.matchOrder(bid(5, 9))(0)
      trade.amount must_== BTC(5)
      trade.price must_== SEK(9)
      trade.sellerId must_== sellerId
      trade.buyerId must_== buyerId
    }

    "Remove orders in a matched trade for an bid order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      dut.matchOrder(bid(5, 9))(0)
      dut.askOrders.isEmpty must_== true
      dut.bidOrders.isEmpty must_== true
    }

    "Return a trade with the oldest price if the ask and bid overrun each other for a bid order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      val trade = dut.matchOrder(bid(5, 11))(0)
      trade.amount must_== BTC(5)
      trade.price must_== SEK(9)
      trade.sellerId must_== sellerId
      trade.buyerId must_== buyerId
    }

    "Return a partial trade if the ask and bid amounts differ" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      val trade = dut.matchOrder(bid(3, 10))(0)
      trade.amount must_== BTC(3)
      trade.price must_== SEK(9)
      trade.sellerId must_== sellerId
      trade.buyerId must_== buyerId
    }

    "Replace the ask order with an ask order with the remaing amount if the match bid amount was lower" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      dut.matchOrder(bid(3, 10))
      verifyOrders(dut.askOrders, ask(2, 9))
    }

    "Replace the bid order with an bid order with the remaing amount if the match ask amount was lower" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(bid(9, 10)).isEmpty must_== true
      dut.matchOrder(ask(5, 9))
      verifyOrders(dut.bidOrders, bid(4, 10))
    }

    "Match multiple asks and yield several Trades for one bid order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(ask(5, 9)).isEmpty must_== true
      dut.matchOrder(ask(5, 10)).isEmpty must_== true
      val trades = dut.matchOrder(bid(11, 10))
      val trade1 = Trade(BTC(5), SEK(9), sellerId, buyerId)
      val trade2 = Trade(BTC(5), SEK(10), sellerId, buyerId)
      verifyTrades(trades, List(trade1, trade2))
      dut.askOrders.isEmpty must_== true
      verifyOrders(dut.bidOrders, bid(1, 10))
    }

    "Match multiple bids and yield several Trades for one ask order" in {
      val dut = new OrderBook[BTC, SEK]()
      dut.matchOrder(bid(3, 9)).isEmpty must_== true
      dut.matchOrder(bid(3, 10)).isEmpty must_== true
      dut.matchOrder(bid(3, 8)).isEmpty must_== true
      dut.matchOrder((bid(2, 9))).isEmpty must_== true
      val actual = dut.matchOrder(ask(10, 9))
      val expected = List(trade(3, 9), trade(3, 10), trade(2, 9))
      verifyTrades(actual, expected)
      verifyOrders(dut.bidOrders, bid(3, 8))
      verifyOrders(dut.askOrders, ask(2, 9))
    }

  }

  def trade(btc: Int, sek: Int): Trade[BTC, SEK] = {
    Trade(btc, sek, sellerId, buyerId)
  }

  def verifyOrders(orders: List[Order[BTC, SEK]], expected: Order[BTC, SEK]*) = {
    val zip = orders zip expected
    for ((actual, expected) <- zip) {
      actual.amount must_== expected.amount
      actual.price must_== expected.price
    }
    Success()
  }

  implicit def int2Btc(i: Int): BTC = BTC(i)

  implicit def int2Sek(i: Int): SEK = SEK(i)

  def bid(amount: BTC, price: SEK, buyerId: UserId = buyerId) = BidOrderSEK(amount, price, buyerId)

  def ask(amount: BTC, price: SEK, sellerId: UserId = sellerId) = AskOrderSEK(amount, price, sellerId)

  def verifyTrades(actual: List[Trade[BTC, SEK]], expected: List[Trade[BTC, SEK]]) = {
    val zip = actual zip expected
    zip foreach (p => compareTrade(p._1, p._2))
    Success()
  }

  /**
   * Verify that the amount, price, sellerId and buyerId are the same.
   * Ignore the time and id fields.
   * @param actual
   * @param expected
   */
  def compareTrade(actual: Trade[BTC, SEK], expected: Trade[BTC, SEK]) = {
    actual.amount must_== expected.amount
    actual.price must_== expected.price
    actual.sellerId must_== expected.sellerId
    actual.buyerId must_== expected.buyerId
  }
}