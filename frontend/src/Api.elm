module Api exposing (qsSend, wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest, becomeBuyerRequest, awardPointRequest, pingRequest, finishPitchRequest)

import Http exposing (stringBody)
import Model exposing (PlayerState, PlayerInfo, PlayerSummary, Registered, NewGame, ApiError, ApiResponse (..))
import Json.Decode exposing (Decoder, decodeString, field, succeed, maybe, string, bool, list, nullable)
import Json.Decode.Pipeline exposing (decode, required, optional)
import Config exposing (apiUrl)


-- API requests

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


qsSend : (ApiResponse a -> msg) -> Http.Request a -> Cmd msg
qsSend toMessage request =
    Http.send ( toMessage << handleApiResponse ) request


-- API errors

handleApiResponse : Result Http.Error a -> ApiResponse a
handleApiResponse result =
    case result of
        Ok a ->
            ApiOk a
        Err ( Http.BadStatus response ) ->
            case ( decodeString apiErrsDecoder response.body ) of
                Ok apiErrors ->
                    ApiErr apiErrors
                Err _ ->
                    ApiErr [ { message = "Invalid response from server", context = Nothing } ]
        Err ( Http.BadUrl message ) ->
            ApiErr [ { message = message, context = Nothing } ]
        Err Http.Timeout ->
            ApiErr [ { message = "Request timed out", context = Nothing } ]
        Err Http.NetworkError ->
            ApiErr [ { message = "Connection error", context = Nothing } ]
        Err ( Http.BadPayload message response ) ->
            ApiErr [ { message = message, context = Nothing } ]

apiErrsDecoder : Decoder ( List ApiError )
apiErrsDecoder =
    field "errors" ( list apiErrorDecoder )

apiErrorDecoder : Decoder ApiError
apiErrorDecoder =
    decode ApiError
        |> required "message" string
        |> optional "context" (Json.Decode.map Just Json.Decode.string) Nothing


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
