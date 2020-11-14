module Views.Message exposing (message)

import Html exposing (Html, button, div, p, text)
import Model exposing (Lifecycle(..), Model)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, empty, gameNav, icon, row)


message : String -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
message contents model =
    ( []
    , LoadingMessage True [ text contents ]
    , empty
    )
