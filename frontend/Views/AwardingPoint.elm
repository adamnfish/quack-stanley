module Views.AwardingPoint exposing (awardingPoint)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis)


awardingPoint : String -> String -> Model -> Html Msg
awardingPoint role playerName model =
    div [ class "container" ]
        [ h2
            []
            [ text role ]
        , p
            []
            [ text "Awarding point to "
            , text playerName
            ]
        , ul
            []
            ( lis model.otherPlayers )
        ]

otherPlayer : String -> Html Msg
otherPlayer playerName =
    li
        []
        [ text playerName ]
