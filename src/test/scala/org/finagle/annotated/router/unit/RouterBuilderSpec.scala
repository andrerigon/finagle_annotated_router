package org.finagle.annotated.router.unit

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.Root
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Await.result
import com.twitter.util.{Await, Future}
import org.apache.log4j.Logger
import org.finagle.annotated.router.PathImplicits._
import org.finagle.annotated.router.{RouterBuilder, PathImplicits, Route}
import com.twitter.finagle.http.Method
import org.mockito.ArgumentCaptor
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import com.twitter.io.Buf

class RouterBuilderSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {

  @Route("/foo", Method.Get, Method.Post)
  class MyRoute {
    def run() = {
      val r = Response()
      r.content = Buf.Utf8("bar")
      r
    }
  }

  @Route("/foo", Method.Head)
  class MyRoute3  extends MyRoute{
    override def run() = {
      val r = Response()
      r.content = Buf.Utf8("bar")
      r
    }
  }

  @Route(Root, Method.Put)
  class MyRoute2 extends MyRoute {
    override def run() = {
      val r = Response()
      r.content = Buf.Utf8("barbar")
      r
    }
  }

  @Route("/foo", Method.Post)
  class MyRouteDuplicated extends MyRoute {
    override def run() = {
      val r = Response()
      r.content = Buf.Utf8("barbar")
      r
    }
  }

  val f: MyRoute => Service[Request, Response] = a => new Service[Request, Response] {
    override def apply(request: Request): Future[Response] = Future(a.run())
  }

  it should "create router with correct matchers" in {
    val service = new RouterBuilder(f, new MyRoute, new MyRoute2, new MyRoute3).create

    result(service(Request("/foo"))).contentString shouldBe "bar"

    result(service(Request(Method.Post, "/foo"))).contentString shouldBe "bar"

    result(service(Request(Method.Put, "/"))).contentString shouldBe "barbar"

    result(service(Request("/bla"))).statusCode shouldBe 404
  }

  it should "throw exception if there are duplicated routes" in {
    intercept[IllegalArgumentException] {
      new RouterBuilder(f, new MyRoute, new MyRoute2, new MyRouteDuplicated).create
    }
  }

  it should "log correct route info" in {
    val mockedLog = mock[Logger]
    val builder = new RouterBuilder(f, new MyRoute, new MyRoute2) {
      override val log = mockedLog
    }

    builder.print()

    val argumentCaptor = ArgumentCaptor.forClass(classOf[String])

    verify(mockedLog).info(argumentCaptor.capture())

    val result = argumentCaptor.getValue
    result should include("MyRoute")
    result should include("GET")
    result should include("POST")
    result should include("/foo")
  }
}
