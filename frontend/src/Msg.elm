module Msg exposing (Msg (..), update, wakeServer)

import Api.Api exposing (sendApiCall)
import Api.Requests exposing (awardPointRequest, becomeBuyerRequest, createGameRequest, finishPitchRequest, joinGameRequest, lobbyPingRequest, pingRequest, relinquishBuyerRequest, startGameRequest, wakeServerRequest)
import Model exposing (Model, Registered, NewGame, PlayerInfo, SavedGame, Lifecycle (..), ApiResponse (..), ApiError)
import Time exposing (Posix)
import Ports exposing (fetchSavedGames, saveGame, removeSavedGame)


type Msg
    = BackendAwake
        ( ApiResponse () )
    | WelcomeTick
        Posix
    | LoadedGames
        ( List SavedGame )
    | NavigateHome
    | NavigateSpectate
    | CreatingNewGame
        String String ( List ApiError )
    | CreateNewGame
        String String
    | JoiningGame
        String String ( List ApiError )
    | JoinGame
        String String
    | CreatedGame
        String String ( ApiResponse NewGame )
    | JoinedGame
        String String ( ApiResponse Registered )
    | RejoinGame
        SavedGame
    | RemoveSavedGame
        SavedGame
    | StartingGame
        String
    | GameStarted
        String ( ApiResponse PlayerInfo )
    | SelectWord
        String ( List String )
    | DeselectWord
        String ( List String )
    | RequestBuyer
    | BecomeBuyer
        ( ApiResponse PlayerInfo )
    | RelinquishBuyer
    | RelinquishBuyerResult
        ( ApiResponse PlayerInfo )
    | AwardPoint
        String String
    | AwardedPoint
        ( ApiResponse PlayerInfo )
    | PingResult
        ( ApiResponse PlayerInfo )
    | PingEvent
        Posix
    | LobbyPingResult
        ( ApiResponse PlayerInfo )
    | LobbyPingEvent
        Posix
    | StartPitch
        String String
    | FinishedPitch
        String String
    | FinishedPitchResult
        ( ApiResponse PlayerInfo )


keys : Model -> Maybe ( String, String )
keys model =
    Maybe.map2 (\state -> \playerKey -> (state.gameId, playerKey)) model.state model.playerKey


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NavigateHome ->
            ( { model | lifecycle = Welcome
                      , playerKey = Nothing
                      , state = Nothing
                      , isCreator = False
                      , opponents = []
                      , round = Nothing
            }
            , fetchSavedGames ()
            )

        WelcomeTick time ->
            ( { model | time = Time.posixToMillis time }
            , fetchSavedGames ()
            )

        NavigateSpectate ->
            ( { model | lifecycle = Spectating [] [] }, Cmd.none )

        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )

        LoadedGames games ->
            ( { model | savedGames = games }, Cmd.none )

        CreatingNewGame gameName screenName errs ->
            let
                createState =
                    { gameName = gameName
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
                ( { model | lifecycle = Create createState }
                , Cmd.none
                )
        CreateNewGame gameName screenName ->
            let
                createState =
                    { gameName = gameName
                    , screenName = screenName
                    , loading = True
                    , errors = []
                    }
            in
                ( { model | lifecycle = Create createState }
                , createGame model gameName screenName
                )

        JoiningGame gameId screenName errs ->
            let
                joinState =
                    { gameCode = gameId
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
                ( { model | lifecycle = Join joinState }
                , Cmd.none
                )
        JoinGame gameId screenName ->
            let
                joinState =
                    { gameCode = gameId
                    , screenName = screenName
                    , loading = True
                    , errors = []
                    }
            in
                ( { model | lifecycle = Join joinState }
                , joinGame model gameId screenName
                )

        CreatedGame gameName screenName ( ApiErr errs ) ->
            let
                createState =
                    { gameName = gameName
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
                ( { model | lifecycle = Create createState }
                , Cmd.none
                )
        CreatedGame _ _ ( ApiOk newGame ) ->
            ( { model | lifecycle = CreatorWaiting newGame.gameCode []
                      , playerKey = Just newGame.playerKey
                      , state = Just newGame.state
                      , isCreator = True
                      , opponents = []
                      , round = Nothing
                      }
            , Cmd.none
            )
        JoinedGame gameCode screenName ( ApiErr errs ) ->
            let
                joinState =
                    { gameCode = gameCode
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
                ( { model | lifecycle = Join joinState }
                , Cmd.none
                )
        JoinedGame _ _ ( ApiOk registered ) ->
            ( { model | lifecycle = Waiting
                      , playerKey = Just registered.playerKey
                      , state = Just registered.state
                      , isCreator = False
                      }
            , Cmd.none
            )

        RejoinGame savedGame ->
            let
                temporaryState =
                    { gameId = savedGame.gameId
                    , gameName = savedGame.gameName
                    , screenName = savedGame.screenName
                    , hand = []
                    , discardedWords = []
                    , role = Nothing
                    , points = []
                    }
            in
                ( { model | lifecycle = Rejoining savedGame
                          , playerKey = Just savedGame.playerKey
                          , state = Just temporaryState
                          }
                , ping model savedGame.gameId savedGame.playerKey
                )

        RemoveSavedGame savedGame ->
            ( model
            , removeSavedGame savedGame
            )

        StartingGame gameCode ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = Starting }
                    , startGame model gameCode gameId playerKey
                    )
                Nothing ->
                    let
                        errors = [ { message = "Could not start game", context = Nothing } ]
                    in
                        ( { model | lifecycle = CreatorWaiting gameCode errors }, Cmd.none )

        GameStarted gameCode ( ApiErr errs ) ->
            ( { model | lifecycle = CreatorWaiting gameCode errs }, Cmd.none )
        GameStarted gameCode ( ApiOk playerInfo ) ->
            let
                updatedModel =
                    { model | lifecycle = Spectating [] []
                            , state = Just playerInfo.state
                            , opponents = playerInfo.opponents
                            , round = playerInfo.round
                    }
            in
                ( updatedModel
                , saveGame updatedModel
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
                ( { model | lifecycle = Spectating newSelected [] }
                , Cmd.none
                )
        DeselectWord word selected ->
            let
                newSelected = List.filter ( \w -> w /= word ) selected
            in
                ( { model | lifecycle = Spectating newSelected [] }
                , Cmd.none
                )

        RequestBuyer ->
            case keys model of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = BecomingBuyer }
                    , becomeBuyer model gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )


        BecomeBuyer ( ApiErr errs ) ->
            ( { model | lifecycle = Spectating [] errs }
            , Cmd.none
            )
        BecomeBuyer ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Buying ( Maybe.withDefault "Couldn't get a role" playerInfo.state.role )
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
                      , round = playerInfo.round
            }
            , Cmd.none
            )

        RelinquishBuyer ->
            case keys model of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = RelinquishingBuyer }
                    , relinquishBuyer model gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )

        RelinquishBuyerResult ( ApiErr errs ) ->
            ( { model | lifecycle = Spectating [] errs }
            , Cmd.none
            )
        RelinquishBuyerResult ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Spectating [] []
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
                      , round = playerInfo.round
              }
            , Cmd.none
            )

        AwardPoint role playerName ->
            case keys model of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = AwardingPoint role playerName }
                    , awardPoint model gameId playerKey role playerName
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "No game ID or Player Key" ] }
                    , Cmd.none
                    )
                    
        AwardedPoint ( ApiErr err ) ->
            ( { model | lifecycle = Error ( List.map .message err ) }
            , Cmd.none
            )
        AwardedPoint ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Spectating [] []
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
                      , round = playerInfo.round
              }
            , Cmd.none
            )

        PingEvent _ ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( model, ping model gameId playerKey )
                Nothing ->
                    ( model, Cmd.none )

        PingResult ( ApiErr err ) ->
            case model.lifecycle of
                Waiting ->
                    ( model, Cmd.none )
                _ ->
                    ( model , Cmd.none )
        PingResult ( ApiOk playerInfo ) ->
            case model.lifecycle of
                Waiting ->
                    if playerInfo.started then
                        let
                            updatedModel =
                                { model | lifecycle = Spectating [] []
                                        , state = Just playerInfo.state
                                        , opponents = playerInfo.opponents
                                        , round = playerInfo.round
                                }
                        in
                            ( updatedModel
                            , saveGame updatedModel
                            )
                    else
                        ( { model | state = Just playerInfo.state
                                  , opponents = playerInfo.opponents
                                  , round = playerInfo.round
                          }
                        , Cmd.none
                        )
                Rejoining _ ->
                    let
                        prevLifecycle =
                            case playerInfo.state.role of
                                Just role ->
                                    Buying role
                                Nothing ->
                                    Spectating [] []
                    in
                        ( { model | lifecycle = prevLifecycle
                                  , state = Just playerInfo.state
                                  , opponents = playerInfo.opponents
                                  , round = playerInfo.round
                          }
                        , Cmd.none
                        )
                _ ->
                    ( { model | state = Just playerInfo.state
                              , opponents = playerInfo.opponents
                              , round = playerInfo.round
                      }
                    , Cmd.none
                    )

        LobbyPingEvent _ ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( model, lobbyPing model gameId playerKey )
                Nothing ->
                    ( model, Cmd.none )

        LobbyPingResult ( ApiErr errs ) ->
            case model.lifecycle of
                CreatorWaiting gameCode _ ->
                    ( { model | lifecycle = CreatorWaiting gameCode errs }
                    , Cmd.none
                    )
                _ ->
                    ( model
                    , Cmd.none
                    )
        LobbyPingResult ( ApiOk playerInfo ) ->
            case model.lifecycle of
                CreatorWaiting _ _ ->
                    ( { model | state = Just playerInfo.state
                              , opponents = playerInfo.opponents
                      }
                    , Cmd.none
                    )
                _ ->
                    ( model
                    , Cmd.none
                    )

        StartPitch word1 word2 ->
            ( { model | lifecycle = Pitching word1 word2 False }
            , Cmd.none
            )
        FinishedPitch word1 word2 ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = Pitching word1 word2 True }
                    , finishPitch model gameId playerKey ( word1, word2 ) )
                Nothing ->
                    ( model, Cmd.none )
        FinishedPitchResult ( ApiErr err ) ->
            ( { model | lifecycle = Error ( List.map .message err ) }
            , Cmd.none
            )
        FinishedPitchResult ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Spectating [] []
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
                      , round = playerInfo.round
              }
            , Cmd.none
            )


-- API calls

wakeServer : Model -> Cmd Msg
wakeServer model =
    sendApiCall BackendAwake ( wakeServerRequest model )

createGame : Model -> String -> String -> Cmd Msg
createGame model gameName screenName =
    sendApiCall ( CreatedGame gameName screenName ) ( createGameRequest model gameName screenName )

joinGame : Model -> String -> String -> Cmd Msg
joinGame model gameCode screenName =
    sendApiCall ( JoinedGame gameCode screenName ) ( joinGameRequest model gameCode screenName )

startGame : Model -> String -> String -> String -> Cmd Msg
startGame model gameCode gameId playerKey =
    sendApiCall ( GameStarted gameCode ) ( startGameRequest model gameId playerKey )

becomeBuyer : Model -> String -> String -> Cmd Msg
becomeBuyer model gameId playerKey =
    sendApiCall BecomeBuyer ( becomeBuyerRequest model gameId playerKey )

relinquishBuyer : Model -> String -> String -> Cmd Msg
relinquishBuyer model gameId playerKey =
    sendApiCall RelinquishBuyerResult ( relinquishBuyerRequest model gameId playerKey )

awardPoint : Model -> String -> String -> String -> String -> Cmd Msg
awardPoint model gameId playerKey role playerName =
    sendApiCall AwardedPoint ( awardPointRequest model gameId playerKey role playerName )

ping : Model -> String -> String -> Cmd Msg
ping model gameId playerKey =
    sendApiCall PingResult ( pingRequest model gameId playerKey )

lobbyPing : Model -> String -> String -> Cmd Msg
lobbyPing model gameId playerKey =
    sendApiCall LobbyPingResult ( lobbyPingRequest model gameId playerKey )

finishPitch : Model -> String -> String -> ( String, String ) -> Cmd Msg
finishPitch model gameId playerKey words =
    sendApiCall FinishedPitchResult ( finishPitchRequest model gameId playerKey words )
