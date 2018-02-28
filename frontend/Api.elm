module Api exposing (wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest, becomeBuyerRequest)

import Http exposing (stringBody)
import Model exposing (PlayerState, PlayerInfo, Registered)
import Json.Decode exposing (Decoder, succeed, string, bool, list, nullable)
import Json.Decode.Pipeline exposing (decode, required, optional)


apiUrl = "/api"


-- API requests

wakeServerRequest : Http.Request ()
wakeServerRequest =
    Http.post apiUrl ( stringBody "application/json" """{ "operation": "wake" }""" ) ( succeed () )

createGameRequest : String -> String -> Http.Request Registered
createGameRequest gameName screenName =
    let
        body = """{ "operation": "create-game", "screenName": \"""" ++ screenName ++ """", "gameName": \"""" ++ gameName ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) ( registeredDecoder )

joinGameRequest : String -> String -> Http.Request Registered
joinGameRequest gameId screenName =
    let
        body = """{ "operation": "register-player", "screenName": \"""" ++ screenName ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) ( registeredDecoder )

startGameRequest : String -> String -> Http.Request PlayerInfo
startGameRequest gameId playerKey =
    let
        body = """{ "operation": "start-game", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) ( playerInfoDecoder )

becomeBuyerRequest : String -> String -> Http.Request PlayerInfo
becomeBuyerRequest gameId playerKey =
    let
        body = """{ "operation": "become-buyer", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) ( playerInfoDecoder )


-- API serialisation

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
        |> required "otherPlayers" ( list string )
