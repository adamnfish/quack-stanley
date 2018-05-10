module Views.Utils exposing
    ( lis, icon, plural, friendlyError, resumeGameIfItExists
    , container, row, col, card, gameNav, stripMargin, multiLineText
    )

import Html exposing (Html, div, text, button, li, i)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Msg exposing (Msg)
import Model exposing (Model)


friendlyError : Model -> Html Msg
friendlyError model =
    if List.length model.errs > 0
    then
        div
            [ class "card-panel red lighten-4" ]
            ( List.map text model.errs )
    else
        text ""

resumeGameIfItExists : Model -> Html Msg
resumeGameIfItExists model =
    case model.state of
        Just state ->
            button
               [ class "waves-effect waves-light btn-flat"
               , onClick Msg.NavigateSpectate
               ]
               [ icon "navigate_next" "right"
               , text "back to game"
               ]
        Nothing ->
            text ""

lis : List String -> List ( Html Msg )
lis labels =
    let
        anLi label =
            li
                []
                [ text label ]
    in
        List.map anLi labels

icon : String -> String -> Html Msg
icon code align =
    i
        [ class ( "material-icons " ++ align ) ]
        [ text code ]

plural : String -> Int -> String
plural str count =
    if count == 1 then str else ( str ++ "s" )

container : String -> List ( Html Msg ) -> Html Msg
container name children =
    div [ class ( "container " ++ name ) ] children

row : List ( Html Msg ) -> Html Msg
row children =
    div [ class "row" ] children

col : String -> List ( Html Msg ) -> Html Msg
col classes children =
    div [ class ( "col " ++ classes ) ] children

card : List ( Html Msg ) -> Html Msg
card children =
    div [ class "card-panel" ] children

gameNav : List ( Html Msg ) -> Html Msg
gameNav buttons =
    div
        [ class "game-nav" ]
        [ container ""
            [ row
                [ col "s12"
                    buttons
                ]
            ]
        ]

stripMarginLine : String -> String
stripMarginLine line =
    let
        trimmed = String.trimLeft line
    in
        if String.startsWith "|" trimmed then
            String.dropLeft 1 trimmed
        else
            line

stripMargin : String -> String
stripMargin str =
    let
        lines = String.split "\n" str
    in
        String.join "\n" ( List.map stripMarginLine lines )

multiLineText : String -> Html Msg
multiLineText str = text ( stripMargin str )
