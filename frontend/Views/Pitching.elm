module Views.Pitching exposing (pitching)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder, disabled)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..), PitchStatus (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis)


pitching : String -> String -> PitchStatus -> Model -> Html Msg
pitching word1 word2 pitchStatus model =
    let
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        wordMarkup : String -> Bool -> Html Msg
        wordMarkup word include =
            div
                []
                [ text ( if include then word else "" ) ]
    in
    div
        [ class "container" ]
        [ h2
            []
            [ text gameName ]
        , div
            [ class "pitch--container" ]
            [ wordMarkup word1 ( pitchStatus /= NoCards )
            , wordMarkup word2 ( pitchStatus == TwoCards )
            ]
        , div
            []
            [ button
                  [ class "waves-effect waves-light btn"
                  , onClick ( Msg.RevealCard word1 word2 pitchStatus )
                  , disabled ( pitchStatus == TwoCards )
                  ]
                  [ text "Show next" ]

            ]
        , div
            []
            [ button
                [ class "waves-effect waves-light btn"
                , onClick ( Msg.FinishedPitch word1 word2 )
                , disabled False
                ]
                [ text "Finish pitch" ]
            ]
        ]
