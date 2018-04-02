module Views.Join exposing (join)

import Html exposing (Html, div, text, button,form, input)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (row, col, card, icon)


join : String -> String -> Model -> Html Msg
join gameCode screenName model =
    div
        [ class "container" ]
        [ row
            [ col "s12"
                [ button
                    [ class "waves-effect waves-light btn green" ]
                    [ div
                        [ onClick Msg.NavigateHome ]
                        [ icon "navigate_before" "left"
                        , text "back"
                        ]
                    ]
                ]
            ]
        , row
            [ col "s12"
                [ card
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
