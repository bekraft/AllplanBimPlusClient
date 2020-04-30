package bimplus.dto

import akka.http.scaladsl.model.headers.{Authorization, GenericHttpCredentials}
import bimplus.ApplicationContext

/**
 * Authorization grant
 * @param user_id The user id
 * @param password The password as plain text
 * @param application_id The application id
 * @param client_id The returned client id
 * @param access_token The retrieved token
 * @param token_type The retrieved token scheme
 * @param expires_in The retrieved expiration in seconds
 */
final case class AuthGrant(user_id: Option[String],
						   password: Option[String],
						   application_id: Option[String],
						   client_id: Option[String],
						   access_token: Option[String],
						   token_type: Option[String],
						   expires_in: Option[Int]) {

	override def toString: String = s"""user: $user_id, token: $access_token, client_id: $client_id"""

	def toAuthorization: Option[Authorization] = access_token.map( t => Authorization(GenericHttpCredentials(token_type.get, t)))
}

object AuthGrant {
	def request(user: String, password: String)
			   (implicit ctx: ApplicationContext): AuthGrant = AuthGrant(
		Some(user), Some(password), Some(ctx.id), None, None, None, None
	)

	def request(authorization: Authorization)
			   (implicit ctx: ApplicationContext): AuthGrant = AuthGrant(
		None, None, Some(ctx.id), ctx.clientId, Some(authorization.credentials.token()), Some(authorization.credentials.scheme()), None
	)
}