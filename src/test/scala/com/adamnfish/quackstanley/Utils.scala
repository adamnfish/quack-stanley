package com.adamnfish.quackstanley

import java.io.ByteArrayInputStream
import java.nio.charset.Charset

object Utils {
  implicit class TestString(val str: String) extends AnyVal {
    def asStream() = new ByteArrayInputStream(str.getBytes(Charset.forName("UTF-8")))
  }
}
