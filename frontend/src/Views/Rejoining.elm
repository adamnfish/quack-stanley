module Views.Rejoining exposing (rejoining)

import Html exposing (Html, div, strong, span, text)
import Html.Attributes exposing (class, id, placeholder, value, type_, disabled, for)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon)


rejoining : Model -> ( List ( Html Msg ), Html Msg )
rejoining model =
    let
        gameName = Maybe.withDefault "Game name not found" ( Maybe.map .gameName model.state )
    in
        ( []
        , div
            []
            [ container "rejoining"
                [ card
                    [ row
                        [ col "s12 m6"
                            [ icon "gamepad" "left medium"
                            , span
                                [ class "flow-text" ]
                                [ text "Re-joining "
                                , strong
                                    []
                                    [ text gameName ]
                                , text "."
                                ]
                            ]
                        , col "s12 m6"
                            [ icon "hourglass_empty" "right medium hide-on-small-only"
                            , span
                                [ class "flow-text" ]
                                [ text "Fetching game data." ]
                            , icon "hourglass_empty" "left medium hide-on-med-and-up"
                            ]
                        ]
                    ]
                ]
            ]
        )
