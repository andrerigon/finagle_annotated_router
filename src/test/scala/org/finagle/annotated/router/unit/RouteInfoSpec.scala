package org.finagle.annotated.router.unit

import org.finagle.annotated.router.PathImplicits._
import com.twitter.finagle.http.path.{Root, Path}
import org.finagle.annotated.router.Route
import com.twitter.finagle.http.Method._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}

class RouteInfoSpec extends FlatSpec with Matchers with MockitoSugar with BeforeAndAfter {

  @Route("/foo", Get, Post)
  class MyRouteImplicit{}

  @Route(Path("/foo"), Get, Post)
  class MyRoute{}

  @Route(Root, Get, Post)
  class MyRouteRoot{}

  @Route(Root / "bar", Get, Post)
  class MyRouteRootPath{}

   it should "extract Route annotation value using implicit conversion from string" in {
     val route = Route.getFrom(classOf[MyRouteImplicit])
     route.path shouldBe Path("/foo")
     route.methods shouldBe List(Get, Post)
   }

  it should "extract Route annotation value using path" in {
    val route = Route.getFrom(classOf[MyRoute])
    route.path shouldBe Path("/foo")
    route.methods shouldBe List(Get, Post)
  }

  it should "extract Route annotation value using root" in {
    val route = Route.getFrom(classOf[MyRouteRoot])
    route.path shouldBe Path("/")
    route.methods shouldBe List(Get, Post)
  }

  it should "extract Route annotation value using root + path" in {
    val route = Route.getFrom(classOf[MyRouteRootPath])
    route.path shouldBe Path("/bar")
    route.methods shouldBe List(Get, Post)
  }

  it should "throw exception if no annotation is found" in {
    intercept[IllegalArgumentException]{
      Route.getFrom(classOf[String])
    }
  }
 }
