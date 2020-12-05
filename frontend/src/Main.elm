module Main exposing (..)

import Browser
import Browser.Navigation exposing (Key)
import Model exposing (Lifecycle(..), Model, SavedGame)
import Msg exposing (Msg(..), processUrlChange, update, wakeServer)
import Subs exposing (subscriptions)
import Task exposing (Task)
import Time exposing (Posix)
import Url
import View exposing (view)


init : Flags -> Url.Url -> Key -> ( Model, Cmd Msg )
init flags initialUrl key =
    let
        model =
            { lifecycle = Welcome
            , savedGames = flags.savedGames
            , backendAwake = False
            , time = 0
            , state = Nothing
            , playerKey = Nothing
            , isHost = False
            , opponents = []
            , round = Nothing
            , apiRoot = flags.apiRoot
            , urlKey = key
            , url = initialUrl
            }

        ( updatedModel, urlCmd ) =
            processUrlChange initialUrl model
    in
    ( updatedModel
    , Cmd.batch
        [ wakeServer model
        , Task.perform Msg.WelcomeTick Time.now -- initialise model with current time
        , urlCmd
        ]
    )


main : Program Flags Model Msg
main =
    Browser.application
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        , onUrlRequest = UrlRequested
        , onUrlChange = UrlChanged
        }


type alias Flags =
    { apiRoot : String
    , savedGames : List SavedGame
    }
