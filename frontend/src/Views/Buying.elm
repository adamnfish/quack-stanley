module Views.Buying exposing (buying)

import Dict exposing (Dict)
import Html exposing (Html, div, p, span, text, button, ul, li, h2, a, br)
import Html.Attributes exposing (class, placeholder, href, disabled)
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
        products = Maybe.withDefault Dict.empty ( Maybe.map .products model.round )
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
        , container "buying"
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
                               |After they've each had a turn pitching their product,
                               |choose the player whose sales pitch & end product you
                               |most liked from the list below.
                               |"""
                       ]
                    ]
                ]
            , row
                [ col "s12"
                    [ card
                        [ otherPlayers model.opponents products role ]
                    ]
                ]
            ]
        )

otherPlayers : List PlayerSummary -> Dict String ( List String ) -> String -> Html Msg
otherPlayers opponents products role =
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
            [ class "awards__list" ]
            ( List.map ( otherPlayer role products ) opponents )

otherPlayer : String ->  Dict String ( List String ) -> PlayerSummary -> Html Msg
otherPlayer role products playerSummary =
    let
        hasPitched = Dict.member playerSummary.screenName products
        productWords = Maybe.withDefault [] ( Dict.get playerSummary.screenName products )
        product =
            if List.isEmpty productWords then
                "Not yet pitched"
            else
                String.join " " productWords
    in
        li
            [ class "awards__li valign-wrapper" ]
            [ div
                [ class "award-name__container left valign-wrapper" ]
                [ p
                    []
                    [ text playerSummary.screenName ]
                ]
            , div
                [ class "award-winner__container left" ]
                [ button
                    [ class "award-winner__button waves-effect waves-light btn purple btn-large"
                    , onClick ( Msg.AwardPoint role playerSummary.screenName )
                    , disabled ( not hasPitched )
                    ]
                    [ text product
                    , icon "favorite" "right"
                    ]
                ]
            ]
