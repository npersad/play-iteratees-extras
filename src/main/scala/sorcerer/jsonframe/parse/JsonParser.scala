package sorcerer.jsonframe.parse

import spray.json._

final class JsonParser(var buffer: String) extends Combinators { self =>

  var savedBuffer = ""

  private def frame: Option[JsValue] =
    peekOne flatMap {
      case ('[') | (',') =>
        for {
          _ <- drop(1)
          _ <- skipWhitespace
          value <- frame
        } yield value

      case ('{') =>
        _jsonValue

      case _ =>
        None
    }

  def jsonValue: List[JsValue] = {

    savedBuffer = buffer

    frame match {
      case Some(value) =>
        value :: jsonValue
      case None =>
        Nil
    }
  }

  /**
    * Creates a JSON object from a key value Function
    */
  private def jsonObjectCreator(keyValues: Iterable[(String, JsValue)]): JsObject = new JsObject(keyValues.toMap)

  /**
    * Creates a JSON array from a key value Function
    */
  private def jsonArrayCreator(values: Iterable[JsValue]): JsArray = new JsArray(values.toVector)

  //{ key : <object> } where <object> is possibly nested"
  private def jsonKeyValueImpl[A](valueHandler: (String => Option[(String, A)])) = for {
    key <- jsonString
    _ <- skipWhitespace
    _ <- expect(':')
    _ <- skipWhitespace
    value <- valueHandler(key.value)
  } yield value

  private def jsonKeyValuesImpl[A, V](keyValuesHandler: (List[(String, JsValue)] => JsObject) = jsonObjectCreator,
                                      valueHandler: (String => Option[(String, JsValue)]) = (key: String) => _jsonValue.map((key, _))
                                     ): Option[List[(String, JsValue)]] = for {
    _ <- skipWhitespace
    fed <- jsonKeyValueImpl(valueHandler)
    _ <- skipWhitespace
    keyValues <- takeOneOf('}', ',') match {

      //flatten pairs
      case Some('}') =>
        Option(List(fed))

      //accumulate pairs
      case Some(',') =>
        jsonKeyValuesImpl(keyValuesHandler, valueHandler) map (fed :: _)
      case _ => None
    }
  } yield keyValues


  private def jsonObject: Option[JsObject] = jsonObject()

  private def jsonObject[A, V](keyValuesHandler: (List[(String, JsValue)]) => JsObject = jsonObjectCreator,
                       valueHandler: (String => Option[(String, JsValue)]) = (key: String) => _jsonValue.map((key,_))
                      ): Option[JsObject] =
    for {
      list <- jsonObjectImpl(keyValuesHandler, valueHandler)
    } yield keyValuesHandler(list)

  private def jsonObjectImpl[A, V](keyValuesHandler: (List[(String, JsValue)]) => JsObject = jsonObjectCreator,
                                   valueHandler: (String => Option[(String, JsValue)]) = (key: String) => _jsonValue.map((key, _))
                                  ) = for {
    _ <- skipWhitespace
    _ <- expect('{')
    _ <- skipWhitespace
    keyValues <- peekOne match {
      case _ =>
        jsonKeyValuesImpl(keyValuesHandler, valueHandler)
    }
    _ <- skipWhitespace // skip all whitespaces after a object has been parsed since the next object might be separated by some whitespace/newline
  } yield keyValues

  private def jsonValueForEach: Int => Option[JsValue] = index => _jsonValue

  private def jsonArrayValues[A, V](valuesHandler: (List[JsValue] => JsArray),
                            valueHandler: Int => Option[JsValue],
                            index: Int = 0): Option[JsArray] =
    for {
      listO <- jsonArrayValuesImpl(valuesHandler, valueHandler, index)
    } yield valuesHandler(listO)

  private def jsonArrayValuesImpl[A, V](valuesHandler: (List[JsValue] => JsArray),
                                        valueHandler: Int => Option[JsValue],
                                        index: Int = 0): Option[List[JsValue]] = for {
    _ <- skipWhitespace
    fed <- valueHandler(index)
    _ <- skipWhitespace
    ch <- takeOneOf(']', ',')
    values <- ch match {
      case ']' =>
        Option(List(fed))
      case ',' =>
        jsonArrayValuesImpl(valuesHandler, valueHandler, index + 1) map (fed :: _)
    }
    _ <- skipWhitespace
  } yield values


  private def jsonArray: Option[JsArray] = jsonArray()

  private def jsonArray[A, V](valuesHandler: (List[JsValue] => JsArray) = jsonArrayCreator,
                      valueHandler: (Int => Option[JsValue]) = jsonValueForEach
                     ): Option[JsArray] =
    for {
      listO <- jsonArrayImpl(valuesHandler, valueHandler)
    } yield jsonArrayCreator(listO)


  private def jsonArrayImpl[A, V](valuesHandler: (List[JsValue] => JsArray) = jsonArrayCreator,
                                  valueHandler: Int => Option[JsValue] = jsonValueForEach) = for {
    _ <- skipWhitespace
    _ <- expect('[')
    _ <- skipWhitespace
    values <- peekOne match {
      case Some(']') =>
        None
      case _ =>
        jsonArrayValuesImpl(valuesHandler, valueHandler)
    }
    _ <- skipWhitespace
  } yield values


  private def _jsonValue: Option[JsValue] = peekOne flatMap {
    case ('"') => jsonString
    case ('{') => jsonObject
    case ('[') => jsonArray
    case (n) if n == '-' || (n >= '0' && n <= '9') => jsonNumber
    case ('f') | ('t') => jsonBoolean
    case ('n') => jsonNull
  }

  private def jsonNumber = for {
    number <- peekWhile(ch => ch.isDigit || ch == '+' || ch == '-' || ch == '.' || ch == 'e' || ch == 'E')
    jsNumber <- try {
      Option(new JsNumber(BigDecimal(number)))
    } catch {
      case e: NumberFormatException =>
        None
    }
    _ <- dropWhile(ch => ch.isDigit || ch == '+' || ch == '-' || ch == '.' || ch == 'e' || ch == 'E')
  } yield jsNumber

  private def jsonBoolean = for {
    boolean <- peekWhile(ch => ch >= 'a' && ch <= 'z')
    jsBoolean <- boolean match {
      case t if t == "true" =>
        Option(JsBoolean(true))
      case f if f == "false" =>
        Option(JsBoolean(false))
    }
    _ <- dropWhile(ch => ch >= 'a' && ch <= 'z')
  } yield jsBoolean

  private def jsonNull = for {
    nullStr <- peekWhile(ch => ch >= 'a' && ch <= 'z')
    jsNull <- nullStr match {
      case n if n == "null" =>
        Option(JsNull)
    }
    _ <- dropWhile(ch => ch >= 'a' && ch <= 'z')
  } yield jsNull

  private def jsonString: Option[JsString] = {

    def stringContents(contents: String = "", escaped: Option[StringBuilder] = None): Option[String] = buffer match {

      case data =>

        var i = 0
        var start = 0
        var done = false
        var esc = escaped
        var current = contents
        var error: Option[String] = None

        while (i < data.length && !done) {
          val c = data.charAt(i)

          // We're in the middle of an escape sequence
          if (esc.isDefined) {
            unescape(esc.get.append(c)) match {
              case Left(err) =>
                error = Some(err)
              case Right(Some(unesc)) =>
                current ++= ("" + unesc)
                start = i + 1
                esc = None
              case Right(None) =>
            }
          } else if (c >= 0 && c < 32) {
            error = Some("Illegal control character found in JSON String: 0x" + Integer.toString(c, 16))
          } else if (c == '\\') {
            // Start escape sequence
            current ++= data.substring(start, i)
            esc = Some(new StringBuilder)
          } else if (c == '"') {
            // End of string
            current ++= data.substring(start, i)
            done = true
          }
          i += 1
        }

        if (done) {
          buffer = data.substring(i, data.length)
          Option(current)
        } else
          None
    }

    /**
      * Unescape the escape sequence in the StringBuilder
      *
      * @param esc The escape sequence.  Must not include the leading \, and must contain at least 1 character
      * @return Either the result of the unescaping, or an error.  The result of the unescaping is some character, or
      *         None if more input is needed to unescape this sequence.
      */
    def unescape(esc: StringBuilder): Either[String, Option[Char]] = {
      esc(0) match {
        case 'n' => Right(Some('\n'))
        case 'r' => Right(Some('\r'))
        case 't' => Right(Some('\t'))
        case 'b' => Right(Some('\b'))
        case 'f' => Right(Some('\f'))
        case '\\' => Right(Some('\\'))
        case '"' => Right(Some('"'))
        case '/' => Right(Some('/'))
        case 'u' if esc.size >= 5 => try {
          Right(Some(Integer.parseInt(esc.drop(1).toString(), 16).toChar))
        } catch {
          case e: NumberFormatException => Left("Illegal unicode escape sequence: \\u" + esc)
        }
        case 'u' => Right(None)
        case _ => Left("Unknown escape sequence: \\" + esc)
      }
    }

    for {
      _ <- expect('"')
      data <- stringContents("")
    } yield new JsString(data)
  }
}