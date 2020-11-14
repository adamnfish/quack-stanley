module Main exposing (..)

import Browser
import Model exposing (Lifecycle(..), Model)
import Msg exposing (Msg, update, wakeServer)
import Ports exposing (fetchSavedGames)
import Subs exposing (subscriptions)
import Task exposing (Task)
import Time exposing (Posix)
import View exposing (view)


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
        , Task.perform Msg.WelcomeTick Time.now -- initialise model with current time
        , fetchSavedGames ()
        ]
    )


main : Program Flags Model Msg
main =
    Browser.element
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }


type alias Flags =
    { apiRoot : String
    }
