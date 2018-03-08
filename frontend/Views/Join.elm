module Views.Join exposing (join)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton)


join : String -> String -> Model -> Html Msg
join gameCode screenName model =
    div
        [ class "container" ]
        [ form
            [ onSubmit ( Msg.JoinGame gameCode screenName ) ]
            [ input
                [ onInput ( \val -> Msg.JoiningGame val screenName )
                , placeholder "Game code"
                ]
                []
            , input
                [ onInput ( \val -> Msg.JoiningGame gameCode val )
                , placeholder "Player name"
                ]
                []
            , qsStaticButton "Join game"
            ]
        ]