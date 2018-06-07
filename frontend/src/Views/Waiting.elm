module Views.Waiting exposing (waiting)

import Html exposing (Html, div, p, strong, span, text)
import Html.Attributes exposing (class, id, placeholder, value, type_, disabled, for)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, ShroudContent (..))


waiting : Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
waiting model =
    let
        gameName = Maybe.withDefault "Game name not found" ( Maybe.map .gameName model.state )
        screenName = Maybe.withDefault "Your name was not found" ( Maybe.map .screenName model.state )
    in
        ( []
        , NoLoadingShroud
        , div
            []
            [ container "waiting"
                [ row
                    [ col "s12"
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
                    ]
                ]
            ]
        )
