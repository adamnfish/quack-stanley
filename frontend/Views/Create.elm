module Views.Create exposing (create)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, icon)


create : String -> String -> Model -> Html Msg
create gameName screenName model =
    div
        [ class "container" ]
        [ div
            [ class "row" ]
            [ button
                [ class "waves-effect waves-light btn-flat" ]
                [ div
                    [ onClick Msg.NavigateHome ]
                    [ icon "navigate_before" "left"
                    , text "back"
                    ]
                ]
            ]
        , div
            [ class "row" ]
            [ form
                [ onSubmit ( Msg.CreateNewGame gameName screenName ) ]
                [ input
                    [ onInput ( \val -> Msg.CreatingNewGame val screenName )
                    , placeholder "Game name"
                    ]
                    []
                , input
                    [ onInput ( \val -> Msg.CreatingNewGame gameName val )
                    , placeholder "Player name"
                    ]
                    []
                , qsStaticButton "Create game"
                ]
            ]
        ]
