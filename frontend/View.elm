module View exposing (view)

import Html exposing (Html, div, form, input, button, text, h2, ul, li)
import Html.Attributes exposing (placeholder)
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
                 [ input [ onInput (\val -> Msg.CreatingNewGame val screenName ) , placeholder "Game name" ]
                         []
                 , input [ onInput (\val -> Msg.CreatingNewGame gameName val ) , placeholder "Player name" ]
                         []
                 , button []
                          [ text "Create game" ]
                 ]
        Creating ->
            div []
                [ text "Creating game..." ]
        Join gameId screenName ->
            form [ onSubmit ( Msg.JoinGame gameId screenName ) ]
                 [ input [ onInput (\val -> Msg.JoiningGame val screenName ) , placeholder "Game key" ]
                         []
                 , input [ onInput (\val -> Msg.JoiningGame gameId val ) , placeholder "Player name" ]
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
        Spectating ->
            let
                hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
            in
                div []
                    [ text "Game has started"
                    , ul []
                         ( List.map ( \word -> li [] [ text word ] ) hand )
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
