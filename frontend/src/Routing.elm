module Routing exposing (gameUrl, homeUrl, parseCurrentGame, parseJoinState)

import Maybe.Extra
import Model exposing (JoinState)
import Url
import Url.Parser
import Url.Parser.Query as Query
import Utils exposing (tuple2, uncurry)


joinStateParser : Query.Parser (Maybe JoinState)
joinStateParser =
    Query.map3
        (\maybeGameCode hostCode screenName ->
            case maybeGameCode of
                Just gameCode ->
                    Just
                        { gameCode = gameCode
                        , hostCode = hostCode
                        , screenName = screenName
                        , loading = False
                        , errors = []
                        }

                _ ->
                    Nothing
        )
        (Query.string "gameCode")
        (Query.map (Maybe.withDefault "") (Query.string "hostCode"))
        (Query.map (Maybe.withDefault "") (Query.string "name"))


parseJoinState : Url.Url -> Maybe JoinState
parseJoinState url =
    Maybe.Extra.join <|
        Url.Parser.parse
            (Url.Parser.query joinStateParser)
            url


parseCurrentGame : Url.Url -> Maybe ( String, String )
parseCurrentGame url =
    let
        qsParser =
            Query.map2 tuple2
                (Query.string "game")
                (Query.string "player")
    in
    Maybe.andThen
        (uncurry (Maybe.map2 tuple2))
        (Url.Parser.parse
            (Url.Parser.query qsParser)
            url
        )


gameUrl : Url.Url -> String -> String -> String
gameUrl url gameCode screenName =
    Url.toString { url | query = Just <| "game=" ++ gameCode ++ "&player=" ++ screenName }


homeUrl : Url.Url -> String
homeUrl url =
    Url.toString { url | query = Nothing }
