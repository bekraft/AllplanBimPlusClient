package bimplus

import akka.http.scaladsl.model._

case class ApiFailure(status: Option[StatusCode],
					  reason: Option[String],
					  exception: Option[Throwable]) {

	override def toString: String = s"""status: $status; reason: $reason; exception: $exception"""

	def isClientException = exception.isDefined
}

object ApiFailure {

	def apply(exception: Throwable): ApiFailure = ApiFailure(None, Some(exception.getMessage), Some(exception))
	def apply(status: StatusCode, reason: Option[String] = None) : ApiFailure = ApiFailure(Some(status), reason, None)
}
