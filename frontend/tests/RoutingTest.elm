module RoutingTest exposing (..)

import Expect exposing (Expectation)
import Model exposing (..)
import Routing exposing (parseJoinState)
import Test exposing (..)
import Url exposing (Protocol(..), Url)
import Url.Builder
import Utils exposing (uncurry)


all : Test
all =
    describe "update logic"
        [ test "returns Nothing for an empty (home) URL" <|
            \_ ->
                parseJoinState
                    (appUrl [])
                    |> Expect.equal Nothing
        , test "parses a standard join game URL" <|
            \_ ->
                parseJoinState
                    (appUrl
                        [ ( "gameCode", "abcd" )
                        , ( "name", "player" )
                        ]
                    )
                    |> expectJust
                        { gameCode = "abcd"
                        , hostCode = ""
                        , screenName = "player"
                        , loading = False
                        , errors = []
                        }
        , test "parses a host join game URL" <|
            \_ ->
                parseJoinState
                    (appUrl
                        [ ( "gameCode", "defg" )
                        , ( "hostCode", "1234" )
                        , ( "name", "host" )
                        ]
                    )
                    |> expectJust
                        { gameCode = "defg"
                        , hostCode = "1234"
                        , screenName = "host"
                        , loading = False
                        , errors = []
                        }
        ]


appUrl : List ( String, String ) -> Url
appUrl qsParams =
    let
        query =
            Url.Builder.toQuery <|
                List.map
                    (uncurry Url.Builder.string)
                    qsParams
    in
    { protocol = Https
    , host = "host"
    , port_ = Nothing
    , path = "/"
    , query = Just <| String.dropLeft 1 query
    , fragment = Nothing
    }


expectJust : a -> Maybe a -> Expectation
expectJust a ma =
    Maybe.withDefault
        (Expect.fail "Got nothing instead of expected value")
        (Maybe.map (Expect.equal a) ma)
