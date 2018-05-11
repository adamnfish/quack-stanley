module Views.Main exposing (pageTemplate)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)


pageTemplate : (Model -> Html Msg) -> Model -> Html Msg
pageTemplate view model =
    div
        [ id "app-root" ]
        [ header
            []
            [ nav
                [ class ( lifecycleTheme model.lifecycle ) ]
                [ div
                    [ class "nav-wrapper container" ]
                    [ a
                        [ id "logo-container" ]
                        [ text "Quack Stanley" ]
                    , ul
                        [ class "right hide-on-med-and-down" ]
                        [ li
                            []
                            [ a [ href "" ] [ text "help" ] ]
                        ]
                    , ul
                        [ class "side-nav", id "nav-mobile" ]
                        [ li
                            []
                            [ a [ href "" ] [ text "help" ] ]
                        ]
                    ]
                ]
            ]
        , main_
            []
            [ view model ]
        , footer
            [ class ( "page-footer " ++ ( lifecycleTheme model.lifecycle ) ) ]
            [ div
                [ class "container" ]
                [ div
                    [ class "row" ]
                    [ div
                        [ class "col l9 s12" ]
                        [ text "Quack Stanley is a game written by "
                        , a
                            [ class "a--footer white-text"
                            , href "http://www.adamnfish.com/" ]
                            [ text "adamnfish" ]
                        , text ", inspired by "
                        , a
                            [ class "a--footer white-text"
                            , href "https://boardgamegeek.com/boardgame/113289/snake-oil"
                            ]
                            [ text "Snake Oil" ]
                        , text ", designed by "
                        , a
                            [ class "a--footer white-text"
                            , href "https://boardgamegeek.com/boardgamedesigner/58837/jeff-ochs"
                            ]
                            [ text "Jeff Ochs" ]
                        ]
                    ]
                ]
            ]
        ]

lifecycleTheme : Lifecycle -> String
lifecycleTheme lifecycle =
    case lifecycle of
        Welcome ->
            "green"
        Create _ _ ->
            "teal"
        Creating ->
            "teal"
        CreatorWaiting _ ->
            "teal"
        Join _ _ ->
            "cyan"
        Joining ->
            "cyan"
        Waiting ->
            "cyan"
        Starting ->
            "blue"
        Spectating _ ->
            "blue"
        Pitching _ _ _ ->
            "indigo"
        ChooseRole ->
            "purple"
        BecomingBuyer ->
            "purple"
        Buying _ ->
            "purple"
        AwardingPoint _ _ ->
            "purple"
        Error _ ->
            "red"
