module Views.Message exposing (message)

import Html exposing (Html, div, text, p)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (row, col, card)


message : String -> Model -> Html Msg
message contents model =
    div
        [ class "container" ]
        [ row
            [ col "s12"
                [ card
                    [ p
                        []
                        [ text contents ]
                    ]
                ]
            ]
        ]
