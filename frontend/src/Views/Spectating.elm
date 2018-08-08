module Views.Spectating exposing (spectating)

import Html exposing (Html, div, text, button, ul, li, h2, h3, span, p)
import Html.Attributes exposing (class, classList, placeholder, disabled, attribute)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, ApiError, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, empty, plural, icon, showErrors, helpText, ShroudContent (..))


spectating : List String -> List ApiError -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
spectating selected errors model =
    let
        hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
        screenName = Maybe.withDefault "" ( Maybe.map .screenName model.state )
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        points = Maybe.withDefault [] ( Maybe.map .points model.state )
        playerScore = List.length points
        opponentScores = List.map ( .points >> List.length ) model.opponents
        leadingScore =
            max
                playerScore
                ( Maybe.withDefault 0 ( List.maximum ( opponentScores ) ) )
    in
        (
            [ button
                [ class "waves-effect waves-light btn green"
                , onClick Msg.NavigateHome
                ]
                [ icon "home" "left"
                , text "Leave game"
                ]
            ]
        , NoLoadingShroud
        , div
            []
            [ container "spectating"
                [ showErrors errors
                , row
                    [ col "s12"
                        [ card
                            [ helpText
                                """|You can take a turn as the **buyer** (below), or choose 2 words
                                   |that together represent a product you will pitch to the buyer.
                                   |
                                   |When it is your turn to **pitch** select "start pitch". You'll
                                   |be able to tap to reveal your words individually or at once as
                                   |you try and sell your product.
                                   |"""
                            ]
                        ]
                    ]
                , row
                    [ col "s12"
                        [ card
                            [ row
                                [ col "m6 s12"
                                    [ div
                                        [ class "hand__container" ]
                                        ( List.map ( handEntry selected ) hand )
                                    ]
                                , col "m6 s12"
                                    [ selectedWords selected
                                    , div
                                        [ class "container--spaced" ]
                                        [ button
                                            ( case selected of
                                                word1 :: word2 :: [] ->
                                                    [ class "waves-effect waves-light btn btn-large indigo cta__button"
                                                    , onClick ( Msg.StartPitch word1 word2 )
                                                    , disabled False
                                                    ]
                                                _ ->
                                                    [ class "waves-effect waves-light btn btn-large indigo cta__button"
                                                    , disabled True
                                                    ]
                                            )
                                            [ text "Start pitch"
                                            , icon "play_arrow" "right"
                                            ]
                                        ]
                                    ]
                                ]
                            ]
                        ]
                    ]
                , row
                    [ col "m6 s12"
                        [ card
                            [ button
                                [ class "waves-effect waves-light btn purple cta__button"
                                , onClick Msg.RequestBuyer
                                ]
                                [ text "Buyer"
                                , icon "play_arrow" "right"
                                ]
                            ]
                        ]
                    ]
                , row
                    [ col "s12"
                        [ ul
                            [ class "collection z-depth-1" ]
                            (
                                [ li
                                    [ class "collection-item" ]
                                    [ div []
                                        [ text "You"
                                        , span
                                            [ classList
                                                [ ("badge", True)
                                                , ("new amber black-text", leadingScore == playerScore && playerScore > 0)
                                                ]
                                            , attribute "data-badge-caption" ( plural "point" ( List.length points ) )
                                            ]
                                            [ text ( toString playerScore ) ]
                                        ]
                                    ]
                                ] ++ List.map ( playerListEntry leadingScore ) model.opponents
                            )
                        ]
                    ]
                ]
            ]
        )

playerListEntry : Int -> PlayerSummary -> Html Msg
playerListEntry leadingScore playerSummary =
    let
        score = List.length playerSummary.points
    in
        li
            [ class "collection-item" ]
            [ div []
                [ text playerSummary.screenName
                , span
                    [ classList
                        [ ("badge", True)
                        , ("amber black-text", leadingScore == score && score > 0)
                        ]
                    , attribute "data-badge-caption" ( plural "point" ( List.length playerSummary.points ) )
                    ]
                    [ text ( toString score ) ]
                ]
            ]

handEntry : List String -> String -> Html Msg
handEntry selected word =
    div
        [ class "hand__card" ]
        [ button
            [ classList
                [ ( "waves-effect waves-light btn bt-flat blue cta__button", True )
                , ( "word--selected", List.member word selected )
                ]
            , onClick ( Msg.SelectWord word selected )
            , disabled ( List.member word selected )
            ]
            [ text word ]
        ]

selectedWords : List String -> Html Msg
selectedWords selected =
    case selected of
        first :: second :: [] ->
            div
                []
                [ selectedWord selected first
                , selectedWord selected second
                ]
        first :: [] ->
            div
                []
                [ selectedWord selected first
                , unselectedWord
                ]
        [] ->
            div
                []
                [ unselectedWord
                , unselectedWord
                ]
        _ ->
            p
                [ class "error" ]
                [ text "Too many words selected" ]

selectedWord : List String -> String -> Html Msg
selectedWord selected word =
    div
        [ class "container--spaced" ]
        [ button
            [ class "waves-effect waves-light btn btn-large cta__button blue"
            , onClick ( Msg.DeselectWord word selected )
            ]
            [ text word
            , icon "backspace" "right"
            ]
        ]

unselectedWord : Html Msg
unselectedWord =
    div
        [ class "container--spaced" ]
        [ button
            [ class "waves-effect waves-light btn btn-large cta__button blue disabled"
            , disabled True
            ]
            [ text "Select word" ]
        ]
