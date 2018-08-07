module Views.Create exposing (create)

import Html exposing (Html, div, ul, li, text, button, form)
import Html.Attributes exposing (id, class, classList, placeholder, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, ApiError, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, icon, textInput, helpText, shroud, ShroudContent (..), errorsForField, errorsExcludingField, nonFieldErrors, showErrors)


create : Bool -> String -> String -> List ApiError -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
create loading gameName screenName errors model =
    (
        [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "home" "left"
            , text "back"
            ]
        ]
    , LoadingMessage loading ( [ text "Creating game" ] )
    , div
        []
        [ container "create"
            [ row
                [ col "s12"
                    [ card
                        [ showErrors ( nonFieldErrors [ "game name", "screen name" ] errors )
                        , form
                            [ onSubmit ( Msg.CreateNewGame gameName screenName ) ]
                            [ textInput "Game name" "create-game" gameName ( errorsForField "game name" errors )
                                [ onInput ( \val -> Msg.CreatingNewGame val screenName ( errorsExcludingField "game name" errors ) ) ]
                            , textInput "Player name" "player-name" screenName ( errorsForField "screen name" errors )
                                [ onInput ( \val -> Msg.CreatingNewGame gameName val ( errorsExcludingField "screen name" errors ) ) ]
                            , button
                                [ class "waves-effect waves-light teal btn btn-large" ]
                                [ text "Create game"
                                , icon "gamepad" "right"
                                ]
                            ]
                        ]
                    ]
                ]
            , row
                [ col "s12"
                    [ card
                        [ helpText
                            """|Creates a new game that others can join.
                               |You will be able to start the game when player's have joined.
                               |"""
                       ]
                    ]
                ]
            ]
        ]
    )
