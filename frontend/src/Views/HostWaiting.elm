module Views.HostWaiting exposing (hostWaiting)

import Html exposing (Html, button, div, input, label, li, span, text, ul)
import Html.Attributes exposing (class, disabled, for, id, type_, value)
import Html.Events exposing (onClick)
import Model exposing (ApiError, Lifecycle(..), Model, PlayerSummary)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, helpText, icon, row, showErrors)


hostWaiting : String -> List ApiError -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
hostWaiting gameCode errors model =
    ( []
    , NoLoadingShroud
    , container "host-waiting"
        [ row
            [ col "s12"
                [ showErrors errors
                , card
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
                        [ col "s12 m6"
                            [ div
                                []
                                [ span
                                    [ class "flow-text" ]
                                    [ text "Wait for other players to join before starting." ]
                                ]
                            ]
                        , col "s12 m6"
                            [ div
                                [ class "start-game__btn" ]
                                [ button
                                    [ class "waves-effect waves-light blue btn btn-large cta__button"
                                    , onClick (Msg.StartingGame gameCode)
                                    ]
                                    [ text "Start game"
                                    , icon "play_arrow" "right"
                                    ]
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        , row
            [ col "s12"
                [ ul
                    [ class "collection with-header z-depth-1" ]
                    ([ li
                        [ class "collection-header" ]
                        [ div []
                            [ icon "person" "left medium"
                            , span
                                [ class "flow-text" ]
                                [ text "Players" ]
                            ]
                        ]
                     , li
                        [ class "collection-item" ]
                        [ div []
                            [ text "You" ]
                        ]
                     ]
                        ++ List.map lobbyPlayer model.opponents
                    )
                ]
            ]
        , row
            [ col "s12"
                [ card
                    [ helpText
                        """|You have created a new game of Quack Stanley.
                           |Other players can join this game using the game's
                           |code.
                           |
                           |Give the other players time to join and then start
                           |the game using the button above.
                           |
                           |**After the game has started additional players
                           |cannot be added.**
                           |"""
                    ]
                ]
            ]
        ]
    )


lobbyPlayer : PlayerSummary -> Html Msg
lobbyPlayer playerSummary =
    li
        [ class "collection-item" ]
        [ div []
            [ text playerSummary.screenName ]
        ]
