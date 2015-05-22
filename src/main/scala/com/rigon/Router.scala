package com.rigon

import com.rigon.ConsoleUtils.colored
import com.twitter.finagle.http.path.{Path, Root}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finagle.service.{LocalRateLimitingStrategy, RateLimitingFilter}
import com.twitter.finagle.{Service, SimpleFilter}
import com.twitter.util.Duration.fromSeconds
import org.jboss.netty.handler.codec.http.HttpMethod

class Router(filters: List[SimpleFilter[Request, Response] ],
             rateLimitDuration: Int,
             requestsPerDuration: Int,
             routes: List[AnnotatedRoute[_]],
             cache: Cache
             ) extends Log {

  val rateLimitFilter = new RateLimitingFilter[Request, Response](
    new LocalRateLimitingStrategy[Request](_.remoteHost, fromSeconds(rateLimitDuration), requestsPerDuration)
  )

  //new RequestLogFilter andThen corsFilter andThen exceptionFilter andThen rateLimitFilter

  def create = filters.reduceLeft(_ andThen _) andThen HttpRouter.forRoutes(toRoute(routes: _*))

  def toRoute(list: AnnotatedRoute[_]*) = {
    print(list: _*)
    list.map { route =>
      val service = if (route.isCached) cached(route.handler) else notCached(route.handler)
      asMatcher(route.info, service)
    } reduceLeft (_ orElse _)
  }

  def asMatcher[T](route: Route, service: Service[Request, Response]) = {
    new PartialFunction[(HttpMethod, Path), Service[Request, Response]] {
      override def isDefinedAt(x: (HttpMethod, Path)): Boolean = x._2 == route.path && route.methods.contains(x._1)

      override def apply(v1: (HttpMethod, Path)): Service[Request, Response] = service
    }
  }

  def print(routes: AnnotatedRoute[_]*) = {
    val methods = routes.map { r =>
      val info = r.info
      s"${colored(info.methods.mkString(",").toString, Console.GREEN)}"
    }

    val paths = routes.map { r =>
      val info = r.info
      s"${colored(if(info.path == Root) "/" else info.path.toString, Console.WHITE)}"
    }

    val classes = routes.map { r =>
      s"${colored(r.getClass.getSimpleName, Console.YELLOW)}"
    }

    val logs = (methods zip paths) map (x => s" ${x._1} ~ ${x._2}") zip classes map (x => s"${x._1} => ${x._2}")
    log.info("\n\nRoutes: \n\n" + leftPad(logs).mkString("\n") + "\n")
  }

  def leftPad(list: Seq[String]) = {
    val max = list.map(_.length).max
    list.sortBy(-_.length).map(s => s.padTo(max, " ").mkString)
  }


  def cached[T](handler: (Request => ApiHandler[T])) = new CachedHandler(cache, handler)

  def notCached[T](handler: (Request => ApiHandler[T])) = new NoCacheHandler(handler)
}
