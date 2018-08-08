module Views.Buying exposing (buying)

import Html exposing (Html, div, p, span, text, button, ul, li, h2, a)
import Html.Attributes exposing (class, placeholder, href)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, helpText, icon, empty, ShroudContent (..))


buying : String -> Maybe String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
buying role awardingTo model =
    let
        shroud =
            case awardingTo of
                Nothing ->
                    LoadingMessage False [ empty ]
                Just playerName ->
                    LoadingMessage True
                        [ text "Awarding "
                        , text role
                        , text " to "
                        , text playerName
                        ]
    in
        (
            [ button
                [ class "waves-effect waves-light btn blue"
                , onClick Msg.RelinquishBuyer
                ]
                [ icon "close" "left"
                , text "Cancel"
                ]
            ]
        , shroud
        , div
            []
            [ container "buying"
                [ row
                    [ col "s12"
                        [ card
                            [ span
                                [ class "buyer-role__text" ]
                                [ text role ]
                            ]
                        ]
                    ]
                , row
                    [ col "s12"
                        [ card
                            [ helpText
                                """|Show the rest of the players this role.
                                   |
                                   |The other players will each try to **pitch** a product to
                                   |you as that role.
                                   |
                                   |After they've all had a turn pitching their product,
                                   |choose the player who's sales pitch & end product you
                                   |most liked from the list above.
                                   |"""
                           ]
                        ]
                    ]
                , row
                    [ col "s12"
                        [ card
                            [ otherPlayers model.opponents role ]
                        ]
                    ]
                ]
            ]
        )

otherPlayers : List PlayerSummary -> String -> Html Msg
otherPlayers opponents role =
    if List.isEmpty opponents then
        div
            []
            [ helpText
                """|There are no other players in this game
                   |so there's no one to award the point to.
                   |"""
            , p []
                [ button
                    [ class "waves-effect waves-light btn blue"
                    , onClick Msg.RelinquishBuyer
                    ]
                    [ icon "close" "left"
                    , text "Cancel"
                    ]
                ]
            ]
    else
        ul
            []
            ( List.map ( otherPlayer role ) opponents )

otherPlayer : String ->  PlayerSummary -> Html Msg
otherPlayer role playerSummary =
    li
        []
        [ button
            [ class "pitch-winner__button waves-effect waves-light btn purple btn-large"
            , onClick ( Msg.AwardPoint role playerSummary.screenName )
            ]
            [ text playerSummary.screenName
            , icon "done" "right"
            ]
        ]
