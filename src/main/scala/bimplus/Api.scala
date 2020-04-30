package bimplus

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import bimplus.services.{Abort, AttributeGroupService, AttributeProjectTemplateService, AttributeService, AttributeTemplateService, AuthenticationService, OnFailing, RootServiceEndpoint, ServiceEndpoint, WhenFailingBehaviour}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}
import bimplus.dto.{Attribute, AttributeGroup, AttributeProjectTemplate, AttributeTemplate, AuthGrant, DtoEntity, DtoId, ElementType}
import bimplus.DtoResult._
import spray.json._

import scala.language.postfixOps
import scala.concurrent.duration._

/**
 * @param url The root URL of BIM+ API
 * The API main wrapper client.
 * @param timeOut Custom timeout for the API client (only affects the cancellation if less than akka.client.connecting-timeout)
 * @param actorSystem (implicit akka actor system)
 * @param ec (implicit execution context)
 * @param ctx (implicit application context)
 */
class Api(val url : String, val timeOut : Duration = 30 seconds)
		 (implicit actorSystem: ActorSystem, ec : ExecutionContextExecutor, ctx: ApplicationContext) {

	private[Api] lazy val http = Http()

	lazy val authorize = AuthenticationService(this)
	lazy val attributes = AttributeService(this)
	lazy val attributeGroups = AttributeGroupService(this)
	lazy val attributeTemplates = AttributeTemplateService(this)
	lazy val attributeProjectTemplates = AttributeProjectTemplateService(this)

	private[Api] def uriSwitch(endpoint: ServiceEndpoint): String = {
		endpoint match {
			case _: RootServiceEndpoint =>
				ctx.rootUri(endpoint.resourceUri)
			case _: ServiceEndpoint =>
				ctx.resourceUri(endpoint.resourceUri)
		}
	}

	def request[T](resource: String, requestMethod: HttpMethod, requestObject: T)
				  (implicit jsonFrom: JsonWriter[T]): Future[HttpResponse] = {
		val httpRequest = HttpRequest(
			method = requestMethod,
			uri = Uri(url) + "/" + resource,
			headers = ctx.authHeader,
			entity = HttpEntity(ContentTypes.`application/json`, requestObject.toJson.compactPrint)
		)

		Api.log.debug(s"""$requestMethod ~> ${httpRequest.uri}""")
		http.singleRequest(httpRequest)
	}

	def request[T](endpoint: ServiceEndpoint, requestMethod: HttpMethod, requestDto: T)
				  (implicit jsonFrom: JsonWriter[T]): Future[HttpResponse] = {
		request(uriSwitch(endpoint), requestMethod, requestDto)
	}

	def requestDtoContext[T <: DtoEntity](endpoint: ServiceEndpoint, requestMethod: HttpMethod, contextDto: T)
										 (implicit jsonFrom: JsonWriter[T]): Future[HttpResponse] = {
		requestDtoContext[T](endpoint, requestMethod, contextDto.id, Some(contextDto))
	}

	def requestDtoContext[T <: DtoEntity](endpoint: ServiceEndpoint, requestMethod: HttpMethod, id: Option[DtoId], requestDto: Option[T] = None)
										 (implicit jsonFrom: JsonWriter[T]): Future[HttpResponse] = {
		requestDto match {
			case None =>
				requestContext(endpoint, requestMethod, id)
			case Some(dto) =>
				request(uriSwitch(endpoint) + id.map("/" + _).getOrElse(""), requestMethod, dto)
		}
	}

	def requestGetList[R <: DtoEntity](endpoint: ServiceEndpoint, subResourceUri: Option[String] = None)
									  (implicit jsonReader: JsonReader[List[R]]): Future[DtoResult[List[R]]] = {
		val getUri = Uri(url) + "/" + uriSwitch(endpoint) + subResourceUri.map("/" +  _).getOrElse("")
		Api.log.debug(s"""GET ~> ${getUri}""")
		http.singleRequest(HttpRequest(
				method = HttpMethods.GET,
				uri = getUri,
				headers = ctx.authHeader)).toDto[List[R]]
	}

	def requestContext(endpoint: ServiceEndpoint, requestMethod: HttpMethod = HttpMethods.GET, id: Option[DtoId] = None): Future[HttpResponse] = {
		val getUri = Uri(url) + "/" + uriSwitch(endpoint) + id.map("/" + _).getOrElse("")
		val httpRequest = HttpRequest(
			method = requestMethod,
			uri = getUri,
			headers = ctx.authHeader
		)

		Api.log.debug(s"""$requestMethod ~> ${httpRequest.uri}""")
		http.singleRequest(httpRequest)
	}

	/**
	 * Request a login authorization. If successfully performed, authorization token is mapped to the
	 * implicit application context (and thus available for all services)
	 * @param user The user
	 * @param password The password
	 * @return An optional access grant DTO if successful
	 */
	def login(user: String, password: String): Option[AuthGrant] = {
		Await result (authorize.authenticate(user, password), timeOut) match {
			case Left(apiFailure) =>
				Api.log.error(s"""Access denied for user: $user and password. $apiFailure""")
				None
			case Right(grant) =>
				Api.log.info(s"""Authorized access for user: $user. Expiration: ${grant.expires_in.map(_ / 60).getOrElse(0)} seconds""")
				Some(grant)
		}
	}

	/**
	 * True, if logged in successfully
	 * @return
	 */
	def isAuthorized : Boolean = ctx.isAuthorized

	/**
	 * Batch creating templates from group references.
	 * @param projectTemplate The parent project template
	 * @param attr The attributes (having a group references as parent)
	 * @param elemTypes The element type to associated with (at least one)
	 * @return A future of a map from group ID to created template
	 */
	def createTemplateFromGroupReference(projectTemplate: AttributeProjectTemplate,
										 attr: Seq[Attribute],
										 elemTypes: ElementType*): Future[Map[DtoId, Option[AttributeTemplate]]] = {
		Future {
			val groups = attr
				.filter {
					_.parent.isDefined
				}
				.groupBy {
					_.parent.get
				}

			val namedGroups = groups.keys.map { id =>
				Await.result(attributeGroups.get(id), timeOut).unboxed match {
					case None =>
						// Use ID by default (shouldn't happen)
						(id -> id.toString)
					case Some(g) =>
						// Use name or ID (also shouldn't happen)
						(id -> g.name.getOrElse(id.toString))
				}
			}.toMap

			groups.keys.map { id =>
				id -> AttributeTemplate.create(projectTemplate.id.get, namedGroups(id), groups(id), elemTypes: _ *)
			}.map {
				case (id, template) =>
					id -> Await.result(attributeTemplates.create(template), timeOut).unboxed
			}.toMap
		}
	}

	/** Gets the attributes of given groups as a single sequence */
	def groupAttributes(groupIds: DtoId*): Seq[Attribute] = {
		groupIds.flatMap { groupId =>
			Await.result(attributeGroups.attributesOf(AttributeGroup(Some(groupId))).getAll, timeOut).unboxed match {
				case Some(a) =>
					a
				case None =>
					Nil
			}
		}
	}

	override def toString: String = s"""BIM+API Client ($url)"""
}

object Api {
	implicit val system: ActorSystem = ActorSystem()
	implicit val ec: ExecutionContextExecutor = system.dispatcher

	/** Production route */
	lazy val prod : Api = new Api ("https://api.bimplus.net")
	/** Testing route */
	lazy val stage : Api = new Api ("https://api-stage.bimplus.net")

	val log = Logging(system, "BIM+ API")

	def newFailingBehavior[T <: DtoEntity](behaviour: WhenFailingBehaviour): OnFailing[T] = (a:T, f:ApiFailure) =>  {
		log.error(s"""$behaviour: Action on [$a] failed with $f""")
		behaviour
	}

	// Default failing behaviour => Abort and log
	implicit def onFailingDefault[T <: DtoEntity]: OnFailing[T] = newFailingBehavior[T](Abort)

	// Using Excel Application Context
	implicit val applicationContext : ApplicationContext = ApplicationContext("c25706f5-e296-fa1b-9459-a9a25d1d01ac", "v2")

}