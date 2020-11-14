module Views.Error exposing (error)

import Html exposing (Html, button, div, h1, text, ul)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onInput, onSubmit)
import Model exposing (Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, gameNav, icon, lis, resumeGameIfItExists, row)


error : List String -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
error errs model =
    ( [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "home" "left"
            , text "home"
            ]
      , resumeGameIfItExists model
      ]
    , NoLoadingShroud
    , container "error"
        [ row
            [ col "s12"
                [ card
                    [ h1
                        []
                        [ text "Error" ]
                    , ul
                        []
                        (lis errs)
                    ]
                ]
            ]
        ]
    )
