package com.finagle.annotated.router

import org.apache.log4j.Logger

trait Log {
  val log = Logger.getLogger(getClass)
}
