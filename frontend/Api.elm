module Api exposing (wakeServerRequest, createGameRequest, joinGameRequest)

import Http exposing (stringBody)
import Json.Decode exposing (succeed)


apiUrl = "/api"

wakeServerRequest : Http.Request ()
wakeServerRequest =
    Http.post apiUrl (stringBody "application/json" """{ "operation": "wake" }""") (succeed ())

createGameRequest : String -> String -> Http.Request ()
createGameRequest gameName screenName =
    let
        body = """{ "operation": "create-game", "screenName": \"""" ++ screenName ++ """", "gameName": \"""" ++ gameName ++ """" }"""
    in
        Http.post apiUrl (stringBody "application/json" body) (succeed ())

joinGameRequest : String -> String -> Http.Request ()
joinGameRequest gameId screenName =
    let
        body = """{ "operation": "register-player", "screenName": \"""" ++ screenName ++ """", "gameId": \"""" ++ gameId ++ """" }"""
    in
        Http.post apiUrl (stringBody "application/json" body) (succeed ())
