module Views.Utils exposing (qsButton, qsStaticButton, lis, icon, plural, friendlyError, resumeGameIfItExists)

import Html exposing (..)
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
               [ class "waves-effect waves-light btn-flat" ]
               [ div
                   [ onClick Msg.NavigateSpectate ]
                   [ icon "navigate_next" "right"
                   , text "back to game"
                   ]
               ]
        Nothing ->
            text ""

qsButton : String -> Msg -> Html Msg
qsButton buttonText msg =
    button
        [ class "waves-effect waves-light btn"
        , onClick msg
        ]
        [ text buttonText ]

qsStaticButton : String -> Html Msg
qsStaticButton buttonText =
    button
        [ class "waves-effect waves-light btn" ]
        [ text buttonText ]

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
