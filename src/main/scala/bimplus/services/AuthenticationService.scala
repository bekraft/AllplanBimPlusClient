package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpHeader, HttpMethods}
import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import bimplus.DtoResult._
import bimplus.dto.{AuthGrant}
import bimplus.{Api, ApplicationContext, DtoResult}
import bimplus.dto.marshalling.JsonMessageMarshaller

import scala.concurrent.{ExecutionContextExecutor, Future}

case class AuthenticationService(api: Api)
								(implicit ctx: ApplicationContext, actorSystem: ActorSystem, ec : ExecutionContextExecutor)
	extends RootServiceEndpoint with JsonMessageMarshaller {

	val resourceUri: String = "authorize"

	def authenticate(user: String, password: String) : Future[DtoResult[AuthGrant]] = {
		api.request(
				ctx.rootUri(resourceUri),
				HttpMethods.POST,
				AuthGrant.request(user, password)
		).toDto[AuthGrant].map { dto : DtoResult[AuthGrant] => dto match {
			case Left(apiFailure) =>
				println(s"""'$resourceUri' failed: $apiFailure""")
			case Right(grant: AuthGrant) =>
				ctx.synchronized {
					ctx.authorization = grant.toAuthorization
					ctx.clientId = grant.client_id
				}
			}
			dto
		}
	}
}
