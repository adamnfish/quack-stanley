module Main exposing (..)

import Browser
import Html exposing (Html)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg, update, wakeServer)
import View exposing (view)
import Views.Main exposing (pageTemplate)
import Ports exposing (fetchSavedGames)
import Task exposing (Task)
import Time exposing (Posix)
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
      , round = Nothing
      }
    , Cmd.batch
        [ wakeServer
        , Task.perform Msg.WelcomeTick Time.now  -- initialise model with current time
        , fetchSavedGames ()
        ]
    )


main : Program () Model Msg
main =
    Browser.element
        { init = \_ -> init
        , view = \model -> pageTemplate view model
        , update = update
        , subscriptions = subscriptions
        }
