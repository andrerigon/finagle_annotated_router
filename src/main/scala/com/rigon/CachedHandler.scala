package com.rigon

import java.util.concurrent.TimeUnit

import com.rigon.Responses._
import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Path
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import org.jboss.netty.handler.codec.http.{HttpMethod, HttpResponseStatus}

abstract class ApiHandler[T](req: Request) extends Log {

  def ttl = TimeUnit.MINUTES.toSeconds(30).toInt
  def onDataMiss() = {}
  def apply(): Future[Option[T]] = ???
  def cacheKey: String = s"${System.currentTimeMillis()}"
  def onFoundData(resp: T) = {}
  def contentType = "application/json"
}

abstract class AnnotatedRoute[T] {

  def isCached = true

  def handler: Request => ApiHandler[T] = ???

  def info = {
    Route.getFrom(getClass)
  }
}

abstract class SimpleRoute[T](method: HttpMethod, path: Path) extends AnnotatedRoute[T]{
  override def info  = {
    Route(path, method)
  }
}

class NoCacheHandler[T](f: Request => ApiHandler[T]) extends Service[Request, Response] with JsonSupport {
  def apply(req: Request): Future[Response] = {
    implicit val apiHandler = f(req)
    apiHandler.apply map {
      case Some(Seq()) | None => toResponse(None)
      case Some(x) =>
        apiHandler.onFoundData(x)
        x match {
          case x: Array[Byte] => toResponse(Some(x))
          case x: Response => x
          case _ => toResponse(Some(toBytes(x)))
        }
    }
  }

  def toResponse(array: Option[Array[Byte]])(implicit apiHandler: ApiHandler[T]) = array match {
    case Some(bytes) => respond(bytes, contentType = apiHandler.contentType)
    case _ =>
      apiHandler.onDataMiss()
      respond("", HttpResponseStatus.NOT_FOUND)
  }

  val emptyArray = Option.empty[Array[Byte]]
}


class CachedHandler[T](cache: Cache, f: Request => ApiHandler[T]) extends Service[Request, Response] with JsonSupport {

  def apply(req: Request): Future[Response] = {
    implicit val apiHandler = f(req)
    cache.getAsByteArray(apiHandler.cacheKey, apiHandler.ttl)(apiHandler.apply map {
      case Some(Seq()) | None => None
      case Some(x) =>
        apiHandler.onFoundData(x)
        x match {
          case x: Array[Byte] => Some(x)
          case _ => Some(toBytes(x))
        }
    }) map toResponse
  }

  def toResponse(array: Option[Array[Byte]])(implicit apiHandler: ApiHandler[T]) = array match {
    case Some(bytes) => respond(bytes, contentType = apiHandler.contentType)
    case _ =>
      apiHandler.onDataMiss()
      respond("", HttpResponseStatus.NOT_FOUND)
  }

  val emptyArray = Option.empty[Array[Byte]]
}
