module Main exposing (..)

import Html exposing (Html, program)
import Model exposing (Model, Game, Player, Lifecycle (..))
import Msg exposing (Msg, update)
import View exposing (view)
import Subs exposing (subscriptions)


init : ( Model, Cmd Msg )
init = (
        { lifecycle = Welcome
        , backendReady = False
        , game = Nothing
        , player = Nothing
        }
       , Cmd.none
       )


main : Program Never Model Msg
main =
    program
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }

