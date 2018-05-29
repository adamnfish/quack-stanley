module Views.Message exposing (message)

import Html exposing (Html, div, text, p, button)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon)


message : String -> Model -> ( List ( Html Msg ), Html Msg )
message contents model =
    ( []
    , div
        []
        [ container "message"
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
        ]
    )
