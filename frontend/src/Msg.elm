module Msg exposing (Msg (..), update, wakeServer)

import Api.Api exposing (sendApiCall)
import Api.Requests exposing (wakeServerRequest, createGameRequest, joinGameRequest, startGameRequest, becomeBuyerRequest, awardPointRequest, pingRequest, finishPitchRequest)
import Model exposing (Model, Registered, NewGame, PlayerInfo, SavedGame, Lifecycle (..), PitchStatus (..), ApiResponse (..))
import Time exposing (Time)
import Ports exposing (fetchSavedGames, saveGame, removeSavedGame)


type Msg
    = BackendAwake
        ( ApiResponse () )
    | WelcomeTick
        Time
    | LoadedGames
        ( List SavedGame )
    | NavigateHome
    | NavigateSpectate
    | CreatingNewGame
        String String
    | CreateNewGame
        String String
    | JoiningGame
        String String
    | JoinGame
        String String
    | CreatedGame
        String String ( ApiResponse NewGame )
    | JoinedGame
        ( ApiResponse Registered )
    | RejoinGame
        SavedGame
    | RemoveSavedGame
        SavedGame
    | StartingGame
    | GameStarted
        ( ApiResponse PlayerInfo )
    | SelectWord
        String ( List String )
    | DeselectWord
        String ( List String )
    | RequestBuyer
    | BecomeBuyer
        ( ApiResponse PlayerInfo )
    | AwardPoint
        String String
    | AwardedPoint
        ( ApiResponse PlayerInfo )
    | PingResult
        ( ApiResponse PlayerInfo )
    | PingEvent
        Time.Time
    | StartPitch
        String String
    | RevealCard
        String String PitchStatus
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
            ( { model | lifecycle = Welcome }
            , fetchSavedGames ()
            )

        WelcomeTick time ->
            ( { model | time = time }
            , fetchSavedGames ()
            )

        NavigateSpectate ->
            ( { model | lifecycle = Spectating [] }, Cmd.none )

        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )

        LoadedGames games ->
            ( { model | savedGames = games }, Cmd.none )

        CreatingNewGame gameName screenName ->
            let
                createState =
                    { gameName = gameName
                    , screenName = screenName
                    , loading = False
                    , errors = []
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
            ( { model | lifecycle = CreatorWaiting newGame.gameCode
                      , playerKey = Just newGame.playerKey
                      , state = Just newGame.state
                      , isCreator = True
                      }
            , Cmd.none
            )
        JoinedGame ( ApiErr err ) ->
            ( { model | lifecycle = Error ( List.map .message err ) }, Cmd.none )
        JoinedGame ( ApiOk registered ) ->
            ( { model | lifecycle = Waiting
                      , playerKey = Just registered.playerKey
                      , state = Just registered.state
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
                ( { model | lifecycle = Rejoining
                          , playerKey = Just savedGame.playerKey
                          , state = Just temporaryState
                          }
                , ping savedGame.gameId savedGame.playerKey
                )

        RemoveSavedGame savedGame ->
            ( model
            , removeSavedGame savedGame
            )

        StartingGame ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = Starting }
                    , startGame gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "Could not start game" ] }, Cmd.none )

        GameStarted ( ApiErr err ) ->
            ( { model | lifecycle = Error ( List.map .message err ) }, Cmd.none )
        GameStarted ( ApiOk playerInfo ) ->
            let
                updatedModel =
                    { model | lifecycle = Spectating []
                            , state = Just playerInfo.state
                            , opponents = playerInfo.opponents
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
            case keys model of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = BecomingBuyer }
                    , becomeBuyer gameId playerKey
                    )
                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )


        BecomeBuyer ( ApiErr err ) ->
            ( { model | lifecycle = Spectating []
                      , errs =  List.map .message err
              }
            , Cmd.none
            )
        BecomeBuyer ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Buying ( Maybe.withDefault "Couldn't get a role" playerInfo.state.role ) }
            , Cmd.none
            )

        AwardPoint role playerName ->
            case keys model of
                Just (gameId, playerKey) ->
                    ( { model | lifecycle = AwardingPoint role playerName }
                    , awardPoint gameId playerKey role playerName
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
            ( { model | lifecycle = Spectating []
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
              }
            , Cmd.none
            )

        PingEvent _ ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( model, ping gameId playerKey )
                Nothing ->
                    ( model, Cmd.none )

        PingResult ( ApiErr err ) ->
            case model.lifecycle of
                Waiting ->
                    ( model, Cmd.none )
                _ ->
                    ( { model | lifecycle = Error ( List.map .message err ) }
                    , Cmd.none
                    )
        PingResult ( ApiOk playerInfo ) ->
            case model.lifecycle of
                Waiting ->
                    if playerInfo.started then
                        let
                            updatedModel =
                                { model | lifecycle = Spectating []
                                        , state = Just playerInfo.state
                                        , opponents = playerInfo.opponents
                                }
                        in
                            ( updatedModel
                            , saveGame updatedModel
                            )
                    else
                        ( { model | state = Just playerInfo.state
                                  , opponents = playerInfo.opponents
                          }
                        , Cmd.none
                        )
                Rejoining ->
                        ( { model | lifecycle = Spectating []
                                  , state = Just playerInfo.state
                                  , opponents = playerInfo.opponents
                          }
                        , Cmd.none
                        )
                _ ->
                    ( { model | state = Just playerInfo.state
                              , opponents = playerInfo.opponents
                      }
                    , Cmd.none
                    )

        StartPitch word1 word2 ->
            ( { model | lifecycle = Pitching word1 word2 NoCards }
            , Cmd.none
            )
        RevealCard word1 word2 pitchStatus ->
            let
                nextPitchStatus = case pitchStatus of
                    NoCards  -> OneCard
                    OneCard  -> TwoCards
                    TwoCards -> TwoCards
            in
                ( { model | lifecycle = Pitching word1 word2 nextPitchStatus }
                , Cmd.none
                )
        FinishedPitch word1 word2 ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( model, finishPitch gameId playerKey ( word1, word2 ) )
                Nothing ->
                    ( model, Cmd.none )
        FinishedPitchResult ( ApiErr err ) ->
            ( { model | lifecycle = Error ( List.map .message err ) }
            , Cmd.none
            )
        FinishedPitchResult ( ApiOk playerInfo ) ->
            ( { model | lifecycle = Spectating []
                      , state = Just playerInfo.state
                      , opponents = playerInfo.opponents
              }
            , Cmd.none
            )


-- API calls

wakeServer : Cmd Msg
wakeServer =
    sendApiCall BackendAwake ( wakeServerRequest )

createGame : String -> String -> Cmd Msg
createGame gameName screenName =
    sendApiCall ( CreatedGame gameName screenName ) ( createGameRequest gameName screenName )

joinGame : String -> String -> Cmd Msg
joinGame gameId screenName =
    sendApiCall JoinedGame ( joinGameRequest gameId screenName )

startGame : String -> String -> Cmd Msg
startGame gameId playerKey =
    sendApiCall GameStarted ( startGameRequest gameId playerKey )

becomeBuyer : String -> String -> Cmd Msg
becomeBuyer gameId playerKey =
    sendApiCall BecomeBuyer ( becomeBuyerRequest gameId playerKey )

awardPoint : String -> String -> String -> String -> Cmd Msg
awardPoint gameId playerKey role playerName =
    sendApiCall AwardedPoint ( awardPointRequest gameId playerKey role playerName )

ping : String -> String -> Cmd Msg
ping gameId playerKey =
    sendApiCall PingResult ( pingRequest gameId playerKey )

finishPitch : String -> String -> ( String, String ) -> Cmd Msg
finishPitch gameId playerKey words =
    sendApiCall FinishedPitchResult ( finishPitchRequest gameId playerKey words )
