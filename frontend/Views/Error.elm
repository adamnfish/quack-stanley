module Views.Error exposing (error)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis, icon, resumeGameIfItExists)


error : List String -> Model -> Html Msg
error errs model =
    div
        [ class "container" ]
        [ div
            [ class "row" ]
            [ button
                [ class "waves-effect waves-light btn-flat" ]
                [ div
                    [ onClick Msg.NavigateHome ]
                    [ icon "navigate_before" "left"
                    , text "home"
                    ]
                ]
            , resumeGameIfItExists model
            ]
        , div
            [ class "row" ]
            [ h1
                []
                [ text "Error!" ]
            , ul
                []
                ( lis errs )
            ]
        ]
