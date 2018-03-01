module Msg exposing (Msg (..), update, wakeServer)

import Http
import Api exposing (wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest, becomeBuyerRequest, awardPointRequest)
import Model exposing (Model, Registered, PlayerInfo, Lifecycle (..))


type Msg
    = BackendAwake
        ( Result Http.Error () )
    | CreatingNewGame
        String String
    | CreateNewGame
        String String
    | JoiningGame
        String String
    | JoinGame
        String String
    | JoinedGame
        ( Result Http.Error Registered )
    | WaitForStart
    | StartingGame
    | GameStarted
        ( Result Http.Error PlayerInfo )
    | SelectWord
        String ( List String )
    | DeselectWord
        String ( List String )
    | RequestBuyer
    | BecomeBuyer
        ( Result Http.Error PlayerInfo )
    | AwardPoint
        String String
    | AwardedPoint
        ( Result Http.Error PlayerInfo )


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )

        CreatingNewGame gameName screenName ->
            ( { model | lifecycle = Create gameName screenName }, Cmd.none )
        CreateNewGame gameName screenName ->
            ( { model | lifecycle = Creating
                      , isCreator = True
                      }
            , createGame gameName screenName
            )

        JoiningGame gameId screenName ->
            ( { model | lifecycle = Join gameId screenName }, Cmd.none )
        JoinGame gameId screenName->
            ( { model | lifecycle = Joining
                      , isCreator = False
                      }
            , joinGame gameId screenName
            )

        JoinedGame ( Err err ) ->
            ( { model | lifecycle = Error [ "Error joining game" ] }, Cmd.none )
        JoinedGame ( Ok registered ) ->
            ( { model | lifecycle = Waiting
                      , playerKey = Just registered.playerKey
                      , state = Just registered.state
                      }
            , Cmd.none
            )

        WaitForStart ->
            ( { model | lifecycle = Waiting }, Cmd.none ) -- TODO: Timer and poll

        StartingGame ->
            case (Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey) of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = Starting }
                    , startGame gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "Could not start game" ] }, Cmd.none )

        GameStarted ( Err err ) ->
            ( { model | lifecycle = Error [ "Error starting game" ] }, Cmd.none )
        GameStarted ( Ok playerInfo ) ->
            ( { model | lifecycle = Spectating []
                      , state = Just playerInfo.state
                      , otherPlayers = playerInfo.otherPlayers
                      }
            , Cmd.none
            )

        SelectWord newWord selected ->
            let
                newSelected =
                    case selected of
                        [ ] ->
                            [ newWord ]
                        [ word ] ->
                            [ word, newWord ]
                        _ ->
                            selected
            in
                ( { model | lifecycle = Spectating newSelected }
                , Cmd.none
                )
        DeselectWord word selected ->
            let
                newSelected = List.filter ( \w -> w /= word ) selected
            in
                ( { model | lifecycle = Spectating newSelected }
                , Cmd.none
                )

        RequestBuyer ->
            case (Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey) of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = BecomingBuyer }
                    , becomeBuyer gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )


        BecomeBuyer ( Err err ) ->
            let
                hand = Maybe.withDefault [] ( Maybe.map ( \state -> state.hand ) model.state )
            in
                ( { model | lifecycle = Spectating hand
                          , errs =  [ "Could not become buyer" ]
                  }
                , Cmd.none
                )
        BecomeBuyer ( Ok playerInfo ) ->
            ( { model | lifecycle = Buying ( Maybe.withDefault "Couldn't get a role" playerInfo.state.role ) }
            , Cmd.none
            )

        AwardPoint role playerName ->
            case (Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey) of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = AwardingPoint role playerName }
                    , awardPoint gameId playerKey role playerName
                    )
                Nothing ->
                    ( { model | lifecycle= Error [ "No game ID or Player Key" ] }
                    , Cmd.none
                    )
                    
        AwardedPoint ( Err err ) ->
            ( { model | lifecycle = Error [ "Couldn't award point" ] }
            , Cmd.none
            )
        AwardedPoint ( Ok playerInfo ) ->
            ( { model | lifecycle = Spectating []
                      , state = Just playerInfo.state
              }
            , Cmd.none
            )

-- API calls

wakeServer : Cmd Msg
wakeServer =
    Http.send BackendAwake ( wakeServerRequest )

createGame : String -> String -> Cmd Msg
createGame gameName screenName =
    Http.send JoinedGame ( createGameRequest gameName screenName )

joinGame : String -> String -> Cmd Msg
joinGame gameId screenName =
    Http.send JoinedGame ( joinGameRequest gameId screenName )

startGame : String -> String -> Cmd Msg
startGame gameId playerKey =
    Http.send GameStarted ( startGameRequest gameId playerKey )

becomeBuyer : String -> String -> Cmd Msg
becomeBuyer gameId playerKey =
    Http.send BecomeBuyer ( becomeBuyerRequest gameId playerKey )

awardPoint : String -> String -> String -> String -> Cmd Msg
awardPoint gameId playerKey role playerName =
    Http.send AwardedPoint ( awardPointRequest gameId playerKey role playerName )
