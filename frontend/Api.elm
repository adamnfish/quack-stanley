module Api exposing (wakeServerRequest, createGameRequest)

import Http exposing (stringBody)
import Json.Decode exposing (succeed)


apiUrl = "/api"

wakeServerRequest : Http.Request ()
wakeServerRequest =
    Http.post apiUrl (stringBody "application/json" """{ "operation": "wake" }""") (succeed ())

createGameRequest : String -> String -> Http.Request ()
createGameRequest playerName gameName =
    let
        body = """{ "operation": "create-game", "screenName": \"""" ++ playerName ++ """", "gameName": \"""" ++ gameName ++ """" }"""
    in
        Http.post apiUrl (stringBody "application/json" body) (succeed ())
