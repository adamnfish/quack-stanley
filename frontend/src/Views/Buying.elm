module Views.Buying exposing (buying)

import Html exposing (Html, div, text, button, ul, li, h2, a)
import Html.Attributes exposing (class, placeholder, href)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, PlayerSummary, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, empty, ShroudContent (..))


buying : String -> Maybe String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
buying role awardingTo model =
    let
        shroud =
            case awardingTo of
                Nothing ->
                    LoadingMessage False [ empty ]
                Just playerName ->
                    LoadingMessage True
                        [ text "Awarding "
                        , text role
                        , text " to "
                        , text playerName
                        ]
    in
        (
            [ button
                [ class "waves-effect waves-light btn blue"
                , onClick Msg.RelinquishBuyer
                ]
                [ icon "close" "left"
                , text "Cancel"
                ]
            ]
        , shroud
        , div
            []
            [ container "buying"
                [ row
                    [ col "s12"
                        [ card
                            [ h2
                                []
                                [ text role ]
                            , ul
                                []
                                ( List.map ( otherPlayer role ) model.opponents )
                            ]
                        ]
                    ]
                ]
            ]
        )

otherPlayer : String ->  PlayerSummary -> Html Msg
otherPlayer role playerSummary =
    li
        []
        [ button
            [ class "pitch-winner__button waves-effect waves-light btn purple btn-large"
            , onClick ( Msg.AwardPoint role playerSummary.screenName )
            ]
            [ text playerSummary.screenName
            , icon "done" "right"
            ]

        ]
