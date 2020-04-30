package bimplus.services

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpMethods, HttpResponse, StatusCodes}
import bimplus.DtoResult._
import bimplus.dto.{Attribute, DtoId}
import bimplus.dto.marshalling.JsonMessageMarshaller
import bimplus.{Api, ApiFailure, ApplicationContext, DtoResult}

import scala.concurrent.{Await, ExecutionContextExecutor, Future}

/**
 * Attribute IO service
 * @param api The API
 * @param resourceUri The canonical resource URI (default "freeattributes")
 * @param ctx The application context
 * @param actorSystem The actor system
 * @param ec The execution system
 */
case class AttributeService(api: Api, resourceUri: String = "freeattributes")
						   (implicit ctx: ApplicationContext, actorSystem: ActorSystem, ec : ExecutionContextExecutor)

	extends ServiceEndpoint with JsonMessageMarshaller {

	def getAll: Future[DtoResult[List[Attribute]]] = {
		api
			.requestGetList[Attribute](this)
	}

	def getAllComplete(implicit failingDefault: OnFailing[Attribute]): Future[DtoResult[List[Attribute]]] = {
		getAll.map { r: DtoResult[List[Attribute]] =>
			r match {
				case Left(f) =>
					Left(f)
				case Right(attributes: List[Attribute]) =>
					var abort: Boolean = false
					val details = attributes.filter { _ => !abort }.map { a =>
						Await.result(api.requestDtoContext[Attribute](this, HttpMethods.GET, a).toDto[Attribute], api.timeOut) match {
							case Left(f) =>
								failingDefault.apply(a, f) match {
									case Abort =>
										abort = true
										Api.log.info(s"""${a.id}: Aborting due to failure $f""")
									case Ignore =>
										Api.log.info(s"""${a.id}: Ignoring failure $f""")
									case Retry =>
										throw new NotImplementedError("Retry")
								}
								None
							case Right(attribute) =>
								Some(attribute)
						}
					}.filter {
						_.isDefined
					}.map {
						_.get
					}
					Right(details)
			}
		}
	}

	def get(id: Option[DtoId]) : Future[DtoResult[Attribute]] = {
		api
			.requestContext(this, HttpMethods.GET, id)
			.toDto[Attribute]
	}

	def get(attribute: Attribute) : Future[DtoResult[Attribute]] = {
		api
			.requestContext(this, HttpMethods.GET, attribute.id)
			.toDto[Attribute]
	}

	def create(attribute: Attribute) : Future[DtoResult[Attribute]] = {
		api
			.request[Attribute](this, HttpMethods.POST, attribute)
			.toDto[Attribute]
	}

	def createMany(attributes: Seq[Attribute])
				  (implicit failingDefault: OnFailing[Attribute]): Future[Seq[Attribute]] = {
		doMany(attributes, create)
	}

	def update(attribute: Attribute) : Future[DtoResult[Attribute]] = {
		api
			.requestDtoContext[Attribute](this, HttpMethods.PUT, attribute)
			.toDto[Attribute]
	}

	def delete(attribute: Attribute): Future[DtoResult[Attribute]] = {
		api
			.requestDtoContext[Attribute](this, HttpMethods.DELETE, attribute.id)
			.map { response:HttpResponse => response match {
					case HttpResponse(StatusCodes.OK, _, _, _) =>
						// Deliver deleted attribute group
						DtoResult(attribute)
					case HttpResponse(code, _, _, _) =>
						// Otherwise the status code
						DtoResult(code)
				}
			}
	}

	def deleteMany(attributes: Seq[Attribute])
				  (implicit failingDefault: OnFailing[Attribute]): Future[Seq[Attribute]] = {
		doMany(attributes, delete)
	}

	private[AttributeService] def doMany(attributes: Seq[Attribute],
										 delegate: (Attribute => Future[DtoResult[Attribute]]))
										(implicit failingDefault: OnFailing[Attribute]): Future[Seq[Attribute]] = {
		Future {
			var abort: Boolean = false
			attributes.filter {_ => !abort}.zipWithIndex.map {
				case (a:Attribute,i:Int) =>
					Await.result(delegate(a), api.timeOut) match {
						case Left(apiFailure) =>
							Api.log.error(s"""While processing ($a) => $apiFailure""")
							failingDefault.apply(a, apiFailure) match {
								case Abort =>
									abort = true
									Api.log.info(s"""Aborting batch execution due error $apiFailure.""")
								case Ignore =>
									Api.log.info(s"""Failed at $a: Ignoring.""")
								case Retry =>
									// TODO Implement retry
									throw new NotImplementedError("Retry")
							}
							None
						case Right(value) =>
							Api.log.debug(s"""Processed attribute ${i+1}/${attributes.length} ($a)""")
							Some(value)
					}
			}.filter{_.isDefined}.map {_.get}
		}
	}
}
