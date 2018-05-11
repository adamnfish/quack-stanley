module Views.Message exposing (message)

import Html exposing (Html, div, text, p)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card)


message : String -> Model -> Html Msg
message contents model =
    container "message"
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
