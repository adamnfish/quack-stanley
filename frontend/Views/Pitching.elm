module Views.Pitching exposing (pitching)

import Html exposing (..)
import Html.Attributes exposing (class, classList, placeholder, disabled)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..), PitchStatus (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis, icon)


pitching : String -> String -> PitchStatus -> Model -> Html Msg
pitching word1 word2 pitchStatus model =
    let
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        screenName = Maybe.withDefault "" ( Maybe.map .screenName model.state )
    in
    div
        [ class "container" ]
        [ div
            [ class "row" ]
            [ button
                [ class "waves-effect waves-light blue btn-flat" ]
                [ div
                    [ onClick Msg.NavigateSpectate ]
                    [ icon "navigate_before" "left"
                    , text "cancel pitch"
                    ]
                ]

            ]
        , div
            [ class "row" ]
            [ div
                [ class "col m6 s12" ]
                [ wordDisplay word1 ( pitchStatus /= NoCards ) ]
            , div
                [ class "col m6 s12" ]
                [ wordDisplay word2 ( pitchStatus == TwoCards ) ]
            ]
        , div
            [ class "row" ]
            [ div
                [ class "col m6 s12 push-m6" ]
                [ pitchCta word1 word2 pitchStatus ]
            ]
        ]

wordDisplay : String -> Bool -> Html Msg
wordDisplay word show =
    div
        [ classList
            [ ( "card white", True )
            , ( "pitch__card--hidden", ( not show ) )
            ]
        ]
        [ div
            [ class "card-content center-align" ]
            [ span
                [ class "pitch__card center-align" ]
                [ text ( if show then word else "\x00A0" ) ]
            ]
        ]

pitchCta : String -> String -> PitchStatus -> Html Msg
pitchCta word1 word2 pitchStatus =
    if ( pitchStatus == TwoCards )
    then
        button
            [ class "waves-effect waves-light btn btn-large blue cta__button"
            , onClick ( Msg.FinishedPitch word1 word2 )
            , disabled ( pitchStatus /= TwoCards )
            ]
            [ text "Finish pitch"
            , icon "done" "right"
            ]
    else
        button
            [ class "waves-effect waves-light btn btn-large indigo cta__button"
            , onClick ( Msg.RevealCard word1 word2 pitchStatus )
            , disabled ( pitchStatus == TwoCards )
            ]
            [ text "Show next"
            , icon "play_arrow" "right"
            ]
