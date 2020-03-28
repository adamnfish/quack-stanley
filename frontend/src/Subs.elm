module Subs exposing (subscriptions)

import Model exposing (Model, SavedGame, Lifecycle (..))
import Msg exposing (Msg (..))
import Ports exposing (savedGames)
import Time exposing (Posix)


subscriptions : Model -> Sub Msg
subscriptions model =
    case model.lifecycle of
        Welcome ->
            Sub.batch
                [ savedGames LoadedGames
                , Time.every (60 * 1000) WelcomeTick
                ]
        Waiting ->
            Time.every ( 5 * 1000 ) PingEvent
        CreatorWaiting _ _ ->
            Time.every ( 5 * 1000 ) LobbyPingEvent
        Spectating _ _ ->
            Time.every ( 15 * 1000 ) PingEvent
        Buying _ ->
            Time.every ( 5 * 1000 ) PingEvent
        _ ->
            Sub.none
