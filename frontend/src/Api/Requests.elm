module Api.Requests exposing
    ( awardPointRequest
    , becomeBuyerRequest
    , createGameRequest
    , finishPitchRequest
    , joinGameRequest
    , lobbyPingRequest
    , pingRequest
    , relinquishBuyerRequest
    , startGameRequest
    , wakeServerRequest
    )

import Api.Codecs exposing (newGameDecoder, playerInfoDecoder, registeredDecoder)
import Http exposing (jsonBody)
import Json.Decode exposing (succeed)
import Json.Encode
import Model exposing (Model, NewGame, PlayerInfo, Registered)


wakeServerRequest : Model -> Http.Request ()
wakeServerRequest model =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "wake" )
                ]
    in
    Http.post model.apiRoot (jsonBody json) (succeed ())


createGameRequest : Model -> String -> String -> Http.Request NewGame
createGameRequest model gameName screenName =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "create-game" )
                , ( "screenName", Json.Encode.string screenName )
                , ( "gameName", Json.Encode.string gameName )
                ]
    in
    Http.post model.apiRoot (jsonBody json) newGameDecoder


joinGameRequest : Model -> String -> String -> Http.Request Registered
joinGameRequest model gameId screenName =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "register-player" )
                , ( "screenName", Json.Encode.string screenName )
                , ( "gameCode", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) registeredDecoder


startGameRequest : Model -> String -> String -> Http.Request PlayerInfo
startGameRequest model gameId playerKey =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "start-game" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


becomeBuyerRequest : Model -> String -> String -> Http.Request PlayerInfo
becomeBuyerRequest model gameId playerKey =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "become-buyer" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


relinquishBuyerRequest : Model -> String -> String -> Http.Request PlayerInfo
relinquishBuyerRequest model gameId playerKey =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "relinquish-buyer" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


awardPointRequest : Model -> String -> String -> String -> String -> Http.Request PlayerInfo
awardPointRequest model gameId playerKey role playerName =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "award-point" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                , ( "role", Json.Encode.string role )
                , ( "awardToPlayerWithName", Json.Encode.string playerName )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


finishPitchRequest : Model -> String -> String -> ( String, String ) -> Http.Request PlayerInfo
finishPitchRequest model gameId playerKey ( word1, word2 ) =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "finish-pitch" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                , ( "words", Json.Encode.list Json.Encode.string [ word1, word2 ] )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


pingRequest : Model -> String -> String -> Http.Request PlayerInfo
pingRequest model gameId playerKey =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "ping" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder


lobbyPingRequest : Model -> String -> String -> Http.Request PlayerInfo
lobbyPingRequest model gameId playerKey =
    let
        json =
            Json.Encode.object <|
                [ ( "operation", Json.Encode.string "lobby-ping" )
                , ( "playerKey", Json.Encode.string playerKey )
                , ( "gameId", Json.Encode.string gameId )
                ]
    in
    Http.post model.apiRoot (jsonBody json) playerInfoDecoder
