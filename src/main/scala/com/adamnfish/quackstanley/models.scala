package com.adamnfish.quackstanley

import io.circe.Decoder
import io.circe.generic.extras.semiauto._


case class TestArgs(key: Int, key2: Int)
object TestArgs {
  implicit val decoder: Decoder[TestArgs] = deriveDecoder
}
