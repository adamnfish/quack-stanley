package com.adamnfish.quackstanley

import io.circe.{Decoder, Encoder}
import io.circe.generic.extras.semiauto._
import org.joda.time.DateTime


// typed JSON API operations, represent the possible client requests
trait ApiOperation
case class CreateGame(
  name: String
) extends ApiOperation
object CreateGame {
  implicit val decoder: Decoder[CreateGame] = deriveDecoder
}
case class RegisterPlayer(
  gameId: String,
  playerKey: String,
  playerName: String
) extends ApiOperation
object RegisterPlayer {
  implicit val decoder: Decoder[RegisterPlayer] = deriveDecoder
}
case class AwardPoint(
  gameId: String,
  playerKey: String,
  role: String,
  awardToPlayerWithName: String
) extends ApiOperation
object AwardPoint {
  implicit val decoder: Decoder[AwardPoint] = deriveDecoder
}
case class Mulligan(
  gameId: String,
  playerKey: String,
  role: String
) extends ApiOperation
object Mulligan {
  implicit val decoder: Decoder[Mulligan] = deriveDecoder
}
case class Ping(
  gameId: String,
  playerKey: String
) extends ApiOperation
object Ping {
  implicit val decoder: Decoder[Ping] = deriveDecoder
}

object ApiOperation {
  implicit val decoder: Decoder[ApiOperation] = Decoder.instance(c =>
    c.downField("type").as[String].flatMap {
      case "create-game" => c.as[CreateGame]
      case "register-player" => c.as[RegisterPlayer]
      case "award-point" => c.as[RegisterPlayer]
      case "mulligan" => c.as[Mulligan]
      case "ping" => c.as[Ping]
    }
  )
}

trait ApiResponse
// data returned to clients after all requests
case class PlayerInfo(
  state: PlayerState,
  gameId: String,
  started: Boolean,
  otherPlayers: List[String]
) extends ApiResponse
object PlayerInfo {
  implicit val encoder: Encoder[PlayerInfo] = deriveEncoder
}
// registers a user with a game
case class Registered(
  gameId: String,
  playerKey: PlayerKey
) extends ApiResponse
object Registered {
  implicit val encoder: Encoder[Registered] = deriveEncoder
}
object ApiResponse {
  implicit val encoder: Encoder[ApiResponse] = Encoder.instance {
    case playerInfo: PlayerInfo => PlayerInfo.encoder.apply(playerInfo)
    case registered: Registered => Registered.encoder.apply(registered)
  }
}

case class GameState(
  startTime: DateTime,
  started: Boolean, // once game has started players cannot be added
  creator: PlayerKey,
  players: Map[PlayerKey, PlayerState]
)

// used to authenticate users
case class PlayerKey(value: String) extends AnyVal

// representation of a player
case class PlayerState(
  name: String,
  hand: List[Word],
  discardedWords: List[Word], // these words are no longer available
  role: Option[Role], // current player has a role
  points: List[Role]
)

// game data
case class Role(value: String) extends AnyVal
case class Word(value: String) extends AnyVal
