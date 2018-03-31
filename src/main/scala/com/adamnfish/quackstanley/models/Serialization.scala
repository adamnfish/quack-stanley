package com.adamnfish.quackstanley.models

import com.adamnfish.quackstanley.attempt.{Attempt, Failure}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe._
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

import scala.util.control.NonFatal


object Serialization {
  // dependencyDateTimetypes
  private val dtfmt = ISODateTimeFormat.dateTime()
  implicit val dateTimeEncoder: Encoder[DateTime] = Encoder.encodeString.contramap[DateTime](dtfmt.print)
  implicit val dateTimeDecoder: Decoder[DateTime] = Decoder.decodeString.emap { str =>
    try {
      Right(dtfmt.parseDateTime(str))
    } catch {
      case NonFatal(e) =>
        Left(s"Failed to parse DateTime, ${e.getMessage}")
    }
  }

  // value classes
  implicit val playerKeyEncoder: Encoder[PlayerKey] = Encoder.encodeString.contramap[PlayerKey](_.value)
  implicit val playerKeyDecoder: Decoder[PlayerKey] = Decoder.decodeString.emap(str => Right(PlayerKey(str)))
  implicit val playerKeyAsMapKeyEncoder: KeyEncoder[PlayerKey] = (pk: PlayerKey) => pk.value
  implicit val playerKeyAsMapKeyDecoder: KeyDecoder[PlayerKey] = (pkStr: String) => Some(PlayerKey(pkStr))
  implicit val gameIdEncoder: Encoder[GameId] = Encoder.encodeString.contramap[GameId](_.value)
  implicit val gameIdDecoder: Decoder[GameId] = Decoder.decodeString.emap(str => Right(GameId(str)))
  implicit val roleEncoder: Encoder[Role] = Encoder.encodeString.contramap[Role](_.value)
  implicit val roleDecoder: Decoder[Role] = Decoder.decodeString.emap(str => Right(Role(str)))
  implicit val wordEncoder: Encoder[Word] = Encoder.encodeString.contramap[Word](_.value)
  implicit val wordDecoder: Decoder[Word] = Decoder.decodeString.emap(str => Right(Word(str)))

  // inputs
  implicit val createGameDecoder: Decoder[CreateGame] = deriveDecoder[CreateGame]
  implicit val registerPlayerDecoder: Decoder[RegisterPlayer] = deriveDecoder[RegisterPlayer]
  implicit val startGameDecoder: Decoder[StartGame] = deriveDecoder[StartGame]
  implicit val becomeBuyerDecoder: Decoder[BecomeBuyer] = deriveDecoder[BecomeBuyer]
  implicit val startPitchDecoder: Decoder[StartPitch] = deriveDecoder[StartPitch]
  implicit val finishPitchDecoder: Decoder[FinishPitch] = deriveDecoder[FinishPitch]
  implicit val awardPointDecoder: Decoder[AwardPoint] = deriveDecoder[AwardPoint]
  implicit val mulliganDecoder: Decoder[Mulligan] = deriveDecoder[Mulligan]
  implicit val pingDecoder: Decoder[Ping] = deriveDecoder[Ping]
  implicit val wakeDecoder: Decoder[Wake] = deriveDecoder[Wake]
  implicit val apiOperationDecoder: Decoder[ApiOperation] = Decoder.instance(c =>
    c.downField("operation").as[String].flatMap {
      case "create-game" => c.as[CreateGame]
      case "register-player" => c.as[RegisterPlayer]
      case "start-game" => c.as[StartGame]
      case "become-buyer" => c.as[BecomeBuyer]
      case "start-pitch" => c.as[StartPitch]
      case "finish-pitch" => c.as[FinishPitch]
      case "award-point" => c.as[AwardPoint]
      case "mulligan" => c.as[Mulligan]
      case "ping" => c.as[Ping]
      case "wake" => c.as[Wake]
    }
  )

  // response types
  implicit val playerInfoEncoder: Encoder[PlayerInfo] = deriveEncoder[PlayerInfo]
  implicit val newGameEncoder: Encoder[NewGame] = deriveEncoder[NewGame]
  implicit val registeredEncoder: Encoder[Registered] = deriveEncoder[Registered]
  implicit val okResponseEncoder: Encoder[Ok] = deriveEncoder[Ok]
  implicit val apiResponseEncoder: Encoder[ApiResponse] = Encoder.instance {
    case playerInfo: PlayerInfo => playerInfoEncoder.apply(playerInfo)
    case newGame: NewGame => newGameEncoder.apply(newGame)
    case registered: Registered => registeredEncoder.apply(registered)
    case ok: Ok => okResponseEncoder.apply(ok)
  }

  // persisted types
  implicit val opponentEncoder: Encoder[PlayerSummary] = deriveEncoder[PlayerSummary]
  implicit val opponentDecoder: Decoder[PlayerSummary] = deriveDecoder[PlayerSummary]

  implicit val playerStateEncoder: Encoder[PlayerState] = deriveEncoder[PlayerState]
  implicit val playerStateDecoder: Decoder[PlayerState] = deriveDecoder[PlayerState]

  implicit val gameStateEncoder: Encoder[GameState] = deriveEncoder[GameState]
  implicit val gameStateDecoder: Decoder[GameState] = deriveDecoder[GameState]

  def extractJson[A](json: Json)(implicit decoder: Decoder[A]): Attempt[A] = {
    Attempt.fromEither(json.as[A].left.map { decodingFailure =>
      Failure(s"Failed to parse request body as expected type: ${decodingFailure.message}", "Failed to parse request JSON", 400, Some(decodingFailure.history.mkString("|"))).asAttempt
    })
  }
}
