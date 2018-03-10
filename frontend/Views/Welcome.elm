module Views.Welcome exposing (welcome)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton)


welcome : Model -> Html Msg
welcome model =
    if model.backendAwake then
        awake
    else
        asleep

awake : Html Msg
awake =
    div
        [ class "container" ]
        [ qsButton "Create game" ( Msg.CreatingNewGame "" "" )
        , qsButton "Join game" ( Msg.JoiningGame "" "" )
        ]

asleep : Html Msg
asleep =
    div
        [ class "container" ]
        [ text "Loading backend" ]
