package akkaJsonFrame


import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.Timeout
import sorcerer.jsonframe.graph.stages.JsonFramingStage
import akka.stream._
import akka.stream.scaladsl._
import akka.testkit.{ImplicitSender, TestKit}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Seconds, Span}
import org.scalatest.{Matchers, WordSpecLike}

import scala.concurrent.duration._

// this is the spray ast

class AkkaJsonFrameSpec extends TestKit(ActorSystem("ExtraTestAS")) with ImplicitSender with WordSpecLike with Matchers with ScalaFutures {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(5, Seconds), interval = Span(5, Seconds))


  "An AkkaJsonFrame" should {

    "jsonStream graph behavior" in {
      implicit val materializer: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))
      implicit val timeout = Timeout(10.seconds)

      val weatherInLondon = s"/api/v0/en/calendars/default/2015/6"

      val checkIPFlow = Http().outgoingConnection("calapi.inadiutorium.cz") // this is the spray ast

      val byteSource = Source.single(HttpRequest(HttpMethods.GET, uri = Uri(weatherInLondon)))
        .via(checkIPFlow)
        .flatMapConcat {
          case response@HttpResponse(StatusCodes.OK, _, _, _) =>
            response.entity.dataBytes
              .map(_.utf8String)
              .via(new JsonFramingStage)
        }

      val result = byteSource.runWith(Sink.head)

      import spray.json._
      val expected = """
                       |  {
                       |    "date":"2015-06-01",
                       |    "season":"ordinary",
                       |    "season_week":9,
                       |    "celebrations":[
                       |      {
                       |        "title":"Saint Justin Martyr",
                       |        "colour":"red",
                       |        "rank":"memorial",
                       |        "rank_num":3.1
                       |      }
                       |    ],
                       |    "weekday":"monday"
                       |  }""".stripMargin.parseJson

      whenReady(result) { res =>
        res.prettyPrint shouldBe expected.prettyPrint
      }
    }

  }
}
