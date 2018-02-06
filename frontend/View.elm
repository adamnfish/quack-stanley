module View exposing (view)

import Html exposing (Html, div, form, input, button, text)
import Html.Attributes exposing (placeholder)
import Html.Events exposing (onClick, onSubmit)
import Model exposing (Model, Game, Player, Lifecycle (..))
import Msg exposing (Msg)


view : Model -> Html Msg
view model =
    case model.lifecycle of
        Welcome ->
            if model.backendAwake then
                div []
                    [ text "Ready"
                    , button [ onClick Msg.SelectCreateNewGame ]
                             [ text "Create game" ]
                    , button [ onClick Msg.SelectJoinGame ]
                             [ text "Join game" ]
                    ]
            else
                div []
                    [ text "Loading backend" ]
        Create ->
            form [ onSubmit Msg.CreateNewGame ]
                 [ input [ placeholder "Name" ]
                         []
                 , button []
                          [ text "Create game" ]
                 ]
        Creating ->
            div []
                [ text "Creating game..." ]
        Join ->
            form [ onSubmit Msg.JoinGame ]
                 [ input [ placeholder "Game key" ]
                         []
                 , button []
                          [ text "Join game" ]
                 ]
        Joining ->
            div []
                [ text "Joining game..." ]
        _ ->
            div []
                [ text "Unknown application state" ]
