module Views.Welcome exposing (welcome)

import Html exposing (Html, div, ul, li, dl, dt, dd, button, p, strong, em, span, text)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, SavedGame, Lifecycle (..))
import Msg exposing (Msg)
import Time exposing (Time)
import Views.Utils exposing (container, row, col, card, icon, empty, gameNav, shroud, helpText, ShroudContent (..))


welcome : Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
welcome model =
    awake model.time model.savedGames model.backendAwake

awake : Time -> List SavedGame -> Bool -> ( List ( Html Msg ), ShroudContent, Html Msg )
awake now savedGames isAwake =
    ( []
    , LoadingMessage ( not isAwake ) [ text "Waking server" ]
    , div
        []
        [ container "welcome"
            [ savedGamesBlock now savedGames
            , row
                [ col "s12 m6 center-align"
                    [ card
                        [ button
                            [ class "waves-effect waves-light btn btn-large teal cta__button"
                            , onClick ( Msg.CreatingNewGame "" "" [] )
                            ]
                            [ text "Create game"
                            , icon "gamepad" "right"
                            ]
                        ]
                    ]
                , col "s12 m6 center-align"
                    [  card
                        [ button
                            [ class "waves-effect waves-light btn btn-large cyan cta__button"
                            , onClick ( Msg.JoiningGame "" "" [] )
                            ]
                            [ text "Join game"
                            , icon "person_add" "right"
                            ]
                        ]
                    ]
                ]
            , welcomeHelp
            ]
        ]
    )

savedGamesBlock : Time -> List SavedGame -> Html Msg
savedGamesBlock now savedGames =
    if List.isEmpty savedGames then
        empty
    else
        row ( List.map ( savedGameBlock now ) savedGames )

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
        col "s12 m6"
            [ card
                [ button
                    [ class "rejoin--close waves-effect waves-light btn btn-floating brown right"
                    , onClick ( Msg.RemoveSavedGame game )
                    ]
                    [ icon "close" "right" ]
                , div
                    [ class "valign-wrapper" ]
                    [ icon "access_time" "left"
                    , div
                        [ class "rejoin-game__ago-text grey-text" ]
                        [ em
                            []
                            [ text ago ]
                        ]
                    ]
                , p
                    [ class "rejoin-game__game-name valign-wrapper" ]
                    [ icon "gamepad" "left"
                    , strong
                        []
                        [ text game.gameName ]
                    ]
                , p
                    [ class "rejoin-screen-name valign-wrapper" ]
                    [ icon "person" "left"
                    , text game.screenName ]
                , button
                    [ class "waves-effect waves-light btn btn-large cyan cta__button"
                    , onClick ( Msg.RejoinGame game )
                    ]
                    [ text "Rejoin"
                    , icon "group_add" "right"
                    ]
                ]
            ]

welcomeHelp : Html Msg
welcomeHelp =
    row
        [ col "s12"
            [ card
                [ p []
                    [ text "Quack Stanley is a party game for 3+ players that rewards quick thinking, wit and comedy. It is best played in person."
                    ]
                , p []
                    [ text "How to play:" ]
                , helpText
                    """|Players take turns to take on the a role as **buyer**.
                       |Each of the other players then takes a bit of time to choose
                       |2 words from their hand that together represent a **product**
                       |they think the buyer would like.
                       |One at a time, they use those words to **pitch** their
                       |product to the buyer.
                       |
                       |After each player has pitched their product the buyer chooses
                       |the player with their favourite idea & delivery. This player
                       |wins the round and receives 1 point.
                       |
                       |Now another player can have a turn as buyer and the same
                       |thing happens again.
                       |
                       |The game goes on for as long as you are all enjoying yourselves.
                       |Good luck, have fun!
                       |"""
                ]
            ]
        ]
