package com.rigon

import org.apache.log4j.Logger

trait Log {
  val log = Logger.getLogger(getClass)
}
