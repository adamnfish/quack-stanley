module Subs exposing (subscriptions)

import Model exposing (Model, SavedGame, Lifecycle (Welcome, Waiting, Spectating, Buying))
import Msg exposing (Msg (WelcomeTick, PingEvent, LoadedGames))
import Ports exposing (savedGames)
import Time exposing (Time, minute)


subscriptions : Model -> Sub Msg
subscriptions model =
    case model.lifecycle of
        Welcome ->
            Sub.batch
                [ savedGames LoadedGames
                , Time.every minute WelcomeTick
                ]
        Waiting ->
            Time.every ( 5 * Time.second ) PingEvent
        Spectating _ _ ->
            Time.every ( 15 * Time.second ) PingEvent
        Buying _ ->
            Time.every ( 5 * Time.second ) PingEvent
        _ ->
            Sub.none
