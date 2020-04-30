package bimplus.dto

import java.util.UUID

import spray.json.{DeserializationException, JsNumber, JsString, JsValue, RootJsonFormat}

import scala.reflect.runtime.universe._
import scala.reflect.ClassTag

import scala.util.{Failure, Try, Success}

package object marshalling {

	implicit val rm = runtimeMirror(this.getClass.getClassLoader)

	def enumJsonFormat[T <: Enumeration](enu: T, asInt: Boolean = false): RootJsonFormat[T#Value] =
		new RootJsonFormat[T#Value] {
			def write(obj: T#Value): JsValue = {
				if (asInt) JsNumber(obj.id) else JsString(obj.toString.toLowerCase)
			}
			def read(json: JsValue): T#Value = {
				json match {
					case JsNumber(digit) => Try(enu(digit.toInt)) match {
						case Success(value) => value
						case Failure(exception) => throw exception
					}
					case JsString(txt) => enu.withName(txt.toLowerCase)
					case somethingElse => throw DeserializationException(s"Expected a value from enum $enu instead of $somethingElse")
				}
			}
		}


	def createNew[N](productValues: Seq[Any])(implicit ct: ClassTag[N], tt: TypeTag[N]): Try[N] = {
		ct.runtimeClass.getMethods.find { m =>
			m.getName == "apply" && m.isBridge
		} match {
			case None =>
				Failure(new Exception(s"""Unable to create new instance of ${ct.runtimeClass.getName}"""))
			case Some(m) =>
				Try(m.invoke(tt, productValues: _*).asInstanceOf[N])
		}
	}
/*
	def parseEnum(enumValueType: Type, enumValueName: String): Try[Enumeration#Value] = {
		enumValueType.members.find { m =>
			m.name.toString.endsWith("outerEnum")
		} match {
			case None =>
				Failure(new )
		}
	}
*/
	def parseTo[N](str: String)(implicit tt: TypeTag[N], ct: ClassTag[N]): Try[N] = {
		try {
			tt match {
				case _ if typeOf[N] <:< typeOf[Enumeration] =>
					ct.runtimeClass.getMethods.find {
						m => m.getName == "withName"
					} match {
						case None =>
							Failure(new Exception("Missing \"withName\" method for enumeration."))
						case Some(m) =>
							Try(m.invoke(tt, str).asInstanceOf[N])
					}
				case _ if typeOf[N] <:< typeOf[UUID] =>
					Try(UUID.fromString(str).asInstanceOf[N])
				case _ if typeOf[N] <:< typeOf[Int] =>
					Try(str.toInt.asInstanceOf[N])
				case _ if typeOf[N] <:< typeOf[Double] =>
					Try(str.toDouble.asInstanceOf[N])
				case _ =>
					Failure[N](new Exception("Unknown parsing type"))
			}
		} catch {
			case e:Throwable => Failure[N](e)
		}
	}

	implicit val attributeTypeJsonFormat = enumJsonFormat(AttributeTypes)

	implicit val booleanIntsJsonFormat = enumJsonFormat(BooleanInts, true)

	implicit val attributeControlTypeJsonFormat = enumJsonFormat(AttributeControlTypes)

	implicit object UUIDJsonFormat extends RootJsonFormat[java.util.UUID] {
		def write(uuid: java.util.UUID) = {
			JsString(uuid.toString)
		}
		def read(value: JsValue) = {
			value match {
				case JsString(uuid) =>
					java.util.UUID.fromString(uuid)
				case _ =>
					throw new DeserializationException("Expected hexadecimal UUID string")
			}
		}
	}

}
