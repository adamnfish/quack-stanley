module Api.Requests exposing
    ( wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest
    , becomeBuyerRequest, relinquishBuyerRequest, awardPointRequest, finishPitchRequest
    , pingRequest
    )

import Api.Codecs exposing (apiErrsDecoder, registeredDecoder, newGameDecoder, playerStateDecoder, playerSummaryDecoder, playerInfoDecoder)
import Config exposing (apiUrl)
import Http exposing (stringBody)
import Json.Decode exposing (succeed)
import Model exposing (Model, NewGame, PlayerInfo, Registered)



wakeServerRequest : Model -> Http.Request ()
wakeServerRequest model =
    Http.post (apiUrl model) ( stringBody "application/json" """{ "operation": "wake" }""" ) ( succeed () )

createGameRequest : Model -> String -> String -> Http.Request NewGame
createGameRequest model gameName screenName =
    let
        body = """{ "operation": "create-game", "screenName": \"""" ++ screenName ++ """", "gameName": \"""" ++ gameName ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) newGameDecoder

joinGameRequest : Model -> String -> String -> Http.Request Registered
joinGameRequest model gameId screenName =
    let
        body = """{ "operation": "register-player", "screenName": \"""" ++ screenName ++ """", "gameCode": \"""" ++ gameId ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) registeredDecoder

startGameRequest : Model -> String -> String -> Http.Request PlayerInfo
startGameRequest model gameId playerKey =
    let
        body = """{ "operation": "start-game", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder

becomeBuyerRequest : Model -> String -> String -> Http.Request PlayerInfo
becomeBuyerRequest model gameId playerKey =
    let
        body = """{ "operation": "become-buyer", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder

relinquishBuyerRequest : Model -> String -> String -> Http.Request PlayerInfo
relinquishBuyerRequest model gameId playerKey =
    let
        body = """{ "operation": "relinquish-buyer", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder

awardPointRequest : Model -> String -> String -> String -> String -> Http.Request PlayerInfo
awardPointRequest model gameId playerKey role playerName =
    let
        body =
            """{ "operation": "award-point", "gameId": \"""" ++ gameId ++
                """", "playerKey": \"""" ++ playerKey ++
                    """", "role": \"""" ++ role ++
                        """", "awardToPlayerWithName": \"""" ++ playerName ++
                            """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder

finishPitchRequest : Model -> String -> String ->  ( String, String ) -> Http.Request PlayerInfo
finishPitchRequest model gameId playerKey ( word1, word2 ) =
    let
        body = """{ "operation": "finish-pitch", "playerKey": \""""
               ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """", "words": [\""""
                   ++ word1 ++ """", \"""" ++ word2 ++ """"] }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder

pingRequest : Model -> String -> String -> Http.Request PlayerInfo
pingRequest model gameId playerKey =
    let
        body = """{ "operation": "ping", "playerKey": \"""" ++ playerKey ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post (apiUrl model) ( stringBody "application/json" body ) playerInfoDecoder
