module Views.CreatorWaiting exposing (creatorWaiting)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton)


creatorWaiting : String -> Model -> Html Msg
creatorWaiting gameCode model =
    div
        [ class "container" ]
        [ text "Joined game!"
        , div
            []
            [ text gameCode ]
        , div
            []
            [ qsButton "start game" Msg.StartingGame ]
        ]
