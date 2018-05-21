module Subs exposing (subscriptions)

import Model exposing (Model, SavedGame, Lifecycle (Welcome, Waiting, Spectating))
import Msg exposing (Msg (PingEvent, LoadedGames))
import Ports exposing (savedGames)
import Time


subscriptions : Model -> Sub Msg
subscriptions model =
    case model.lifecycle of
        Welcome ->
            savedGames LoadedGames
        Waiting ->
            Time.every ( 5 * Time.second ) PingEvent
        Spectating _ ->
            Time.every ( 15 * Time.second ) PingEvent
        _ ->
            Sub.none
