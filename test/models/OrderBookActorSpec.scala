/*
 * Copyright © 2012 Martin Zachrison.
 *
 *     This file is part of bitcex
 *

 *     bitcex is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     bitcex is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Pu1blic License
 *     along with bitcex.  If not, see <http://www.gnu.org/licenses/>.
 */

package models

import org.specs2.mutable.Specification
import akka.actor.ActorSystem
import akka.testkit.TestActorRef
import akka.dispatch.Await
import akka.pattern.ask
import akka.util.{Timeout, Duration}
import org.specs2.mock.Mockito

class OrderBookActorSpec extends Specification with Mockito {

  implicit val system = ActorSystem("OrderBookActorSpec")
  val duration = Duration("5 seconds")
  implicit val timeout = Timeout(duration)
  val sellerId = UserId(1L)
  val askOrderSEK_10_5 = AskOrderSEK(BTC(10), SEK(5), sellerId)
  val askOrderSEK_10_6 = AskOrderSEK(BTC(10), SEK(6), sellerId)
  val buyerId = UserId(2L)
  val bidOrderSEK_9_4 = BidOrderSEK(BTC(9), SEK(4), buyerId)


  "An OrderBookActor" should {

    val orderBook = TestActorRef(new OrderBookActor[BTC, SEK]())
    orderBook ! askOrderSEK_10_5
    orderBook ! askOrderSEK_10_6
    orderBook ! bidOrderSEK_9_4

    "Return all orders for ListOrder" in {
      val orders = Await.result((orderBook ? ListOrders), duration).asInstanceOf[Orders[BTC, SEK]]
      orders must_== Orders(List(askOrderSEK_10_5, askOrderSEK_10_6), List(bidOrderSEK_9_4))
    }

    "Return an filter list for ListOrder(userId)" in {
      val orders = Await.result(orderBook ? ListOrders(buyerId), duration).asInstanceOf[Orders[BTC, SEK]]
      orders must_== Orders(List(), List(bidOrderSEK_9_4))
    }

  }

}