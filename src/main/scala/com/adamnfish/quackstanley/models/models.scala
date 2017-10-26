package com.adamnfish.quackstanley.models

import org.joda.time.DateTime


// typed JSON API operations, represent the possible client requests
sealed trait ApiOperation
case class CreateGame(
  name: String
) extends ApiOperation
case class RegisterPlayer(
  gameId: String,
  playerKey: String,
  playerName: String
) extends ApiOperation
case class AwardPoint(
  gameId: String,
  playerKey: String,
  role: String,
  awardToPlayerWithName: String
) extends ApiOperation
case class Mulligan(
  gameId: String,
  playerKey: String,
  role: String
) extends ApiOperation
case class Ping(
  gameId: String,
  playerKey: String
) extends ApiOperation
object ApiOperation


sealed trait ApiResponse
// data returned to clients after all requests
case class PlayerInfo(
  state: PlayerState,
  gameId: String,
  started: Boolean,
  otherPlayers: List[String]
) extends ApiResponse
// registers a user with a game
case class Registered(
  gameId: String,
  playerKey: PlayerKey
) extends ApiResponse
object ApiResponse

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
