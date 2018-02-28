module View exposing (view)

import Html exposing (Html, div, form, input, button, text, h2, ul, li)
import Html.Attributes exposing (placeholder, disabled)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)


view : Model -> Html Msg
view model =
    case model.lifecycle of
        Welcome ->
            if model.backendAwake then
                div []
                    [ text "Ready"
                    , button [ onClick ( Msg.CreatingNewGame "" "" ) ]
                             [ text "Create game" ]
                    , button [ onClick ( Msg.JoiningGame "" "" ) ]
                             [ text "Join game" ]
                    ]
            else
                div []
                    [ text "Loading backend" ]

        Create gameName screenName ->
            form [ onSubmit ( Msg.CreateNewGame gameName screenName ) ]
                 [ input [ onInput ( \val -> Msg.CreatingNewGame val screenName ) , placeholder "Game name" ]
                         []
                 , input [ onInput ( \val -> Msg.CreatingNewGame gameName val ) , placeholder "Player name" ]
                         []
                 , button []
                          [ text "Create game" ]
                 ]

        Creating ->
            div []
                [ text "Creating game..." ]

        Join gameId screenName ->
            form [ onSubmit ( Msg.JoinGame gameId screenName ) ]
                 [ input [ onInput ( \val -> Msg.JoiningGame val screenName ) , placeholder "Game key" ]
                         []
                 , input [ onInput ( \val -> Msg.JoiningGame gameId val ) , placeholder "Player name" ]
                         []
                 , button []
                          [ text "Join game" ]
                 ]

        Joining ->
            div []
                [ text "Joining game..." ]

        Waiting ->
            div []
                [ text "Joined game!"
                , div []
                      [
                        if model.isCreator then
                            button [ onClick Msg.StartingGame ]
                                   [ text "start game" ]
                        else
                            text "waiting for game to start"
                      ]
                ]

        Starting ->
            div []
                [ text "Starting game..." ]

        Spectating selected ->
            let
                hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
                gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
            in
                div []
                    [ h2 []
                         [ text gameName ]
                    , div []
                          [ text "Players:"
                          , ul []
                               ( List.map ( \playerName -> li [] [ text playerName ] ) model.otherPlayers )
                          ]
                    , ul []
                         ( List.map ( \word -> button [ onClick ( Msg.DeselectWord word selected ) ]
                                                      [ text ( word ++ " x" ) ]
                                    ) selected )
                    , ul []
                         ( List.map ( \word -> button [ onClick ( Msg.SelectWord word selected )
                                                      , disabled ( List.member word selected )
                                                      ]
                                                      [ text word ]
                                    )
                           hand
                         )
                    , div []
                          [ button [ onClick ( Msg.RequestBuyer ) ] [ text "Buyer" ]]
                    ]

        BecomingBuyer ->
            div []
                [ h2 []
                     [ text "Loading role" ]
                ]

        Buying role ->
            div []
                [ h2 []
                     [ text role ]
                , ul []
                     ( List.map ( \playerName -> li [] [ text playerName ] ) model.otherPlayers )
                ]

        Error errs ->
            div []
                [ h2 []
                     [ text "Application error!" ]
                , ul []
                     ( List.map ( \msg -> li [] [ text msg ] ) errs )
                ]

        _ ->
            div []
                [ text "Unknown application state" ]
