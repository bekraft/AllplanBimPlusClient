package object bimplus {

	type DtoResult[T] = Either[ApiFailure,T]

	implicit def toOption[T](o:T): Option[T] = Option(o)

	implicit def optionToString[T <: Option[_]](o: T): String = o.map(_.toString).getOrElse("(None)")

	implicit def toBlankOption(s:String): Option[String] = if (s.isBlank) None else Some(s)

}
