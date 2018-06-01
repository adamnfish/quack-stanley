module View exposing (view)

import Html exposing (Html, div, form, input, button, text, h2, p, ul, li)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)

import Views.Message exposing (message)
import Views.Error exposing (error)
import Views.Welcome exposing (welcome)
import Views.Create exposing (create)
import Views.CreatorWaiting exposing (creatorWaiting)
import Views.Join exposing (join)
import Views.Waiting exposing (waiting)
import Views.Rejoining exposing (rejoining)
import Views.Spectating exposing (spectating)
import Views.Pitching exposing (pitching)
import Views.Buying exposing (buying)
import Views.AwardingPoint exposing (awardingPoint)


view : Model -> ( List ( Html Msg ), Html Msg )
view model =
    case model.lifecycle of
        Welcome ->
            welcome model

        Create gameName screenName ->
            create False gameName screenName model
        Creating gameName screenName ->
            create True gameName screenName model

        Join gameCode screenName ->
            join gameCode screenName model
        Joining ->
            message "Joining game..." model

        Waiting ->
            waiting model
        Rejoining ->
            rejoining model
        CreatorWaiting gameCode ->
            creatorWaiting gameCode model
        Starting ->
            message "Starting game..." model

        Spectating selected ->
            spectating selected model

        Pitching word1 word2 pitchStatus ->
            pitching word1 word2 pitchStatus model

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
