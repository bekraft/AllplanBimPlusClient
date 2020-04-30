package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}

import bimplus.DtoResult._
import bimplus.{Api, ApplicationContext, DtoResult}
import bimplus.dto.AttributeTemplate
import bimplus.dto.marshalling.JsonMessageMarshaller

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * The attribute template service.
 *
 * @param api         The API
 * @param ctx         The application context
 * @param actorSystem The akka actor system
 * @param ec          The execution context
 */
case class AttributeTemplateService(api : Api)
								   (implicit ctx: ApplicationContext, actorSystem: ActorSystem, ec : ExecutionContextExecutor)

	extends ServiceEndpoint with JsonMessageMarshaller {

	val resourceUri: String = "attributetemplates"

	def getAll: Future[DtoResult[List[AttributeTemplate]]] = {
		api.requestGetList[AttributeTemplate](this)
	}

	def getAllComplete: Future[DtoResult[List[AttributeTemplate]]] = ???

	def get(projectTemplate: AttributeTemplate) : Future[DtoResult[AttributeTemplate]] = {
		api
			.requestContext(this, HttpMethods.GET, projectTemplate.id)
			.toDto[AttributeTemplate]
	}

	def create(projectTemplate: AttributeTemplate): Future[DtoResult[AttributeTemplate]] = {
		api
			.request[AttributeTemplate](this, HttpMethods.POST, projectTemplate)
			.toDto[AttributeTemplate]
	}

	def update(AttributeTemplate: AttributeTemplate): Future[DtoResult[AttributeTemplate]] = {
		api
			.requestDtoContext[AttributeTemplate](this, HttpMethods.PUT, AttributeTemplate)
			.toDto[AttributeTemplate]
	}

	def delete(AttributeTemplate: AttributeTemplate): Future[DtoResult[AttributeTemplate]] = {
		api
			.requestDtoContext[AttributeTemplate](this, HttpMethods.DELETE, AttributeTemplate.id)
				.map { response:HttpResponse => response match {
					case HttpResponse(StatusCodes.OK, _, _, _) =>
						// Deliver deleted attribute group
						DtoResult(AttributeTemplate)
					case HttpResponse(code, _, _, _) =>
						// Otherwise the status code
						DtoResult(code)
				}
			}
	}
	
}
