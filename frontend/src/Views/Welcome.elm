module Views.Welcome exposing (welcome)

import Html exposing (Html, div, ul, li, text, button)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, SavedGame, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, icon, gameNav)


welcome : Model -> Html Msg
welcome model =
    if model.backendAwake then
        awake model.savedGames
    else
        asleep

awake : List SavedGame -> Html Msg
awake savedGames =
    div
        []
        [ gameNav []
        , container "welcome"
            [ savedGamesBlock savedGames
            , row
                [ col "s12 m6 center-align"
                    [ card
                        [ button
                            [ class "waves-effect waves-light btn btn-large teal cta__button"
                            , onClick ( Msg.CreatingNewGame "" "" )
                            ]
                            [ text "Create game"
                            , icon "person" "right"
                            ]
                        ]
                    ]
                , col "s12 m6 center-align"
                    [  card
                        [ button
                            [ class "waves-effect waves-light btn btn-large cyan cta__button"
                            , onClick ( Msg.JoiningGame "" "" )
                            ]
                            [ text "Join game"
                            , icon "person_add" "right"
                            ]
                        ]
                    ]
                ]
            ]
        ]

savedGamesBlock : List SavedGame -> Html Msg
savedGamesBlock savedGames =
    if List.isEmpty savedGames then
        text ""
    else
        row
            [ col "s12"
                [ card
                    [ text "Games in progress:"
                    , ul
                        []
                        ( List.map savedGameBlock savedGames )
                    ]
                ]
            ]

savedGameBlock : SavedGame -> Html Msg
savedGameBlock game =
    li
        []
        [ button
            [ class "waves-effect waves-light btn btn-large cyan cta__button"
            , onClick ( Msg.RejoinGame game )
            ]
            [ text "Rejoin: "
            , text game.gameName
            , text " "
            , text game.screenName
            ]
        ]


asleep : Html Msg
asleep =
    div
        []
        [ gameNav []
        , container "welcome"
            [ text "Loading backend" ]
        ]
