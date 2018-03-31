module Views.Spectating exposing (spectating)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder, disabled, attribute)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton, lis, plural)


spectating : List String -> Model -> Html Msg
spectating selected model =
    let
        hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
        screenName = Maybe.withDefault "" ( Maybe.map .screenName model.state )
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
        points = Maybe.withDefault [] ( Maybe.map .points model.state )
    in
    div
        [ class "container" ]
        [ h2
            []
            [ text gameName ]
        , h3
            []
            [ text screenName ]
        , ul
            []
            ( List.map ( \word -> qsButton ( word ++ " x" ) ( Msg.DeselectWord word selected ) ) selected )
        , ul
            []
            ( List.map ( \word ->
                button
                    [ class "waves-effect waves-light btn"
                    , onClick ( Msg.SelectWord word selected )
                    , disabled ( List.member word selected )
                    ]
                    [ text word ]
              )
              hand
            )
        , div
            []
            [ button (
                case selected of
                    word1 :: word2 :: [] ->
                        [ class "waves-effect waves-light btn"
                        , onClick ( Msg.StartPitch word1 word2 )
                        , disabled False
                        ]
                    _ ->
                        [ class "waves-effect waves-light btn"
                        , disabled True
                        ]
              )
            [ text "Start pitch" ]
            ]
        , div
            []
            [ qsButton "Buyer" Msg.RequestBuyer ]
        , ul
            [ class "collection with-header" ]
            (
                [ li
                    [ class "collection-header" ]
                    [ h4
                        []
                        [ text "Players" ]
                    ]
                , li
                    [ class "collection-item" ]
                    [ text "You"
                    , span
                        [ class "badge"
                        , attribute "data-badge-caption" ( plural "point" ( List.length points ) )
                        ]
                        [ text ( toString ( List.length points ) ) ]
                    ]
                ] ++ List.map collectionLi model.opponents
            )
        ]

collectionLi : PlayerSummary -> Html Msg
collectionLi playerSummary =
    li
        [ class "collection-item" ]
        [ span
            [ class "badge"
            , attribute "data-badge-caption" ( plural "point" ( List.length playerSummary.points ) )
            ]
            [ text ( toString ( List.length playerSummary.points ) ) ]
        , text playerSummary.screenName
        ]
