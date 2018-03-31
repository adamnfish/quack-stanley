module Views.Waiting exposing (waiting)

import Html exposing (..)
import Html.Attributes exposing (class, id, placeholder, value, type_, disabled, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, icon)


waiting : Model -> Html Msg
waiting model =
    let
        gameName = Maybe.withDefault "Game name not found" ( Maybe.map .gameName model.state )
    in
    div
        [ class "container" ]
        [ div
            [ class "card-panel" ]
            [ div
                [ class "row" ]
                [ div
                    [ class "col s12 m6" ]
                    [ icon "beenhere" "left medium"
                    , span
                        [ class "flow-text" ]
                        [ text ( "You have joined " ++ gameName ++ "." ) ]
                    ]
                , div
                    [ class "col s12 m6" ]
                    [ icon "hourglass_empty" "right medium hide-on-small-only"
                    , span
                        [ class "flow-text" ]
                        [ text "Waiting for other players to join." ]
                    , icon "hourglass_empty" "left medium hide-on-med-and-up"
                    ]
                ]
            ]
        ]
