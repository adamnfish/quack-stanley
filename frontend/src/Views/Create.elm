module Views.Create exposing (create)

import Html exposing (Html, button, div, form, li, text, ul)
import Html.Attributes exposing (class, classList, for, id, placeholder)
import Html.Events exposing (onClick, onInput, onSubmit)
import Model exposing (ApiError, Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, errorsExcludingField, errorsForField, gameNav, helpText, icon, nonFieldErrors, row, showErrors, shroud, textInput)


create : Bool -> String -> String -> List ApiError -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
create loading gameName screenName errors model =
    ( [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "home" "left"
            , text "back"
            ]
      ]
    , LoadingMessage loading [ text "Creating game" ]
    , container "create"
        [ row
            [ col "s12"
                [ card
                    [ showErrors (nonFieldErrors [ "game name", "screen name" ] errors)
                    , form
                        [ onSubmit (Msg.CreateNewGame gameName screenName) ]
                        [ textInput "Game name"
                            "create-game"
                            gameName
                            (errorsForField "game name" errors)
                            [ onInput (\val -> Msg.CreatingNewGame val screenName (errorsExcludingField "game name" errors)) ]
                        , textInput "Player name"
                            "player-name"
                            screenName
                            (errorsForField "screen name" errors)
                            [ onInput (\val -> Msg.CreatingNewGame gameName val (errorsExcludingField "screen name" errors)) ]
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
                           |You will be able to start the game when players have joined.
                           |"""
                    ]
                ]
            ]
        ]
    )
