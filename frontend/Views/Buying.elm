module Views.Buying exposing (buying)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
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
            ( List.map ( otherPlayer role ) model.opponents )
        ]

otherPlayer : String ->  PlayerSummary -> Html Msg
otherPlayer role playerSummary =
    li
        []
        [ qsButton playerSummary.screenName ( Msg.AwardPoint role playerSummary.screenName ) ]
