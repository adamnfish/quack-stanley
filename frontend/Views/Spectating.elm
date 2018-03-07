module Views.Spectating exposing (spectating)

import Html exposing (..)
import Html.Attributes exposing (class, placeholder, disabled)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (qsButton, qsStaticButton)


spectating : List String -> Model -> Html Msg
spectating selected model =
    let
        hand = Maybe.withDefault [] ( Maybe.map .hand model.state )
        gameName = Maybe.withDefault "" ( Maybe.map .gameName model.state )
    in
    div
        [ class "container" ]
        [ h2
            []
            [ text gameName ]
        , div
            []
            [ text "Players:"
            , ul
                []
                ( List.map ( \playerName -> li [] [ text playerName ] ) model.otherPlayers )
            ]
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
                        , onClick ( Msg.FinishedPitch word1 word2 )
                        , disabled False
                        ]
                    _ ->
                        [ class "waves-effect waves-light btn"
                        , disabled True
                        ]
              )
            [ text "Finish pitch" ]
            ]
        , div
            []
            [ qsButton "Buyer" Msg.RequestBuyer ]
        ]
