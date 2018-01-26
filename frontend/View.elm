module View exposing (view)

import Html exposing (Html, div, text)
import Model exposing (Model, Game, Player, Lifecycle (..))
import Msg exposing (Msg)


view : Model -> Html Msg
view model =
    case model.lifecycle of
        Welcome ->
            if model.backendReady then
                div []
                    [ text "Ready" ]
            else
                div []
                    [ text "Loading backend" ]
        _ ->
            div []
                [ text "Unknown application state" ]
