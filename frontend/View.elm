module View exposing (view)

import Html exposing (Html, div, form, input, button, text)
import Html.Attributes exposing (placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Game, Player, Lifecycle (..))
import Msg exposing (Msg)


view : Model -> Html Msg
view model =
    case model.lifecycle of
        Welcome ->
            if model.backendAwake then
                div []
                    [ text "Ready"
                    , button [ onClick (Msg.CreatingNewGame "" "") ]
                             [ text "Create game" ]
                    , button [ onClick Msg.JoinGame ]
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
            form [ onSubmit Msg.JoinGame ]
                 [ input [ placeholder "Game key" ]
                         []
                 , button []
                          [ text "Join game" ]
                 ]
        Joining ->
            div []
                [ text "Joining game..." ]
        Waiting ->
            div []
                [ text "Joined game!" ]
        _ ->
            div []
                [ text "Unknown application state" ]
