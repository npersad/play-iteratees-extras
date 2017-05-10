package sorcerer.jsonframe.parse

import java.io.{BufferedWriter, FileWriter}

trait Combinators {
  self =>
  var buffer: String

  //because i could not get breakpoints to work through Intellij
  def sillyLog[T](statements: T*) = {
    val file = new java.io.File("~/Log.txt")
    val bw = new BufferedWriter(new FileWriter(file, true))
    statements.foreach { stmt =>
      bw.append(stmt.toString)
    }
    bw.close()
    Option(Unit)
  }

  def skipWhitespace = {
    buffer = buffer.dropWhile(_.isWhitespace)
    Option(Unit)
  }

  def expect(value: Char): Option[Char] = for {
    result <- peekOne match {
      case Some(c) if c == value =>
        Option(c)
      case _ =>
        None
    }
    _ <- drop(1)
  } yield result


  def drop(n: Int) = {
    Option(buffer = buffer.drop(n))
  }

  def takeOneOf(values: Char*): Option[Char] = {
    val taked = peekOne match {
      case Some(value) if values.contains(value) =>
        Option(value)
      case _@x =>
        None
    }

    drop(1)

    taked
  }

  def dropWhile(p: Char => Boolean): Option[Unit] = {
    buffer = buffer.dropWhile(p)
    Option(Unit)
  }

  def peekWhile(p: Char => Boolean, peeked: String = ""): Option[String] = buffer match {
    case (data) if data.length > 0 => {
      val taken = data.takeWhile(p)
      if (taken.length == data.length) {
        peekWhile(p, peeked ++ taken)
      } else {

        buffer = peeked ++ data
        Option((peeked ++ taken).mkString)
      }
    }
    case _ => None
  }

  def peekOne: Option[Char] = {
    buffer match {
      case data =>
        val huh = data
        data.headOption.map { c =>
          Some(c)
        }.getOrElse(None)
    }
  }
}
