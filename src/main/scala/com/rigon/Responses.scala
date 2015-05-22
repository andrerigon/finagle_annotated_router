package com.rigon

import com.twitter.finagle.http.Response
import org.jboss.netty.buffer.ChannelBuffers.{wrappedBuffer => toBuffer}
import org.jboss.netty.handler.codec.http.HttpResponseStatus

object Responses extends JsonSupport {

  def respondList(obj: Seq[AnyRef]) = obj match {
    case x :: xs => respond(obj)
    case _ => respond("", HttpResponseStatus.NOT_FOUND)
  }

  def respondOption[T](opt: Option[T]) = opt match {
    case Some(x) => respond(x)
    case _ => respond("", HttpResponseStatus.NOT_FOUND)
  }
  
  def respond(obj: Any, status: HttpResponseStatus = HttpResponseStatus.OK, contentType: String = "application/json"): Response = {
    val res = Response()
    res.setStatusCode(status.getCode)
    res.setContentType(contentType)
    res.setContent(serialize(obj))

    // TODO: This must be more restrictive, but for now this will do it.
    res.headers().set("Access-Control-Allow-Origin", "*")
    res
  }

  private def serialize(obj: Any) = {
    obj match {
      case s: String => toBuffer(s.getBytes)
      case a: Array[Byte] => toBuffer(a)
      case _ => toBuffer(toBytes(obj))
    }
  }
}