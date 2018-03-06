module Msg exposing (Msg (..), update, wakeServer)

import Http
import Api exposing (wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest, becomeBuyerRequest, awardPointRequest, pingRequest, finishPitchRequest)
import Model exposing (Model, Registered, NewGame, PlayerInfo, Lifecycle (..))
import Time


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
    | CreatedGame
        ( Result Http.Error NewGame )
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
    | PingResult
        ( Result Http.Error PlayerInfo )
    | PingEvent
        Time.Time
    | FinishedPitch
        String String
    | FinishedPitchResult
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

        CreatedGame ( Err err ) ->
            ( { model | lifecycle = Error [ "Error creating game" ] }, Cmd.none )
        CreatedGame ( Ok newGame ) ->
            ( { model | lifecycle = CreatorWaiting newGame.gameCode
                      , playerKey = Just newGame.playerKey
                      , state = Just newGame.state
                      }
            , Cmd.none
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
            ( { model | lifecycle = Waiting }, Cmd.none )

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
                    ( { model | lifecycle = Error [ "No game ID or Player Key" ] }
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

        PingEvent _ ->
            case ( Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey ) of
                Just ( gameId, playerKey ) ->
                    ( model, ping gameId playerKey )
                Nothing ->
                    ( model, Cmd.none )

        PingResult ( Err err ) ->
            case model.lifecycle of
                Waiting ->
                    ( model, Cmd.none )
                _ ->
                    ( { model | lifecycle = Error [ "Lost connection to game" ] }
                    , Cmd.none
                    )
        PingResult ( Ok playerInfo ) ->
            case model.lifecycle of
                Waiting ->
                    if playerInfo.started then
                        ( { model | lifecycle = Spectating []
                                  , state = Just playerInfo.state
                                  , otherPlayers = playerInfo.otherPlayers
                          }
                        , Cmd.none
                        )
                    else
                        ( { model | state = Just playerInfo.state
                                  , otherPlayers = playerInfo.otherPlayers
                          }
                        , Cmd.none
                        )
                _ ->
                    ( { model | state = Just playerInfo.state
                              , otherPlayers = playerInfo.otherPlayers
                      }
                    , Cmd.none
                    )

        FinishedPitch word1 word2 ->
            case ( Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey ) of
                Just ( gameId, playerKey ) ->
                    ( model, finishPitch gameId playerKey ( word1, word2 ) )
                Nothing ->
                    ( model, Cmd.none )
        FinishedPitchResult ( Err err ) ->
            ( { model | lifecycle = Error [ "Lost connection to game" ] }
            , Cmd.none
            )
        FinishedPitchResult ( Ok playerInfo ) ->
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
    Http.send CreatedGame ( createGameRequest gameName screenName )

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

ping : String -> String -> Cmd Msg
ping gameId playerKey =
    Http.send PingResult ( pingRequest gameId playerKey )

finishPitch : String -> String -> ( String, String ) -> Cmd Msg
finishPitch gameId playerKey words =
    Http.send FinishedPitchResult ( finishPitchRequest gameId playerKey words )
