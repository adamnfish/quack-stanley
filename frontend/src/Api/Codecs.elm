module Api.Codecs exposing
    ( apiErrsDecoder
    , newGameDecoder
    , playerInfoDecoder
    , playerStateDecoder
    , playerSummaryDecoder
    , registeredDecoder
    )

import Json.Decode exposing (Decoder, bool, dict, field, list, nullable, string)
import Json.Decode.Pipeline exposing (optional, required)
import Model exposing (ApiError, NewGame, PlayerInfo, PlayerState, PlayerSummary, Registered, Round)


apiErrsDecoder : Decoder (List ApiError)
apiErrsDecoder =
    field "errors" (list apiErrorDecoder)


apiErrorDecoder : Decoder ApiError
apiErrorDecoder =
    Json.Decode.succeed ApiError
        |> required "message" string
        |> optional "context" (Json.Decode.map Just Json.Decode.string) Nothing


playerStateDecoder : Decoder PlayerState
playerStateDecoder =
    Json.Decode.succeed PlayerState
        |> required "gameId" string
        |> required "gameName" string
        |> required "screenName" string
        |> required "hand" (list string)
        |> required "discardedWords" (list string)
        |> required "role" (nullable string)
        |> required "points" (list string)


playerSummaryDecoder : Decoder PlayerSummary
playerSummaryDecoder =
    Json.Decode.succeed PlayerSummary
        |> required "screenName" string
        |> required "points" (list string)


newGameDecoder : Decoder NewGame
newGameDecoder =
    Json.Decode.succeed NewGame
        |> required "state" playerStateDecoder
        |> required "playerKey" string
        |> required "gameCode" string


registeredDecoder : Decoder Registered
registeredDecoder =
    Json.Decode.succeed Registered
        |> required "state" playerStateDecoder
        |> required "playerKey" string


roundDecoder : Decoder Round
roundDecoder =
    Json.Decode.succeed Round
        |> required "buyer" string
        |> required "role" string
        |> required "products" (dict (list string))


playerInfoDecoder : Decoder PlayerInfo
playerInfoDecoder =
    Json.Decode.succeed PlayerInfo
        |> required "state" playerStateDecoder
        |> required "started" bool
        |> required "opponents" (list playerSummaryDecoder)
        |> optional "round" (Json.Decode.map Just roundDecoder) Nothing
