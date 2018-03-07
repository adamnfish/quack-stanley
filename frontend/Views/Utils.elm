module Views.Utils exposing (qsButton, qsStaticButton)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Msg exposing (Msg)

qsButton : String -> Msg -> Html Msg
qsButton buttonText msg =
    button
        [ class "waves-effect waves-light btn"
        , onClick msg
        ]
        [ text buttonText ]

qsStaticButton : String -> Html Msg
qsStaticButton buttonText =
    button
        [ class "waves-effect waves-light btn" ]
        [ text buttonText ]
