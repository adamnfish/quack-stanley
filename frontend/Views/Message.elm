module Views.Message exposing (message)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton)


message : String -> Model -> Html Msg
message contents model =
    div
        [ class "container" ]
        [ div
            [ class "row" ]
            [ div
                [ class "col s12" ]
                [ div
                    [ class "card-panel" ]
                    [ p
                        []
                        [ text contents ]
                    ]
                ]
            ]
        ]
