module Views.Standings exposing (standings)

import Html exposing (Html, div, text, button, ul, li, span)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, plural, ShroudContent (..))


standings : Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
standings model =
    let
        points = Maybe.withDefault [] ( Maybe.map .points model.state )
    in
        ( []
        , NoLoadingShroud
        , div
            []
            [ container "standings"
                [ row
                    [ col "s12"
                        [ ul
                            [ class "collection z-depth-1" ]
                            (
                                [ li
                                    [ class "collection-item" ]
                                    [ text "You"
                                    , span
                                        [ class "badge"
                                        , attribute "data-badge-caption" ( plural "point" ( List.length points ) )
                                        ]
                                        [ text ( toString ( List.length points ) ) ]
                                    ]
                                ] ++ List.map playerListEntry model.opponents
                            )
                        ]

                    ]
                ]
            ]
        )

playerListEntry : PlayerSummary -> Html Msg
playerListEntry playerSummary =
    li
        [ class "collection-item" ]
        [ span
            [ class "badge"
            , attribute "data-badge-caption" ( plural "point" ( List.length playerSummary.points ) )
            ]
            [ text ( toString ( List.length playerSummary.points ) ) ]
        , text playerSummary.screenName
        ]
