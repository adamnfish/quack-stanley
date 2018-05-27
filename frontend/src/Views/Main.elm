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
                        [ id "logo-container"
                        , class ( lifecycleTheme model.lifecycle )
                        ]
                        [ img
                            [ src "/images/stanley.png"
                            , alt "Quack Stanley"
                            , width 80
                            , height 80
                            ]
                            []
                        ]
                    ]
                ]
            ]
        , main_
            []
            [ view model ]
        , footer
            [ class ( "page-footer " ++ ( lifecycleTheme model.lifecycle ) ) ]
            [ ]
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
        Rejoining ->
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
