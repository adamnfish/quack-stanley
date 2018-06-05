module Views.AwardingPoint exposing (awardingPoint)

import Html exposing (Html, div, text, button, ul, li, p, h2)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, ShroudContent (..))


awardingPoint : String -> String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
awardingPoint role playerName model =
    ( []
    , NoLoadingShroud
    , div
        []
        [ container "awarding-point"
            [ row
                [ col "s12"
                    [ card
                        [ h2
                            []
                            [ text role ]
                        , p
                            []
                            [ text "Awarding point to "
                            , text playerName
                            ]
                        , ul
                            []
                            ( List.map otherPlayer model.opponents )
                        ]
                    ]
                ]
            ]
        ]
    )

otherPlayer : PlayerSummary -> Html Msg
otherPlayer playerSummary =
    li
        []
        [ text playerSummary.screenName ]
