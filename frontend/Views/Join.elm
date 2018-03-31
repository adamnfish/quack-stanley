module Views.Join exposing (join)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, icon)


join : String -> String -> Model -> Html Msg
join gameCode screenName model =
    div
        [ class "container" ]
        [ div
            [ class "row" ]
            [ button
                [ class "waves-effect waves-light btn green" ]
                [ div
                    [ onClick Msg.NavigateHome ]
                    [ icon "navigate_before" "left"
                    , text "back"
                    ]
                ]
            ]
        , div
            [ class "row" ]
            [ div
                [ class "col s12" ]
                [ div
                    [ class "card-panel" ]
                    [ form
                        [ onSubmit ( Msg.JoinGame gameCode screenName ) ]
                        [ input
                            [ onInput ( \val -> Msg.JoiningGame val screenName )
                            , placeholder "Game code"
                            ]
                            []
                        , input
                            [ onInput ( \val -> Msg.JoiningGame gameCode val )
                            , placeholder "Player name"
                            ]
                            []
                        , button
                            [ class "waves-effect waves-light cyan btn btn-large" ]
                            [ text "Join game"
                            , icon "person_add" "right"
                            ]
                        ]
                    ]
                ]
            ]
        ]
