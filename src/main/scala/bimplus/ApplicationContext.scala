package bimplus

import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers.Authorization

/**
 * The BIM+ application context. There are several mutable options like {{{teamslug}}}.
 * @param id The application id in use
 * @param apiVersion The targeted API version
 */
case class ApplicationContext(id: String,
							  apiVersion: String) {

	var clientId: Option[String] = None
	var authorization: Option[Authorization] = None

	var teamslug: Option[String] = None

	def rootUri(resource: String) : String = s"""$apiVersion/$resource"""
	def resourceUri(resource: String) : String = s"""$apiVersion/${teamslug.getOrElse("content")}/$resource"""

	var propertySetIds = Seq()

	def authHeader: List[HttpHeader] = synchronized {
		authorization match {
			case Some(token) =>
				List(token)
			case _ =>
				List()
		}
	}

	def isAuthorized : Boolean = authorization.isDefined
}