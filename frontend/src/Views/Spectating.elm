module Views.Spectating exposing (spectating)

import Html exposing (Html, div, text, button, ul, li, h2, h3, span, p)
import Html.Attributes exposing (class, classList, placeholder, disabled, attribute)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, plural, icon, friendlyError)


spectating : List String -> Model -> Html Msg
spectating selected model =
    let
        hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
        screenName = Maybe.withDefault "" ( Maybe.map .screenName model.state )
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        points = Maybe.withDefault [] ( Maybe.map .points model.state )
    in
    div
        []
        [ gameNav []
        , container "spectating"
            [ friendlyError model
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
                                [ text "You"
                                , span
                                    [ class "badge"
                                    , attribute "data-badge-caption" ( plural "point" ( List.length points ) )
                                    ]
                                    [ text ( toString ( List.length points ) ) ]
                                ]
                            ] ++ List.map playerListEntry model.opponents
                        )
                    ]
                ]
            ]
        ]

playerListEntry : PlayerSummary -> Html Msg
playerListEntry playerSummary =
    li
        [ class "collection-item" ]
        [ span
            [ class "badge"
            , attribute "data-badge-caption" ( plural "point" ( List.length playerSummary.points ) )
            ]
            [ text ( toString ( List.length playerSummary.points ) ) ]
        , text playerSummary.screenName
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
