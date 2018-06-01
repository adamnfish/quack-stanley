module Views.Main exposing (pageTemplate)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (gameNav, icon)


pageTemplate : (Model -> ( List ( Html Msg ), Html Msg ) ) -> Model -> Html Msg
pageTemplate view model =
    let
        ( navButtons, content ) = view model
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
        Create _ _ ->
            "teal"
        Creating _ _ ->
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
