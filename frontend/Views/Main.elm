module Views.Main exposing (pageTemplate)

import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)


pageTemplate : (Model -> Html Msg) -> Model -> Html Msg
pageTemplate view model =
    div
        [ id "root" ]
        [ header
            []
            [ nav
                [ class "cyan darken-4" ]
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
            [ class "page-footer cyan darken-4" ]
            [ div
                [ class "container" ]
                [ div
                    [ class "row" ]
                    [ div
                        [ class "col l9 s12" ]
                        [ text "Quack Stanley is a game written by "
                        , a [ href "http://www.adamnfish.com/" ] [ text "adamnfish" ]
                        , text ", inspired by "
                        , a [ href "https://boardgamegeek.com/boardgame/113289/snake-oil" ] [ text "Snake Oil" ]
                        , text ", designed by "
                        , a [ href "https://boardgamegeek.com/boardgamedesigner/58837/jeff-ochs" ] [ text "Jeff Ochs" ]
                        ]
                    ]
                ]
            ]
        ]