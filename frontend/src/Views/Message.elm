module Views.Message exposing (message)

import Html exposing (Html, div, text, p, button)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon)


message : String -> Model -> Html Msg
message contents model =
        div
            []
            [ gameNav []
            , container "message"
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
