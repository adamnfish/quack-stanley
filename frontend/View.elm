module View exposing (view)

import Html exposing (Html, div, text)
import Model exposing (Model (..))
import Msg exposing (Msg)


view : Model -> Html Msg
view model =
    case model of
        Welcome True ->
            div []
                [ text "Ready" ]
        Welcome False ->
            div []
                [ text "Loading backend" ]
