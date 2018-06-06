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
import Views.Utils exposing (ShroudContent (..))


view : Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
view model =
    case model.lifecycle of
        Welcome ->
            welcome model

        Create createState ->
            create createState.loading createState.gameName createState.screenName createState.errors model

        Join joinState ->
            join joinState.loading joinState.gameCode joinState.screenName joinState.errors model

        Waiting ->
            waiting model
        Rejoining savedGame ->
            rejoining savedGame model
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
