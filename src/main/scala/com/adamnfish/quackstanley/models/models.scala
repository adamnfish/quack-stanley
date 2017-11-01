package com.adamnfish.quackstanley.models

import org.joda.time.DateTime


case class Role(value: String) extends AnyVal
case class Word(value: String) extends AnyVal

case class PlayerKey(value: String) extends AnyVal
case class GameId(value: String) extends AnyVal

// representation of a player
case class PlayerState(
  gameId: GameId,
  gameName: String,
  screenName: String,
  hand: List[Word],
  discardedWords: List[Word], // these words are no longer available
  role: Option[Role], // current player has a role
  points: List[Role]
)

// private global gamestate
case class GameState(
  gameId: GameId,
  gameName: String,
  startTime: DateTime,
  started: Boolean, // once game has started players cannot be added
  creator: PlayerKey,
  players: Map[PlayerKey, String]
)

// typed JSON API operations, represent the possible client requests
sealed trait ApiOperation
case class CreateGame(
  screenName: String,
  gameName: String
) extends ApiOperation
case class RegisterPlayer(
  gameId: GameId,
  playerName: String
) extends ApiOperation
case class StartGame(
  gameId: GameId,
  playerKey: PlayerKey
) extends ApiOperation
case class AwardPoint(
  gameId: GameId,
  playerKey: PlayerKey,
  role: Role,
  awardToPlayerWithName: String
) extends ApiOperation
case class Mulligan(
  gameId: GameId,
  playerKey: PlayerKey,
  role: Role
) extends ApiOperation
case class FinishPitch(
  gameId: GameId,
  playerKey: PlayerKey,
  words: (Word, Word)
) extends ApiOperation
case class Ping(
  gameId: GameId,
  playerKey: PlayerKey
) extends ApiOperation
object ApiOperation


sealed trait ApiResponse
// data returned to clients after all requests
case class PlayerInfo(
  state: PlayerState,
  started: Boolean,
  otherPlayers: List[String]
) extends ApiResponse
// registers a user with a game
case class Registered(
  state: PlayerState,
  playerKey: PlayerKey
) extends ApiResponse
object ApiResponse
