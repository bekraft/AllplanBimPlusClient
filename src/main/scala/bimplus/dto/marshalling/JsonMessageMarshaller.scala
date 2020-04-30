package bimplus.dto.marshalling

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol

trait JsonMessageMarshaller extends SprayJsonSupport with DefaultJsonProtocol {

	import bimplus.dto._

	implicit val authGrantFormat = jsonFormat7(AuthGrant.apply)
	implicit val attributeFormat = jsonFormat13(Attribute.apply)
	implicit val attributeGroupFormat = jsonFormat4(AttributeGroup.apply)
	implicit val elementTypeFormat = jsonFormat5(ElementType.apply)
	implicit val attributeTemplateFormat = jsonFormat10(AttributeTemplate.apply)
	implicit val attributeProjectTemplateFormat = jsonFormat3(AttributeProjectTemplate.apply)
}
