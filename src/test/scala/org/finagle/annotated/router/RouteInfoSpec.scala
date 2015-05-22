package org.finagle.annotated.router

import PathImplicits._
import com.twitter.finagle.http.path.{Root, Path}
import org.jboss.netty.handler.codec.http.HttpMethod._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}


class RouteInfoSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {

  @Route("/foo", GET, POST)
  class MyRouteImplicit{}

  @Route(Path("/foo"), GET, POST)
  class MyRoute{}

  @Route(Root, GET, POST)
  class MyRouteRoot{}

  @Route(Root / "bar", GET, POST)
  class MyRouteRootPath{}

   it should "extract Route annotation value using implicit conversion from string" in {
     val route = Route.getFrom(classOf[MyRouteImplicit])
     route.path shouldBe Path("/foo")
     route.methods shouldBe List(GET, POST)
   }

  it should "extract Route annotation value using path" in {
    val route = Route.getFrom(classOf[MyRoute])
    route.path shouldBe Path("/foo")
    route.methods shouldBe List(GET, POST)
  }

  it should "extract Route annotation value using root" in {
    val route = Route.getFrom(classOf[MyRouteRoot])
    route.path shouldBe Path("/")
    route.methods shouldBe List(GET, POST)
  }

  it should "extract Route annotation value using root + path" in {
    val route = Route.getFrom(classOf[MyRouteRootPath])
    route.path shouldBe Path("/bar")
    route.methods shouldBe List(GET, POST)
  }

  it should "throw exception if no annotation is found" in {
    intercept[IllegalArgumentException]{
      Route.getFrom(classOf[String])
    }
  }
 }
