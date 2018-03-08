module Views.Error exposing (error)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis)


error : List String -> Model -> Html Msg
error errs model =
    div
        [ class "container" ]
        [ h1
            []
            [ text "Error!" ]
        , ul
            []
            ( lis errs )
        ]
