module Views.Main exposing (pageTemplate)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (gameNav, icon, shroud, ShroudContent (..))


pageTemplate : (Model -> ( List ( Html Msg ), ShroudContent, Html Msg ) ) -> Model -> Html Msg
pageTemplate view model =
    let
        ( navButtons, shroudContent, content ) = view model
    in
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
            , gameNav navButtons
            , shroud shroudContent
            , main_
                []
                [ content ]
            , footer
                [ class "page-footer" ]
                [ div
                    [ class ( "footer-color " ++ ( lifecycleTheme model.lifecycle ) ) ]
                    []
                ]
            ]

lifecycleTheme : Lifecycle -> String
lifecycleTheme lifecycle =
    case lifecycle of
        Welcome ->
            "green"
        Create _ ->
            "teal"
        CreatorWaiting _ _ ->
            "teal"
        Join _ ->
            "cyan"
        Waiting ->
            "cyan"
        Rejoining _ ->
            "cyan"
        Starting ->
            "blue"
        Spectating _ _ ->
            "blue"
        Pitching _ _ loading ->
            if loading then
                "blue"
            else
                "indigo"
        ChooseRole ->
            "purple"
        BecomingBuyer ->
            "purple"
        RelinquishingBuyer ->
            "blue"
        Buying _ ->
            "purple"
        AwardingPoint _ _ ->
            "purple"
        Error _ ->
            "red"
