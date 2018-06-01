module Views.Create exposing (create)

import Html exposing (Html, div, ul, li, text, button, form)
import Html.Attributes exposing (id, class, classList, placeholder, for)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, row, col, card, gameNav, icon, textInput, shroud)


create : Bool -> String -> String -> Model -> ( List ( Html Msg ), Html Msg )
create loading gameName screenName model =
    (
        [ button
            [ class "waves-effect waves-light btn green"
            , onClick Msg.NavigateHome
            ]
            [ icon "navigate_before" "left"
            , text "back"
            ]
        ]
    , div
        []
        [ shroudIfLoading loading
        , container "create"
            [ row
                [ col "col s12"
                    [ card
                        [ showErrors model
                        , form
                            [ onSubmit ( Msg.CreateNewGame gameName screenName ) ]
                            [ textInput "Game name" "create-game-input" gameName
                                [ onInput ( \val -> Msg.CreatingNewGame val screenName ) ]
                            , textInput "Player name" "player-name-input" screenName
                                [ onInput ( \val -> Msg.CreatingNewGame gameName val ) ]
                            , button
                                  [ class "waves-effect waves-light teal btn btn-large" ]
                                  [ text "Create game"
                                  , icon "gamepad" "right"
                                  ]
                            ]
                        ]
                    ]
                ]
            ]
        ]
    )

shroudIfLoading : Bool -> Html Msg
shroudIfLoading loading =
    if loading then
        shroud
            [ text "Creating game" ]
    else
        text ""

showErrors : Model -> Html Msg
showErrors model =
    if List.isEmpty model.errs then
        text ""
    else
        ul
            []
            ( List.map (\msg -> li [][ text msg ]) model.errs )
