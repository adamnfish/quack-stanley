module Views.Buying exposing (buying)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, icon)


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
        [ button
            [ class "waves-effect waves-light btn purple btn-large"
            , onClick ( Msg.AwardPoint role playerSummary.screenName )
            ]
            [ text playerSummary.screenName
            , icon "done" "right"
            ]

        ]
