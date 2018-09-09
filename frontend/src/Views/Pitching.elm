module Views.Pitching exposing (pitching)

import Html exposing (Html, div, text, button, span)
import Html.Attributes exposing (class, classList, placeholder, disabled)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, lis, icon, ShroudContent (..))


pitching : String -> String  -> Bool -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
pitching word1 word2 loading model =
    let
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        screenName = Maybe.withDefault "" ( Maybe.map .screenName model.state )
    in
        (
            [ button
                [ class "waves-effect waves-light blue btn-flat"
                , onClick Msg.NavigateSpectate
                ]
                [ icon "close" "left"
                , text "cancel"
                ]
            ]
        , LoadingMessage loading [ text "Finishing pitch" ]
        , div
            []
            [ div
                [ class "container pitching" ]
                [ row
                    [ col "s12"
                        [ card
                            [ div
                                [ class "pitch__container" ]
                                [ span
                                    [ class "pitch--word" ]
                                    [ text word1 ]
                                , text " "
                                , span
                                    [ class "pitch__divider grey-text text-lighten-1" ]
                                    [ text " " ]
                                , text " "
                                , span
                                    [ class "pitch--word" ]
                                    [ text word2 ]
                                ]
                            ]
                        ]
                    ]
                , row
                    [ col "m6 s12 push-m6"
                        [ card
                            [ button
                                [ class "waves-effect waves-light btn btn-large blue cta__button"
                                , onClick ( Msg.FinishedPitch word1 word2 )
                                ]
                                [ text "Finish pitch"
                                , icon "done" "right"
                                ]
                            ]
                        ]
                    ]
                ]
            ]
        )
