package bimplus

import akka.actor.ActorSystem
import akka.event.Logging
import bimplus.dto.DtoEntity
import bimplus.services.{Abort, OnFailing}
import org.scalatest.Inside
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContextExecutor

trait BaseTestSpec extends AnyFlatSpec with Matchers with Inside {

	implicit val system: ActorSystem = ActorSystem()
	implicit val ec: ExecutionContextExecutor = system.dispatcher

	def newOnFailingDefault[T <: DtoEntity]: OnFailing[T] = (a:T, f:ApiFailure) => {
		log.error(s"""$a => $f""")
		Abort
	}

	protected val log = Logging(system, "Testing BIM+ API")

}