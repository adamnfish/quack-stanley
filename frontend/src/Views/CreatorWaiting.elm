module Views.CreatorWaiting exposing (creatorWaiting)

import Html exposing (Html, div, text, button, input, label, span, ul, li)
import Html.Attributes exposing (class, id, value, type_, disabled, for)
import Html.Events exposing (onClick)
import Model exposing (ApiError, Lifecycle(..), Model, PlayerSummary)
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, icon, helpText, showErrors, ShroudContent (..))


creatorWaiting : String -> List ApiError -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
creatorWaiting gameCode errors model =
    ( []
    , NoLoadingShroud
    , container "creator-waiting"
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
                                    , onClick ( Msg.StartingGame gameCode )
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
                    (
                        [ li
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
                        ] ++ List.map lobbyPlayer model.opponents
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