package com.adamnfish.quackstanley.models

import io.circe.generic.extras.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}


object Serialization {
  implicit val createGameDecoder: Decoder[CreateGame] = deriveDecoder
  implicit val registerPlayerDecoder: Decoder[RegisterPlayer] = deriveDecoder
  implicit val awardPointDecoder: Decoder[AwardPoint] = deriveDecoder
  implicit val mulliganDecoder: Decoder[Mulligan] = deriveDecoder
  implicit val pingDecoder: Decoder[Ping] = deriveDecoder
  implicit val apiOperationDecoder: Decoder[ApiOperation] = Decoder.instance(c =>
    c.downField("operation").as[String].flatMap {
      case "create-game" => c.as[CreateGame]
      case "register-player" => c.as[RegisterPlayer]
      case "award-point" => c.as[RegisterPlayer]
      case "mulligan" => c.as[Mulligan]
      case "ping" => c.as[Ping]
    }
  )

  implicit val playerKeyencoder: Encoder[PlayerKey] = Encoder.encodeString.contramap[PlayerKey](_.value)
  implicit val playerKeyDecoder: Decoder[PlayerKey] = Decoder.decodeString.emap(str => Right(PlayerKey(str)))

  implicit val playerInfoencoder: Encoder[PlayerInfo] = deriveEncoder
  implicit val registeredEncoder: Encoder[Registered] = deriveEncoder
  implicit val apiResponseencoder: Encoder[ApiResponse] = Encoder.instance {
    case playerInfo: PlayerInfo => playerInfoencoder.apply(playerInfo)
    case registered: Registered => registeredEncoder.apply(registered)
  }
  implicit val playerStateEncoder: Encoder[PlayerState] = deriveEncoder

  implicit val roleEncoder: Encoder[Role] = Encoder.encodeString.contramap[Role](_.value)
  implicit val wordEncoder: Encoder[Word] = Encoder.encodeString.contramap[Word](_.value)
}
