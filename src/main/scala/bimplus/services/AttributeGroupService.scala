package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}
import bimplus.dto._
import bimplus.{Api, ApplicationContext, DtoResult}
import bimplus.DtoResult._
import bimplus.dto.marshalling.JsonMessageMarshaller

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Attribute group service endpoint wrapping "freeattributegroups" resource.
 * @param api The API client
 * @param ctx The application context
 * @param actorSystem The actor system
 * @param ec The execution system
 */
case class AttributeGroupService(api: Api)
								(implicit ctx: ApplicationContext, actorSystem: ActorSystem, ec : ExecutionContextExecutor)
	extends ServiceEndpoint with JsonMessageMarshaller {

	override val resourceUri: String = "freeattributegroups"

	def getAll: Future[DtoResult[List[AttributeGroup]]] = {
		api
			.requestGetList[AttributeGroup](this)
	}

	def get(id: Option[DtoId]): Future[DtoResult[AttributeGroup]] = {
		api
			.requestContext(this, HttpMethods.GET, id)
			.toDto[AttributeGroup]
	}

	def get(attributeGroup: AttributeGroup): Future[DtoResult[AttributeGroup]] = {
		api
    		.requestContext(this, HttpMethods.GET, attributeGroup.id)
    		.toDto[AttributeGroup]
	}

	def attributesOf(attributeGroup: AttributeGroup): AttributeService = AttributeService(api, s"""$resourceUri/${attributeGroup.id.get}/freeattributes""")

	def create(name: String, description: Option[String] = None): Future[DtoResult[AttributeGroup]] = {
		api
			.request[AttributeGroup](this, HttpMethods.POST, AttributeGroup.create(name, description, None))
    		.toDto[AttributeGroup]
	}

	def create(attributeGroup: AttributeGroup): Future[DtoResult[AttributeGroup]] = {
		api
			.request[AttributeGroup](this, HttpMethods.POST, attributeGroup)
			.toDto[AttributeGroup]
	}

	def update(attributeGroup: AttributeGroup): Future[DtoResult[AttributeGroup]] = {
		api
			.requestDtoContext[AttributeGroup](this, HttpMethods.PUT, attributeGroup)
			.toDto[AttributeGroup]
	}

	def delete(id: DtoId, withChildren: Boolean)
			  (implicit failingDefault: OnFailing[Attribute]): Future[DtoResult[AttributeGroup]] = {
		get(Some(id)).map {
			case Right(g) =>
				delete(g, withChildren)
			case Left(f) =>
				Future.successful(Left(f))
		}.flatten
	}

	def delete(attributeGroup: AttributeGroup, withChildren: Boolean = false)
			  (implicit failingDefault: OnFailing[Attribute]): Future[DtoResult[AttributeGroup]] = {
		// Pre-evaluate attribute children if required
		val preEvaluate: Future[_] = if (withChildren)
			attributesOf(attributeGroup).getAll.map {
				result => result.unboxed.map(attributes => api.attributes.deleteMany(attributes))
			}.filter(_.isDefined).map(_.get).flatten
		else
			Future.successful()

		preEvaluate.map { _ =>
			api
				.requestDtoContext[AttributeGroup](this, HttpMethods.DELETE, attributeGroup.id)
				.map { response:HttpResponse => response match {
						case HttpResponse(StatusCodes.OK, _, _, _) =>
							// Deliver deleted attribute group
							DtoResult(attributeGroup)
						case HttpResponse(code, _, _, _) =>
							// Otherwise the status code
							DtoResult(code)
					}
				}
		}.flatten
	}

}
