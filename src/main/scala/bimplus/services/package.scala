package bimplus

import bimplus.dto._

package object services {

	/** On-failing handler */
	type OnFailing[T <: DtoEntity] = (T, ApiFailure) => WhenFailingBehaviour

	/** Service end-point trait */
	trait ServiceEndpoint {
		def resourceUri: String
		val api: Api
	}

	/** Marker trait for root services without teamslug context */
	trait RootServiceEndpoint extends ServiceEndpoint

}
