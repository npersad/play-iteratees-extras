package sorcerer.jsonframe.graph.stages

import java.io.PrintWriter

import akka.stream.scaladsl.Framing.FramingException
import akka.stream.stage.{OutHandler, InHandler, GraphStageLogic, GraphStage}
import akka.stream.{Attributes, Outlet, Inlet, FlowShape}
import sorcerer.jsonframe.parse.JsonParser
import spray.json.JsValue

import scala.util.Try

class JsonFramingStage extends GraphStage[FlowShape[String, JsValue]] {

  val in = Inlet[String]("JsonFramingStage.in")
  val out = Outlet[JsValue]("JsonFramingStage.out")
  override val shape: FlowShape[String, JsValue] = FlowShape(in, out)

  override def toString: String = "JsonFraming"

  override def createLogic(inheritedAttributes: Attributes): GraphStageLogic = new GraphStageLogic(shape) with InHandler with OutHandler {
    private val allowTruncation = true

    private var buffer = ""
    private var frameCount = 0

    override def onPush(): Unit = {
      buffer ++= grab(in)
      doParse()
    }

    override def onPull(): Unit = doParse()

    override def onUpstreamFinish(): Unit = {
      if (buffer.isEmpty) {
        completeStage()
      } else if (isAvailable(out)) {
        doParse()
      } // else swallow the termination and wait for pull
    }

    private def tryPull(): Unit = {
      if (isClosed(in)) {
        if (allowTruncation) {

          val buffed = new JsonParser(buffer).jsonValue
          if (buffed.nonEmpty)
            emitMultiple(out, buffed.toIterator)
          completeStage()
        } else
          failStage(new FramingException(
            "Stream finished but there was a truncated final frame in the buffer")
          )
      } else pull(in)
    }

    //    @tailrec
    private def doParse(): Unit = {

      if (buffer.length == 0) {

        Try {
          val pw = new PrintWriter("/usr/local/nclcom/test.txt")
          pw.close()
        }
        tryPull()
      }
      else {

        val parser = new JsonParser(buffer)
        val frames = parser.jsonValue
         if (frames.isEmpty) {
          tryPull()
        }
        else {
          buffer = parser.savedBuffer
//          parser.sillyLog("\n")
          frameCount += frames.size
//          parser.sillyLog(frames.map(_.prettyPrint).mkString("\n\n") + s"\nFRAME COUNT:$frameCount\n\n")
          emitMultiple(out, frames.toIterator)
        }
      }
    }


    setHandlers(in, out, this)
  }

}