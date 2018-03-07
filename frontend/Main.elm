module Main exposing (..)

import Html exposing (Html, program)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg, update, wakeServer)
import View exposing (view)
import Views.Main exposing (pageTemplate)
import Subs exposing (subscriptions)


init : ( Model, Cmd Msg )
init =
    ( { lifecycle = Welcome
      , backendAwake = False
      , state = Nothing
      , playerKey = Nothing
      , isCreator = False
      , otherPlayers = []
      , errs = []
      }
    , wakeServer
    )


main : Program Never Model Msg
main =
    program
        { init = init
        , view = \model -> pageTemplate view model
        , update = update
        , subscriptions = subscriptions
        }
