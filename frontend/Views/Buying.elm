module Views.Buying exposing (buying)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton)


buying : String -> Model -> Html Msg
buying role model =
    div [ class "container" ]
        [ h2
            []
            [ text role ]
        , ul
            []
            ( List.map ( otherPlayer role ) model.otherPlayers )
        ]

otherPlayer : String ->  String -> Html Msg
otherPlayer role playerName =
    li
        []
        [ qsButton playerName ( Msg.AwardPoint role playerName ) ]
