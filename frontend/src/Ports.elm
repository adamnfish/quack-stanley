port module Ports exposing (saveGame, fetchSavedGames, savedGames, removeSavedGame)

import Model exposing (Model, SavedGame)
import Time exposing (posixToMillis)


send : String -> String -> String -> String -> Int -> Cmd msg
send gameId gameName playerKey screenName time =
    sendGameToJS
        { gameId = gameId
        , gameName = gameName
        , playerKey = playerKey
        , screenName = screenName
        , startTime = time
        }

saveGame : Model -> Cmd msg
saveGame model =
    Maybe.withDefault
        Cmd.none
        ( Maybe.map5
            send
            ( Maybe.map .gameId model.state )
            ( Maybe.map .gameName model.state )
            ( model.playerKey )
            ( Maybe.map .screenName model.state )
            ( Just <| model.time )
        )

port sendGameToJS : SavedGame -> Cmd msg

port fetchSavedGames : () -> Cmd msg

port savedGames : ( List SavedGame -> msg ) -> Sub msg

port removeSavedGame : SavedGame -> Cmd msg
