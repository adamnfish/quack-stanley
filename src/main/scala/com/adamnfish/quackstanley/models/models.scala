package com.adamnfish.quackstanley.models

import org.joda.time.DateTime


case class Role(value: String) extends AnyVal
case class Word(value: String) extends AnyVal

case class PlayerKey(value: String) extends AnyVal
case class GameId(value: String) extends AnyVal

// representation of a player
case class PlayerState(
  name: String,
  hand: List[Word],
  discardedWords: List[Word], // these words are no longer available
  role: Option[Role], // current player has a role
  points: List[Role]
)

// private global gamestate
case class GameState(
  startTime: DateTime,
  started: Boolean, // once game has started players cannot be added
  creator: PlayerKey,
  players: Map[PlayerKey, PlayerState]
)

// typed JSON API operations, represent the possible client requests
sealed trait ApiOperation
case class CreateGame(
  name: String
) extends ApiOperation
case class RegisterPlayer(
  gameId: GameId,
  playerKey: PlayerKey,
  playerName: String
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
  gameId: GameId,
  started: Boolean,
  otherPlayers: List[String]
) extends ApiResponse
// registers a user with a game
case class Registered(
  gameId: GameId,
  playerKey: PlayerKey
) extends ApiResponse
object ApiResponse
