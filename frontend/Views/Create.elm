module Views.Create exposing (create)

import Html exposing (Html, div, text, button, form, input)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, icon)


create : String -> String -> Model -> Html Msg
create gameName screenName model =
    container
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
            [ col "col s12"
                [ card
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
                        , button
                              [ class "waves-effect waves-light teal btn btn-large" ]
                              [ text "Create game"
                              , icon "person" "right"
                              ]
                        ]
                    ]
                ]
            ]
        ]
