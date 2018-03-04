module Subs exposing (subscriptions)

import Model exposing (Model)
import Msg exposing (Msg (PingEvent))
import Time


subscriptions : Model -> Sub Msg
subscriptions model =
    Time.every ( 5 * Time.second ) PingEvent
