module View exposing (view)

import Html exposing (Html, div, text, button)
import Html.Events exposing (onClick)
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
        _ ->
            div []
                [ text "Unknown application state" ]
