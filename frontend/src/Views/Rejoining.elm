module Views.Rejoining exposing (rejoining)

import Html exposing (Html, div, p, span, strong, text)
import Model exposing (Lifecycle(..), Model, SavedGame)
import Msg exposing (Msg)
import Views.Utils exposing (ShroudContent(..), card, col, container, gameNav, icon, row)


rejoining : SavedGame -> Model -> ( List (Html Msg), ShroudContent, Html Msg )
rejoining savedGame model =
    ( []
    , LoadingMessage True
        [ p
            []
            [ text "Re-joining "
            , text savedGame.gameName
            ]
        ]
    , container "rejoining" []
    )
