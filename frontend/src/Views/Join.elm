module Views.Join exposing (join)

import Html exposing (Html, button, div, form, text)
import Html.Attributes exposing (class, placeholder)
import Html.Events exposing (onClick, onInput, onSubmit)
import Model exposing (ApiError, Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, errorsExcludingField, errorsForField, gameNav, helpText, icon, nonFieldErrors, row, showErrors, textInput)


join : Bool -> String -> String -> List ApiError -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
join loading gameCode screenName errors model =
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
                        [ onSubmit (Msg.JoinGame gameCode screenName) ]
                        [ textInput "Game code"
                            "game-code"
                            gameCode
                            (errorsForField "game code" errors)
                            [ onInput (\val -> Msg.JoiningGame val screenName (errorsExcludingField "game code" errors)) ]
                        , textInput "Player name"
                            "player-name"
                            screenName
                            (errorsForField "screen name" errors)
                            [ onInput (\val -> Msg.JoiningGame gameCode val (errorsExcludingField "screen name" errors)) ]
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
                    [ helpText
                        """|Join an existing game and set your player name.
                           |The game's host can tell you the game code.
                           |"""
                    ]
                ]
            ]
        ]
    )
