module Views.CreatorWaiting exposing (creatorWaiting)

import Html exposing (Html, div, text, button, input, label, span, br)
import Html.Attributes exposing (class, id, placeholder, value, type_, disabled, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (row, col, card, icon)


creatorWaiting : String -> Model -> Html Msg
creatorWaiting gameCode model =
    div
        [ class "container" ]
        [ row
            [ col "s12"
                [ card
                    [ row
                        [ col "s12 m6"
                            [ div
                                [ class "input-field" ]
                                [ icon "assignment" "prefix"
                                , input
                                    [ class "game-code__input"
                                    , id "game-code"
                                    , value gameCode
                                    , type_ "text"
                                    , disabled True
                                    ]
                                    []
                                , label
                                    [ class "active"
                                    , for "game-code"
                                    ]
                                    [ text "Game code" ]
                                ]
                            ]
                        , col "s12 m6"
                            [ span
                                [ class "flow-text" ]
                                [ text "Other players can use this code to join your game." ]
                            ]
                        ]
                    ]
                ]
            ]
        , row
            [ col "s12"
                [ card
                    [ row
                        [ col "s12 m6 push-m6"
                            [ button
                                [ class "waves-effect waves-light blue btn btn-large cta__button"
                                , onClick Msg.StartingGame
                                ]
                                [ text "Start game"
                                , icon "play_arrow" "right"
                                ]
                            ]
                        , col "s12 m6 pull-m6"
                            [ div
                                []
                                [ icon "group_add" "left medium hide-on-small-only" ]
                            , br
                                [ class "clearfix" ]
                                []
                            , div
                                []
                                [ span
                                    [ class "flow-text" ]
                                    [ text "Wait for other players to join before starting." ]
                                , icon "group_add" "left medium hide-on-med-and-up"
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
