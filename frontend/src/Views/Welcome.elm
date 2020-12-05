module Views.Welcome exposing (welcome)

import Html exposing (Html, button, dd, div, dl, dt, em, li, p, span, strong, text, ul)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onInput, onSubmit)
import Model exposing (Lifecycle(..), Model, SavedGame)
import Msg exposing (Msg)
import Time exposing (Posix)
import Views.Utils exposing (ShroudContent(..), card, col, container, empty, gameNav, helpText, icon, row, shroud)


welcome : Model -> ( List (Html Msg), ShroudContent, Html Msg )
welcome model =
    awake model.time model.savedGames model.backendAwake


awake : Int -> List SavedGame -> Bool -> ( List (Html Msg), ShroudContent, Html Msg )
awake now savedGames isAwake =
    ( []
    , LoadingMessage (not isAwake) [ text "Loading" ]
    , container "welcome"
        [ savedGamesBlock now savedGames
        , row
            [ col "s12 m6 center-align"
                [ card
                    [ button
                        [ class "waves-effect waves-light btn btn-large teal cta__button"
                        , onClick (Msg.CreatingNewGame "" "" [])
                        ]
                        [ text "Create game"
                        , icon "gamepad" "right"
                        ]
                    ]
                ]
            , col "s12 m6 center-align"
                [ card
                    [ button
                        [ class "waves-effect waves-light btn btn-large cyan cta__button"
                        , onClick (Msg.JoiningGame "" "" "" [])
                        ]
                        [ text "Join game"
                        , icon "person_add" "right"
                        ]
                    ]
                ]
            ]
        , welcomeHelp
        ]
    )


savedGamesBlock : Int -> List SavedGame -> Html Msg
savedGamesBlock now savedGames =
    if List.isEmpty savedGames then
        empty

    else
        row (List.map (savedGameBlock now) savedGames)


savedGameBlock : Int -> SavedGame -> Html Msg
savedGameBlock now game =
    let
        delta =
            now - game.startTime

        minutes =
            modBy 60 <| floor (toFloat delta / 1000 / 60)

        hours =
            modBy 24 <| floor (toFloat delta / 1000 / 60 / 60)

        days =
            floor (toFloat delta / 1000 / 60 / 60 / 24)

        ago =
            if minutes < 1 then
                "Just now"

            else if minutes == 1 then
                "1 minute ago"

            else if hours < 1 then
                String.fromInt minutes ++ " minutes ago"

            else if hours == 1 then
                "1 hour ago"

            else if days < 1 then
                String.fromInt hours ++ " hours ago"

            else if days == 1 then
                "yesterday"

            else
                String.fromInt days ++ " days ago"
    in
    col "s12 m6"
        [ card
            [ button
                [ class "rejoin--close waves-effect waves-light btn btn-floating brown right"
                , onClick (Msg.RemoveSavedGame game)
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
                , text game.screenName
                ]
            , button
                [ class "waves-effect waves-light btn btn-large cyan cta__button"
                , onClick (Msg.RejoinGame game)
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
                    [ text "Quack Stanley is a party game for 3+ players that rewards quick thinking, wit and comedy."
                    ]
                , p []
                    [ text "How to play:" ]
                , helpText
                    """|One player chooses to become the **buyer**, and is given a **role**.
                       |
                       |Each of the other players chooses 2 words from their hand
                       |that together represent a **product** they think the buyer would like.
                       |
                       |One at a time, they use those words to **pitch** their
                       |product to the buyer.
                       |
                       |After each player has pitched their product the buyer chooses their
                       |favourite pitch. This player wins the round and receives 1 point.
                       |
                       |Now another player can have a turn as the buyer and the same
                       |thing happens again.
                       |
                       |The game keeps going for as long as you are all enjoying yourselves.
                       |"""
                , p []
                    [ text "Top tips:" ]
                , helpText
                    """|Consider the buyer's role when you choose and pitch your
                       |product.
                       |
                       |**Example:** If the buyer is an *Astronaut* and your
                       |hand contains *Hoodie*, *Velcro*, *Barricade*, *Pyjamas*,
                       |*Oven* and *Cone* you might choose *Velcro* and *Pyjamas*.
                       |You could pitch *Velcro Pyjamas* to the buyer by talking about
                       |how hard it is to get a good night's sleep when you're floating
                       |in space. Be creative!
                       |"""
                ]
            ]
        ]
