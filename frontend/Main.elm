module Main exposing (..)

import Html exposing (Html, program)
import Model exposing (Model, Game, Player, Lifecycle (..))
import Msg exposing (Msg, update, wakeServer)
import View exposing (view)
import Subs exposing (subscriptions)


init : ( Model, Cmd Msg )
init = (
        { lifecycle = Welcome
        , backendAwake = False
        , game = Nothing
        , player = Nothing
        }
       , wakeServer
       )


main : Program Never Model Msg
main =
    program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }
