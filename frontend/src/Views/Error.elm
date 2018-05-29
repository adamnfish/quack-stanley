module Views.Error exposing (error)

import Html exposing (Html, div, text, button, h1, ul)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, lis, icon, resumeGameIfItExists)


error : List String -> Model -> ( List ( Html Msg ), Html Msg )
error errs model =
    (
        [ gameNav
            [ button
                [ class "waves-effect waves-light btn green"
                , onClick Msg.NavigateHome
                ]
                [ icon "navigate_before" "left"
                , text "home"
                ]
            , resumeGameIfItExists model
            ]
        ]
    , div
        []
        [ container "error"
            [ row
                [ col "s12"
                    [ card
                        [ h1
                            []
                            [ text "Error!" ]
                        , ul
                            []
                            ( lis errs )
                        ]
                    ]
                ]
            ]
        ]
    )
