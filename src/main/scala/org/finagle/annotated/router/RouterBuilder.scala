package org.finagle.annotated.router

import com.twitter.finagle.Service
import com.twitter.finagle.http.path.{Path, Root}
import com.twitter.finagle.http.service.RoutingService
import com.twitter.finagle.http.{Request, Response}
import com.twitter.util.Future
import ConsoleUtils.colored
import Route.getFrom
import org.jboss.netty.handler.codec.http.HttpMethod

class RouterBuilder[T,R <: Request](f: (T => Service[R, Response]), routes: T*) extends Log {


  def create = {
    assertNoDuplicates
    createRoutingService(
      routes.map { route =>
        asMatcher(getFrom(route.getClass), f(route))
      } reduceLeft (_ orElse _)
    )
  }

  def assertNoDuplicates = {
    val infos = routes.map(r => getFrom(r.getClass))

    infos.foreach { r =>
      val samePath = infos.filter(_.path == r.path)
      if (samePath.size > 1) {
        samePath.map(_.methods).reduceLeft { (a, b) => {
          val intersect = a intersect b
          if (intersect.nonEmpty) {
            val duplicatedClasses = routes.filter(x => getFrom(x.getClass).path == r.path).map(_.getClass.getSimpleName).mkString(", ")
            throw new IllegalArgumentException(s"path: '${r.path}' contains duplicated implementations: [$duplicatedClasses]")
          }
          a ++ b
        }
        }
      }
    }
  }

  private def createRoutingService(routes: PartialFunction[(HttpMethod, Path), Service[R, Response]]) =
    new RoutingService(
      new PartialFunction[Request, Service[R, Response]] {
        override def isDefinedAt(request: Request): Boolean = routes.isDefinedAt(request.method -> Path(request.path))

        override def apply(request: Request): Service[R, Response] = new Service[R, Response] {
          override def apply(request: R): Future[Response] = routes(request.method -> Path(request.path))(request)
        }
      })

  private def asMatcher(route: Route, service: Service[R, Response]) = {
    new PartialFunction[(HttpMethod, Path), Service[R, Response]] {
      override def isDefinedAt(x: (HttpMethod, Path)): Boolean = x._2 == route.path && route.methods.contains(x._1)

      override def apply(v1: (HttpMethod, Path)): Service[R, Response] = service
    }
  }

  def print() = {
    val methods = routes.map { r =>
      val info = getFrom(r.getClass)
      s"${colored(info.methods.mkString(",").toString, Console.GREEN)}"
    }

    val paths = routes.map { r =>
      val info = getFrom(r.getClass)
      s"${colored(if (info.path == Root) "/" else info.path.toString, Console.WHITE)}"
    }

    val classes = routes.map { r =>
      s"${colored(r.getClass.getSimpleName, Console.YELLOW)}"
    }

    val logs = (methods zip paths) map (x => s" ${x._1} ~ ${x._2}") zip classes map (x => s"${x._1} => ${x._2}")
    log.info("\n\nRoutes: \n\n" + leftPad(logs).mkString("\n") + "\n")
  }

  private def leftPad(list: Seq[String]) = {
    val max = list.map(_.length).max
    list.sortBy(-_.length).map(s => s.padTo(max, " ").mkString)
  }
}