package com.rigon

import com.twitter.util.Future

trait Cache {

  def getAsByteArray(key: String, ttl: Int)(onMiss: => Future[Option[Array[Byte]]]): Future[Option[Array[Byte]]]
}
