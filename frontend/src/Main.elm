module Main exposing (..)

import Html exposing (Html, program)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg, update, wakeServer)
import View exposing (view)
import Views.Main exposing (pageTemplate)
import Ports exposing (fetchSavedGames)
import Task exposing (Task)
import Time exposing (Time)
import Subs exposing (subscriptions)


init : ( Model, Cmd Msg )
init =
    ( { lifecycle = Welcome
      , savedGames = []
      , backendAwake = False
      , time = 0
      , state = Nothing
      , playerKey = Nothing
      , isCreator = False
      , opponents = []
      , errs = []
      }
    , Cmd.batch
        [ wakeServer
        , Task.perform Msg.WelcomeTick Time.now  -- initialise model with current time
        , fetchSavedGames ()
        ]
    )


main : Program Never Model Msg
main =
    program
        { init = init
        , view = \model -> pageTemplate view model
        , update = update
        , subscriptions = subscriptions
        }
