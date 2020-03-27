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


init : Flags -> ( Model, Cmd Msg )
init flags =
    let
        model =
            { lifecycle = Welcome
            , savedGames = []
            , backendAwake = False
            , time = 0
            , state = Nothing
            , playerKey = Nothing
            , isCreator = False
            , opponents = []
            , round = Nothing
            , apiRoot = flags.apiRoot
            }
    in
    ( model
    , Cmd.batch
        [ wakeServer model
        , Task.perform Msg.WelcomeTick Time.now  -- initialise model with current time
        , fetchSavedGames ()
        ]
    )


main : Program Flags Model Msg
main =
    Browser.element
        { init = init
        , view = \model -> pageTemplate view model
        , update = update
        , subscriptions = subscriptions
        }


type alias Flags =
    { apiRoot : String
    }
