package bimplus

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.util.ByteString
import spray.json._
import spray.json.JsonReader

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

object DtoResult {
	implicit class DtoWrapper(val httpResponse: Future[HttpResponse]) {
		def toDto[R](implicit jsonReader: JsonReader[R], actorSystem: ActorSystem, ec : ExecutionContextExecutor): Future[DtoResult[R]] = {
			httpResponse.flatMap { parse(_) }
		}

		def awaitDto[R](timeOut: Duration = Duration.Inf)
					   (implicit jsonReader: JsonReader[R], actorSystem: ActorSystem, ec : ExecutionContextExecutor): DtoResult[R] = {
			Await result (toDto[R], timeOut)
		}
	}

	implicit class DtoResultWrapper[R](val dtoResult: DtoResult[R]) {
		def unboxed: Option[R] = dtoResult match {
			case Left(apiFailure: ApiFailure) =>
				Api.log.error(s"""Unwrapped failure: $apiFailure""")
				None
			case Right(dto) =>
				Some(dto)
		}
	}

	def parse[R](httpResponse: HttpResponse)
				(implicit jsonReader: JsonReader[R], actorSystem: ActorSystem, ec : ExecutionContextExecutor): Future[DtoResult[R]] = {

		val entity = httpResponse.entity
		httpResponse.status match {
			case StatusCodes.OK | StatusCodes.Created | StatusCodes.Accepted =>
				val dto = httpResponse.entity.dataBytes.runFold(ByteString(""))(_ ++ _).map {
					body => body.utf8String.parseJson.convertTo[R]
				}
				dto.map {Right(_)}
			case _ =>
				entity.dataBytes.runFold(ByteString(""))(_ ++ _)
						.map { _.utf8String }
						.map { message => Left(ApiFailure(httpResponse.status, Some(message))) }
		}
	}

	def await[R](httpResponse: HttpResponse, timeOut: Duration = Duration.Inf)
				(implicit jsonReader: JsonReader[R], actorSystem: ActorSystem, ec : ExecutionContextExecutor): DtoResult[R] = {
		Await result (parse[R](httpResponse), timeOut)
	}

	def apply[R](dto: R): DtoResult[R] = Right(dto)

	def apply[R](httpStatus: StatusCode): DtoResult[R] = Left(ApiFailure(httpStatus))
}
