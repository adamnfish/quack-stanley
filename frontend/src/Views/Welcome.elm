module Views.Welcome exposing (welcome)

import Html exposing (Html, div, text, button)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, icon, gameNav)


welcome : Model -> Html Msg
welcome model =
    if model.backendAwake then
        awake
    else
        asleep

awake : Html Msg
awake =
    div
        []
        [ gameNav []
        , container "welcome"
            [ row
                [ col "s12 m6 center-align"
                    [ card
                        [ button
                            [ class "waves-effect waves-light btn btn-large teal cta__button"
                            , onClick ( Msg.CreatingNewGame "" "" )
                            ]
                            [ text "Create game"
                            , icon "person" "right"
                            ]
                        ]
                    ]
                , col "s12 m6 center-align"
                    [  card
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
        ]

asleep : Html Msg
asleep =
    div
        [ class "container" ]
        [ text "Loading backend" ]
