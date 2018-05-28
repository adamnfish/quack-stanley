module Api.Requests exposing
    ( wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest
    , becomeBuyerRequest, awardPointRequest, finishPitchRequest, pingRequest
    )

import Api.Codecs exposing (apiErrsDecoder, registeredDecoder, newGameDecoder, playerStateDecoder, playerSummaryDecoder, playerInfoDecoder)
import Config exposing (apiUrl)
import Http exposing (stringBody)
import Json.Decode exposing (succeed)
import Model exposing (PlayerInfo, Registered, NewGame)



wakeServerRequest : Http.Request ()
wakeServerRequest =
    Http.post apiUrl ( stringBody "application/json" """{ "operation": "wake" }""" ) ( succeed () )

createGameRequest : String -> String -> Http.Request NewGame
createGameRequest gameName screenName =
    let
        body = """{ "operation": "create-game", "screenName": \"""" ++ screenName ++ """", "gameName": \"""" ++ gameName ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) newGameDecoder

joinGameRequest : String -> String -> Http.Request Registered
joinGameRequest gameId screenName =
    let
        body = """{ "operation": "register-player", "screenName": \"""" ++ screenName ++ """", "gameCode": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) registeredDecoder

startGameRequest : String -> String -> Http.Request PlayerInfo
startGameRequest gameId playerKey =
    let
        body = """{ "operation": "start-game", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) playerInfoDecoder

becomeBuyerRequest : String -> String -> Http.Request PlayerInfo
becomeBuyerRequest gameId playerKey =
    let
        body = """{ "operation": "become-buyer", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) playerInfoDecoder

awardPointRequest : String -> String -> String -> String -> Http.Request PlayerInfo
awardPointRequest gameId playerKey role playerName =
    let
        body =
            """{ "operation": "award-point", "gameId": \"""" ++ gameId ++
                """", "playerKey": \"""" ++ playerKey ++
                    """", "role": \"""" ++ role ++
                        """", "awardToPlayerWithName": \"""" ++ playerName ++
                            """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) playerInfoDecoder

finishPitchRequest : String -> String ->  ( String, String ) -> Http.Request PlayerInfo
finishPitchRequest gameId playerKey ( word1, word2 ) =
    let
        body = """{ "operation": "finish-pitch", "playerKey": \""""
               ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """", "words": [\""""
                   ++ word1 ++ """", \"""" ++ word2 ++ """"] }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) playerInfoDecoder

pingRequest : String -> String -> Http.Request PlayerInfo
pingRequest gameId playerKey =
    let
        body = """{ "operation": "ping", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl ( stringBody "application/json" body ) playerInfoDecoder
