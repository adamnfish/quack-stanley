module Views.Message exposing (message)

import Html exposing (Html, div, text, p, button)
import Model exposing (Model, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, empty, ShroudContent (..))


message : String -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
message contents model =
    ( []
    , LoadingMessage True [ text contents ]
    , empty
    )
