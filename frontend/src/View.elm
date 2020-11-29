module View exposing (view)

import Html exposing (..)
import Html.Attributes exposing (..)
import Model exposing (Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Buying exposing (buying)
import Views.Create exposing (create)
import Views.Error exposing (error)
import Views.HostWaiting exposing (hostWaiting)
import Views.Join exposing (join)
import Views.Message exposing (message)
import Views.Pitching exposing (pitching)
import Views.Rejoining exposing (rejoining)
import Views.Spectating exposing (spectating)
import Views.Utils exposing (ShroudContent(..), gameNav, shroud)
import Views.Waiting exposing (waiting)
import Views.Welcome exposing (welcome)


view : Model -> Html Msg
view model =
    let
        ( navButtons, shroudContent, content ) =
            dispatchView model

        theme =
            lifecycleTheme model.lifecycle
    in
    div
        [ id "app-root" ]
        [ header
            []
            [ nav
                [ class theme ]
                [ div
                    [ class "nav-wrapper container" ]
                    [ a
                        [ id "logo-container"
                        , class theme
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
                [ class ("footer-color " ++ theme) ]
                []
            ]
        ]


dispatchView : Model -> ( List (Html Msg), ShroudContent, Html Msg )
dispatchView model =
    case model.lifecycle of
        Welcome ->
            welcome model

        Create createState ->
            create createState.loading createState.gameName createState.screenName createState.errors model

        Join joinState ->
            join joinState.loading joinState.gameCode joinState.screenName joinState.errors model

        Waiting ->
            waiting model

        Rejoining savedGame ->
            rejoining savedGame model

        HostWaiting gameCode errors ->
            hostWaiting gameCode errors model

        Starting ->
            message "Starting game" model

        Spectating selected errors ->
            spectating selected errors model

        Pitching word1 word2 loading ->
            pitching word1 word2 loading model

        BecomingBuyer ->
            message "Loading role" model

        Buying role ->
            buying role Nothing model

        RelinquishingBuyer ->
            message "Returning to game" model

        AwardingPoint role playerName ->
            buying role (Just playerName) model

        Error errs ->
            error errs model

        _ ->
            error [ "Unknown application state" ] model


lifecycleTheme : Lifecycle -> String
lifecycleTheme lifecycle =
    case lifecycle of
        Welcome ->
            "green"

        Create _ ->
            "teal"

        HostWaiting _ _ ->
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
