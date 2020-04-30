package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}
import bimplus.dto._
import bimplus.{Api, ApplicationContext, DtoResult}
import bimplus.DtoResult._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol


import spray.json._

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Abstract implementation of get-listing, creating, updating and deleting of DTOs.
 * @tparam T The type of DTO
 */

trait DefaultDtoServiceEndpoint[T <: DtoEntity] extends ServiceEndpoint {

	import bimplus.dto.marshalling.JsonMarshaller._

	protected [this] implicit val json : JsonFormat[T]
	protected [this] implicit val jsonList : JsonFormat[List[T]]
	protected [this] implicit def ctx: ApplicationContext
	protected [this] implicit def actorSystem: ActorSystem
	protected [this] implicit def ec: ExecutionContextExecutor

	def create(o: T): Future[DtoResult[T]] = {
		api.request[T](this, HttpMethods.POST, o).toDto[T]
	}

	def update(o: T): Future[DtoResult[T]] = {
		api.requestDtoContext[T](this, HttpMethods.PUT, o).toDto[T]
	}

	def delete(o: T): Future[DtoResult[T]] = {
		api
			.requestDtoContext[T](this, HttpMethods.DELETE, o.id)
			.map { response:HttpResponse => response match {
					case HttpResponse(StatusCodes.OK, _, _, _) =>
						// Deliver deleted attribute group
						DtoResult(o)
					case HttpResponse(code, _, _, _) =>
						// Otherwise the status code
						DtoResult(code)
				}
			}
	}

	def getAll: Future[DtoResult[List[T]]] = api.requestGetList[T](this)(jsonList)
}
