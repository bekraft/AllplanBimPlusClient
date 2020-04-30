package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}
import bimplus.dto.{Attribute, AttributeGroup, AttributeProjectTemplate, AttributeTemplate}
import bimplus.dto.marshalling.JsonMessageMarshaller
import bimplus.{Api, ApplicationContext, DtoResult}
import bimplus.DtoResult._

import scala.concurrent.{ExecutionContextExecutor, Future}

case class AttributeProjectTemplateService(api : Api)
										  (implicit ctx: ApplicationContext, actorSystem: ActorSystem, ec : ExecutionContextExecutor)

	extends ServiceEndpoint with JsonMessageMarshaller {

	val resourceUri : String = "projectattributetemplates"

	def getAll: Future[DtoResult[List[AttributeProjectTemplate]]] = {
		api.requestGetList[AttributeProjectTemplate](this)
	}

	// TODO getAllComplete AttributeProjectTemplate
	def getAllComplete: Future[DtoResult[List[AttributeProjectTemplate]]] = ???

	def get(projectTemplate: AttributeProjectTemplate) : Future[DtoResult[AttributeProjectTemplate]] = {
		api
			.requestContext(this, HttpMethods.GET, projectTemplate.id)
			.toDto[AttributeProjectTemplate]
	}

	def create(name: String): Future[DtoResult[AttributeProjectTemplate]] = {
		create(AttributeProjectTemplate.create(name))
	}

	def create(projectTemplate: AttributeProjectTemplate): Future[DtoResult[AttributeProjectTemplate]] = {
		api
			.request[AttributeProjectTemplate](this, HttpMethods.POST, projectTemplate)
    		.toDto[AttributeProjectTemplate]
	}

	// TODO copy AttributeProjectTemplate
	def copy(from: AttributeProjectTemplate, to: AttributeProjectTemplate): Future[DtoResult[AttributeProjectTemplate]] = ???

	def update(attributeProjectTemplate: AttributeProjectTemplate): Future[DtoResult[AttributeProjectTemplate]] = {
		api
    		.requestDtoContext[AttributeProjectTemplate](this, HttpMethods.PUT, attributeProjectTemplate)
    		.toDto[AttributeProjectTemplate]
	}

	def delete(attributeProjectTemplate: AttributeProjectTemplate): Future[DtoResult[AttributeProjectTemplate]] = {
		api
			.requestDtoContext[AttributeProjectTemplate](this, HttpMethods.DELETE, attributeProjectTemplate.id)
			.map { response:HttpResponse => response match {
					case HttpResponse(StatusCodes.OK, _, _, _) =>
						// Deliver deleted attribute group
						DtoResult(attributeProjectTemplate)
					case HttpResponse(code, _, _, _) =>
						// Otherwise the status code
						DtoResult(code)
				}
			}
	}
}
