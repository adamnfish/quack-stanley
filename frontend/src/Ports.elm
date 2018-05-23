port module Ports exposing (saveGame, fetchSavedGames, savedGames, removeSavedGame)

import Model exposing (Model, SavedGame)


send : String -> String -> String -> String -> Cmd msg
send gameId gameName playerKey screenName =
    sendGameToJS
        { gameId = gameId
        , gameName = gameName
        , playerKey = playerKey
        , screenName = screenName
        , startTime = 0
        }

saveGame : Model -> Cmd msg
saveGame model =
    Maybe.withDefault
        Cmd.none
        ( Maybe.map4
            send
            ( Maybe.map .gameId model.state )
            ( Maybe.map .gameName model.state )
            ( model.playerKey )
            ( Maybe.map .screenName model.state )
        )

port sendGameToJS : SavedGame -> Cmd msg

port fetchSavedGames : () -> Cmd msg

port savedGames : ( List SavedGame -> msg ) -> Sub msg

port removeSavedGame : SavedGame -> Cmd msg
