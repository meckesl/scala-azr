package utils

import org.slf4j.LoggerFactory

class Logger {
  private val logger = LoggerFactory.getLogger(getClass)

  def info(msg: String): Unit = logger.info(msg)
  def warn(msg: String): Unit = logger.warn(msg)
  def error(msg: String): Unit = logger.error(msg)
}
