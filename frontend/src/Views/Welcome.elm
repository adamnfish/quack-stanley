module Views.Welcome exposing (welcome)

import Html exposing (Html, div, ul, li, dl, dt, dd, p, button, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, SavedGame, Lifecycle (..))
import Msg exposing (Msg)
import Time exposing (Time)
import Views.Utils exposing (container, row, col, card, icon, gameNav)


welcome : Model -> Html Msg
welcome model =
    if model.backendAwake then
        awake model.time model.savedGames
    else
        asleep

awake : Time -> List SavedGame -> Html Msg
awake now savedGames =
    div
        []
        [ gameNav []
        , container "welcome"
            [ savedGamesBlock now savedGames
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

savedGamesBlock : Time -> List SavedGame -> Html Msg
savedGamesBlock now savedGames =
    if List.isEmpty savedGames then
        text ""
    else
        row
            [ col "s12"
                [ card
                    [ text "Games in progress:"
                    , ul
                        []
                        ( List.map ( savedGameBlock now ) savedGames )
                    ]
                ]
            ]

savedGameBlock : Time -> SavedGame -> Html Msg
savedGameBlock now game =
    let
        delta = now - game.startTime
        minutes = floor ( Time.inMinutes delta )
        hours = floor ( Time.inHours delta )
        days = floor ( ( Time.inHours delta ) / 24 )
        ago =
            if minutes < 1 then
                "Just now"
            else if minutes == 1 then
                "1 minute ago"
            else if hours < 1 then
                ( toString minutes ) ++ " minutes ago"
            else if hours == 1 then
                "1 hour ago"
            else if days < 1 then
                ( toString hours ) ++ " hours ago"
            else if days == 1 then
                "yesterday"
            else
                ( toString days ) ++ " days ago"
    in
        li
            []
            [ dl
                []
                [ dt
                    []
                    [ text "Game name:" ]
                , dd
                    []
                    [ text game.gameName ]
                , dt
                    []
                    [ text "Screen name" ]
                , dd
                    []
                    [ text game.screenName ]
                ]
            , button
                [ class "waves-effect waves-light btn cyan cta__button"
                , onClick ( Msg.RemoveSavedGame game )
                ]
                [ text "x" ]
            , button
                [ class "waves-effect waves-light btn btn-large cyan cta__button"
                , onClick ( Msg.RejoinGame game )
                ]
                [ text "Rejoin"
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
