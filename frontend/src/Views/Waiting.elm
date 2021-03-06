module Views.Waiting exposing (waiting)

import Html exposing (Html, div, p, span, strong, text)
import Html.Attributes exposing (class, disabled, for, id, placeholder, type_, value)
import Model exposing (Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, gameNav, helpText, icon, row)


waiting : Model -> ( List (Html Msg), ShroudContent, Html Msg )
waiting model =
    let
        gameName =
            Maybe.withDefault "Game name not found" (Maybe.map .gameName model.state)

        screenName =
            Maybe.withDefault "Your name was not found" (Maybe.map .screenName model.state)
    in
    ( []
    , NoLoadingShroud
    , container "waiting"
        [ row
            [ col "s12 m6"
                [ card
                    [ p
                        [ class "card-header valign-wrapper" ]
                        [ icon "gamepad" "left"
                        , strong
                            []
                            [ text gameName ]
                        ]
                    , p
                        [ class "valign-wrapper" ]
                        [ icon "person" "left"
                        , text screenName
                        ]
                    , p
                        [ class "flow-text" ]
                        [ text "Waiting for the game to start" ]
                    ]
                ]
            , col "s12 m6"
                [ card
                    [ helpText
                        "When all players have joined the creator can start the game."
                    ]
                ]
            ]
        ]
    )
