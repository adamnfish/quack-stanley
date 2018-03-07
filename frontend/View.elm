module View exposing (view)

import Html exposing (Html, div, form, input, button, text, h2, p, ul, li)
import Html.Attributes exposing (class)
import Html.Events exposing (onClick, onSubmit, onInput)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)

import Views.Message exposing (message)
import Views.Error exposing (error)
import Views.Welcome exposing (welcome)
import Views.Create exposing (create)
import Views.CreatorWaiting exposing (creatorWaiting)
import Views.Join exposing (join)
import Views.Spectating exposing (spectating)
import Views.Buying exposing (buying)
import Views.AwardingPoint exposing (awardingPoint)


view : Model -> Html Msg
view model =
    case model.lifecycle of
        Welcome ->
            welcome model

        Create gameName screenName ->
            create gameName screenName model
        Creating ->
            message "Creating game..." model

        Join gameCode screenName ->
            join gameCode screenName model
        Joining ->
            message "Joining game..." model

        Waiting ->
            message "waiting for game to start" model
        CreatorWaiting gameCode ->
            creatorWaiting gameCode model
        Starting ->
            message "Starting game..." model

        Spectating selected ->
            spectating selected model

        BecomingBuyer ->
            message "Loading role" model
        Buying role ->
            buying role model
        AwardingPoint role playerName ->
            awardingPoint role playerName model

        Error errs ->
            error errs model
        _ ->
            error [ "Unknown application state" ] model
