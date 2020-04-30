package bimplus.services

/** When failing behaviour enumeration */
sealed trait WhenFailingBehaviour

case object Ignore extends WhenFailingBehaviour

case object Abort extends WhenFailingBehaviour

case object Retry extends WhenFailingBehaviour