package bimplus.dto.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

object JsonMarshaller extends JsonMessageMarshaller with SprayJsonSupport with DefaultJsonProtocol
