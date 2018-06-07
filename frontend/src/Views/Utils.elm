module Views.Utils exposing
    ( lis, icon, plural, resumeGameIfItExists
    , container, row, col, card, gameNav, stripMargin, multiLineText
    , textInput, shroud, empty, ShroudContent (..)
    , errorsForField, errorsExcludingField, nonFieldErrors, showErrors
    )

import Html exposing (Html, Attribute, div, text, button, input, label, li, i, span)
import Html.Attributes exposing (..)
import Html.Events exposing (onClick, onSubmit, onInput)
import Msg exposing (Msg)
import Model exposing (Model, ApiError)


empty : Html Msg
empty =
    text ""

resumeGameIfItExists : Model -> Html Msg
resumeGameIfItExists model =
    case model.state of
        Just state ->
            button
               [ class "waves-effect waves-light btn-flat"
               , onClick Msg.NavigateSpectate
               ]
               [ icon "gamepad" "left"
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

textInput : String -> String -> String -> List ApiError -> List ( Attribute Msg ) -> Html Msg
textInput elementLabel elementName value errors attrs =
    let
        showError = not ( List.isEmpty errors )
        elementId = elementName ++ "-id"
        fixedAttrs =
            [ id elementId
            , name elementName
            , type_ "text"
            , autocomplete False
            , classList [ ( "invalid", showError ) ]
            ]
        errMessages = String.concat ( List.intersperse ", " ( List.map .message errors ) )
    in
        div
            [ class "input-field" ]
            [ input
                ( fixedAttrs ++ attrs )
                []
            , label
                [ for elementId
                , classList [ ("active", not ( String.isEmpty value )) ]
                ]
                [ text elementLabel ]
            , span
                [ class "helper-text"
                , attribute "data-error" errMessages
                ]
                []
            ]

shroudMarkup : List ( Html Msg ) -> Bool -> Html Msg
shroudMarkup contents visible =
    div
        [ classList
            [ ( "shroud", True )
            , ( "hidden", not visible )
            ]
        ]
        [ div
            [ classList
                [ ( "message-box__container", True )
                , ( "hidden", not visible )
                ]
            ]
            [ div
                [ class "message-box" ]
                contents
            ]
        ]

shroud : ShroudContent -> Html Msg
shroud shroudContent =
    case shroudContent of
        LoadingMessage visible content ->
            if visible then
                shroudMarkup content visible
            else
                shroudMarkup [ empty ] visible
        ErrorMessage visible content ->
            if visible then
                shroudMarkup content visible
            else
                shroudMarkup [ empty ] visible
        NoLoadingShroud ->
            shroudMarkup [ empty ] False

type ShroudContent
    = LoadingMessage Bool ( List ( Html Msg ) )
    | ErrorMessage Bool ( List ( Html Msg ) )
    | NoLoadingShroud


errorsForField : String -> List ApiError -> List ApiError
errorsForField field errors =
    List.filter
        ( .context >> Maybe.withDefault "" >> ( (==) field ) )
        errors

errorsExcludingField : String -> List ApiError -> List ApiError
errorsExcludingField field errors =
    List.filter
        ( .context >> Maybe.withDefault "" >> ( (/=) field ) )
        errors

nonFieldErrors : List String -> List ApiError -> List ApiError
nonFieldErrors fields errors =
    List.filter
        ( .context >> Maybe.withDefault "" >> ( flip List.member fields ) >> not )
        errors

showErrors : List ApiError -> Html Msg
showErrors errors =
    if List.isEmpty errors then
        text ""
    else
        div
            [ class "card-panel red lighten-4" ]
            [ text ( String.concat ( List.intersperse ", " ( List.map .message errors ) ) ) ]
