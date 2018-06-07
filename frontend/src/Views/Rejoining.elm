module Views.Rejoining exposing (rejoining)

import Html exposing (Html, div, p, strong, span, text)
import Model exposing (Model, SavedGame, Lifecycle (..))
import Msg exposing (Msg)
import Views.Utils exposing (container, gameNav, row, col, card, icon, ShroudContent (..))


rejoining : SavedGame -> Model -> ( List ( Html Msg ), ShroudContent, Html Msg )
rejoining savedGame model =
    let
        gameName = Maybe.withDefault "Game name not found" ( Maybe.map .gameName model.state )
    in
        ( []
        , LoadingMessage True
            [ p
                []
                [ text "Re-joining "
                , text savedGame.gameName
                ]
            ]
        , div
            []
            [ container "rejoining"
                []
            ]
        )
