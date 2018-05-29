module Api.Codecs exposing
    ( apiErrsDecoder, registeredDecoder, newGameDecoder
    , playerStateDecoder, playerSummaryDecoder, playerInfoDecoder
    )

import Json.Decode exposing (Decoder, field, string, bool, list, nullable)
import Json.Decode.Pipeline exposing (decode, required, optional)
import Model exposing (PlayerState, PlayerInfo, PlayerSummary, Registered, NewGame, ApiError)


apiErrsDecoder : Decoder ( List ApiError )
apiErrsDecoder =
    field "errors" ( list apiErrorDecoder )

apiErrorDecoder : Decoder ApiError
apiErrorDecoder =
    decode ApiError
        |> required "message" string
        |> optional "context" (Json.Decode.map Just Json.Decode.string) Nothing

playerStateDecoder : Decoder PlayerState
playerStateDecoder =
    decode PlayerState
        |> required "gameId" string
        |> required "gameName" string
        |> required "screenName" string
        |> required "hand" ( list string )
        |> required "discardedWords" ( list string )
        |> required "role" ( nullable string )
        |> required "points" ( list string )

playerSummaryDecoder : Decoder PlayerSummary
playerSummaryDecoder =
    decode PlayerSummary
        |> required "screenName" string
        |> required "points" ( list string )

newGameDecoder : Decoder NewGame
newGameDecoder =
    decode NewGame
        |> required "state" playerStateDecoder
        |> required "playerKey" string
        |> required "gameCode" string

registeredDecoder : Decoder Registered
registeredDecoder =
    decode Registered
        |> required "state" playerStateDecoder
        |> required "playerKey" string

playerInfoDecoder : Decoder PlayerInfo
playerInfoDecoder =
    decode PlayerInfo
        |> required "state" playerStateDecoder
        |> required "started" bool
        |> required "opponents" ( list playerSummaryDecoder )
