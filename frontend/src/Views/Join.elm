module Views.Join exposing (join)

import Html exposing (Html, div, text, button,form)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, icon, textInput, ShroudContent (..))


join : String -> String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
join gameCode screenName model =
    (
        [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "navigate_before" "left"
            , text "back"
            ]
        ]
    , NoLoadingShroud
    , div
        []
        [ container "join"
            [ row
                [ col "s12"
                    [ card
                        [ form
                            [ onSubmit ( Msg.JoinGame gameCode screenName ) ]
                            [ textInput "Game code" "game-code-input" gameCode
                                [ onInput ( \val -> Msg.JoiningGame val screenName ) ]
                            , textInput "Player name" "player-name-input" screenName
                                [ onInput ( \val -> Msg.JoiningGame gameCode val ) ]
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
        ]
    )
