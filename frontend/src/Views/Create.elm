module Views.Create exposing (create)

import Html exposing (Html, div, text, button, form)
import Html.Attributes exposing (id, class, classList, placeholder, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, icon, textInput)


create : String -> String -> Model -> Html Msg
create gameName screenName model =
    div
        []
        [ gameNav
            [ button
                [ class "waves-effect waves-light btn green"
                , onClick Msg.NavigateHome
                ]
                [ icon "navigate_before" "left"
                , text "back"
                ]
            ]
        , container "create"
            [ row
                [ col "col s12"
                    [ card
                        [ form
                            [ onSubmit ( Msg.CreateNewGame gameName screenName ) ]
                            [ textInput "Game name" "create-game-input" gameName
                                [ onInput ( \val -> Msg.CreatingNewGame val screenName ) ]
                            , textInput "Player name" "player-name-input" screenName
                                [ onInput ( \val -> Msg.CreatingNewGame gameName val ) ]
                            , button
                                  [ class "waves-effect waves-light teal btn btn-large" ]
                                  [ text "Create game"
                                  , icon "gamepad" "right"
                                  ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
