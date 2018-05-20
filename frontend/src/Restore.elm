port module Restore exposing ()

type alias SavedGame =
    { gameId : String
    , gameName : String
    , playerKey : String
    , screenName : String
    }

port saveGame : SavedGame -> Cmd msg

port fetchSavedGames : Cmd msg

port savedGames : ( List SavedGame -> msg ) -> Sub msg


{-  NOTES:
Wire saveGame into the lifecycle when games start.
(at the point the app goes into spectating)

Call from start game for creator and the ping event for other players
-}
