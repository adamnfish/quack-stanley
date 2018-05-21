port module Ports exposing (saveGame, fetchSavedGames, savedGames)

import Model exposing (Model, SavedGame)


send : String -> String -> String -> String -> Cmd msg
send gameId gameName playerKey screenName =
    sendGameToJS { gameId     = gameId
                 , gameName   = gameName
                 , playerKey  = playerKey
                 , screenName = screenName
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


{-  NOTES:
Wire saveGame into the lifecycle when games start.
(at the point the app goes into spectating)

Call from start game for creator and the ping event for other players
-}
