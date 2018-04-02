module Views.Error exposing (error)

import Html exposing (Html, div, text, button, h1, ul)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, lis, icon, resumeGameIfItExists)


error : List String -> Model -> Html Msg
error errs model =
    container
        [ row
            [ col "s12"
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
            ]
        , row
            [ col "s12"
                [ h1
                    []
                    [ text "Error!" ]
                , ul
                    []
                    ( lis errs )
                ]
            ]
        ]
