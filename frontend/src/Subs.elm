module Subs exposing (subscriptions)

import Model exposing (Model, Lifecycle (Waiting, Spectating))
import Msg exposing (Msg (PingEvent))
import Time


subscriptions : Model -> Sub Msg
subscriptions model =
    case model.lifecycle of
        Waiting ->
            Time.every ( 5 * Time.second ) PingEvent
        Spectating _ ->
            Time.every ( 15 * Time.second ) PingEvent
        _ ->
            Sub.none
