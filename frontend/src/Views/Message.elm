module Views.Message exposing (message)

import Html exposing (Html, div, text, p, button)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, ShroudContent (..))


message : String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
message contents model =
    ( []
    , NoLoadingShroud
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
