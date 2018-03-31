module Views.Welcome exposing (welcome)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, icon)


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
        [ div
            [ class "row" ]
            [ div
                [ class "col s12 m6 center-align" ]
                [ div
                    [ class "card-panel" ]
                    [ button
                        [ class "waves-effect waves-light btn btn-large teal cta__button"
                        , onClick ( Msg.CreatingNewGame "" "" )
                        ]
                        [ text "Create game"
                        , icon "person" "right"
                        ]
                    ]
                ]
            , div
                [ class "col s12 m6 center-align" ]
                [  div
                    [ class "card-panel" ]
                    [ button
                        [ class "waves-effect waves-light btn btn-large cyan cta__button"
                        , onClick ( Msg.JoiningGame "" "" )
                        ]
                        [ text "Join game"
                        , icon "person_add" "right"
                        ]
                    ]
                ]
            ]
        ]

asleep : Html Msg
asleep =
    div
        [ class "container" ]
        [ text "Loading backend" ]
