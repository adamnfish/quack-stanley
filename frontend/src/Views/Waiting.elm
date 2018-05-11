module Views.Waiting exposing (waiting)

import Html exposing (Html, div, text, span)
import Html.Attributes exposing (class, id, placeholder, value, type_, disabled, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, icon)


waiting : Model -> Html Msg
waiting model =
    let
        gameName = Maybe.withDefault "Game name not found" ( Maybe.map .gameName model.state )
    in
    container "waiting"
        [ card
            [ row
                [ col "s12 m6"
                    [ icon "beenhere" "left medium"
                    , span
                        [ class "flow-text" ]
                        [ text ( "You have joined " ++ gameName ++ "." ) ]
                    ]
                , col "s12 m6"
                    [ icon "hourglass_empty" "right medium hide-on-small-only"
                    , span
                        [ class "flow-text" ]
                        [ text "Waiting for other players to join." ]
                    , icon "hourglass_empty" "left medium hide-on-med-and-up"
                    ]
                ]
            ]
        ]
