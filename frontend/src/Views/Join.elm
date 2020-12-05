module Views.Join exposing (join)

import Html exposing (Html, button, div, form, text)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onInput, onSubmit)
import Model exposing (ApiError, Lifecycle(..), Model)
import Msg exposing (Msg)
import Utils exposing (nonEmpty)
import Views.Utils exposing (ShroudContent(..), card, col, container, empty, errorsExcludingField, errorsForField, gameNav, helpText, icon, nonFieldErrors, row, showErrors, textInput)


join : Bool -> String -> String -> String -> List ApiError -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
join loading gameCode hostCode screenName errors model =
    let
        submitAction =
            if String.isEmpty hostCode then
                Msg.JoinGame gameCode screenName

            else
                Msg.JoinGameAsHost gameCode hostCode screenName
    in
    ( [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "home" "left"
            , text "back"
            ]
      ]
    , LoadingMessage loading [ text "Joining game" ]
    , container "join"
        [ row
            [ col "s12"
                [ card
                    [ showErrors (nonFieldErrors [ "game code", "screen name" ] errors)
                    , form
                        [ onSubmit submitAction ]
                        [ textInput "Game code"
                            "game-code"
                            gameCode
                            (errorsForField "game code" errors)
                            [ onInput (\val -> Msg.JoiningGame val hostCode screenName (errorsExcludingField "game code" errors)) ]
                        , if hostCode /= "" then
                            textInput "Host code"
                                "host-code"
                                hostCode
                                (errorsForField "host code" errors)
                                [ onInput (\val -> Msg.JoiningGame val hostCode screenName (errorsExcludingField "host code" errors)) ]

                          else
                            empty
                        , textInput "Player name"
                            "player-name"
                            screenName
                            (errorsForField "screen name" errors)
                            [ onInput (\val -> Msg.JoiningGame gameCode hostCode val (errorsExcludingField "screen name" errors)) ]
                        , button
                            [ class "waves-effect waves-light cyan btn btn-large" ]
                            [ text "Join game"
                            , icon "person_add" "right"
                            ]
                        ]
                    ]
                ]
            ]
        , row
            [ col "s12"
                [ card
                    [ if nonEmpty hostCode then
                        helpText
                            """|Host an existing game, setting your name.
                               |You will be able to start the game once all players have joined."""

                      else
                        helpText
                            """|Join an existing game and set your player name.
                               |The game's host can tell you the game code.
                               |"""
                    ]
                ]
            ]
        ]
    )
