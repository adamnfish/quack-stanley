module Msg exposing (Msg(..), processUrlChange, update, wakeServer)

import Api.Api exposing (sendApiCall)
import Api.Requests exposing (awardPointRequest, becomeBuyerRequest, createGameRequest, finishPitchRequest, lobbyPingRequest, pingRequest, registerHostRequest, registerPlayerRequest, relinquishBuyerRequest, startGameRequest, wakeServerRequest)
import Browser exposing (UrlRequest(..))
import Browser.Navigation as Navigation
import List.Extra
import Model exposing (ApiError, ApiResponse(..), Lifecycle(..), Model, NewGame, PlayerInfo, Registered, SavedGame)
import Ports exposing (fetchSavedGames, removeSavedGame, saveGame)
import Routing exposing (gameUrl, homeUrl, parseCurrentGame, parseJoinState)
import Time exposing (Posix)
import Url
import Utils exposing (gameCodeFromId, nonEmpty, tuple2)


type Msg
    = BackendAwake (ApiResponse ())
    | UrlRequested UrlRequest
    | UrlChanged Url.Url
    | WelcomeTick Posix
    | LoadedGames (List SavedGame)
    | NavigateHome
    | NavigateSpectate
    | CreatingNewGame String String (List ApiError)
    | CreateNewGame String String
    | JoiningGame String String String (List ApiError)
    | JoinGame String String
    | JoinGameAsHost String String String
    | CreatedGame String String (ApiResponse NewGame)
    | JoinedGame String String String (ApiResponse Registered)
    | RejoinGame SavedGame
    | RemoveSavedGame SavedGame
    | StartingGame String
    | GameStarted String (ApiResponse PlayerInfo)
    | SelectWord String (List String)
    | DeselectWord String (List String)
    | RequestBuyer
    | BecomeBuyer (ApiResponse PlayerInfo)
    | RelinquishBuyer
    | RelinquishBuyerResult (ApiResponse PlayerInfo)
    | AwardPoint String String
    | AwardedPoint (ApiResponse PlayerInfo)
    | PingResult (ApiResponse PlayerInfo)
    | PingEvent Posix
    | LobbyPingResult (ApiResponse PlayerInfo)
    | LobbyPingEvent Posix
    | StartPitch String String
    | FinishedPitch String String
    | FinishedPitchResult (ApiResponse PlayerInfo)


keys : Model -> Maybe ( String, String )
keys model =
    Maybe.map2 tuple2
        (Maybe.map .gameId model.state)
        model.playerKey


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        NavigateHome ->
            ( model
            , Navigation.pushUrl model.urlKey <|
                homeUrl model.url
            )

        UrlRequested urlRequest ->
            case urlRequest of
                Internal url ->
                    ( model
                    , Navigation.pushUrl model.urlKey (Url.toString url)
                    )

                External url ->
                    ( model
                    , Navigation.load url
                    )

        UrlChanged url ->
            processUrlChange url model

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

        JoiningGame gameId hostCode screenName errs ->
            let
                joinState =
                    { gameCode = gameId
                    , hostCode = hostCode
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
            ( { model | lifecycle = Join joinState }
            , Cmd.none
            )

        JoinGameAsHost gameId hostCode screenName ->
            let
                joinState =
                    { gameCode = gameId
                    , hostCode = hostCode
                    , screenName = screenName
                    , loading = True
                    , errors = []
                    }
            in
            ( { model | lifecycle = Join joinState }
            , registerHost model gameId hostCode screenName
            )

        JoinGame gameId screenName ->
            let
                joinState =
                    { gameCode = gameId
                    , hostCode = ""
                    , screenName = screenName
                    , loading = True
                    , errors = []
                    }
            in
            ( { model | lifecycle = Join joinState }
            , registerPlayer model gameId screenName
            )

        CreatedGame gameName screenName (ApiErr errs) ->
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

        CreatedGame _ _ (ApiOk newGame) ->
            ( { model
                | lifecycle = HostWaiting newGame.gameCode []
                , playerKey = Just newGame.playerKey
                , state = Just newGame.state
                , isHost = True
                , opponents = []
                , round = Nothing
              }
            , Cmd.none
            )

        JoinedGame gameCode hostCode screenName (ApiErr errs) ->
            let
                joinState =
                    { gameCode = gameCode
                    , hostCode = hostCode
                    , screenName = screenName
                    , loading = False
                    , errors = errs
                    }
            in
            ( { model | lifecycle = Join joinState }
            , Cmd.none
            )

        JoinedGame gameCode hostCode _ (ApiOk registered) ->
            let
                lifecycle =
                    if nonEmpty hostCode then
                        HostWaiting gameCode []

                    else
                        Waiting
            in
            ( { model
                | lifecycle = lifecycle
                , playerKey = Just registered.playerKey
                , state = Just registered.state
                , isHost = False
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
            ( { model
                | lifecycle = Rejoining savedGame
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
                        errors =
                            [ { message = "Could not start game", context = Nothing } ]
                    in
                    ( { model | lifecycle = HostWaiting gameCode errors }, Cmd.none )

        GameStarted gameCode (ApiErr errs) ->
            ( { model | lifecycle = HostWaiting gameCode errs }, Cmd.none )

        GameStarted _ (ApiOk playerInfo) ->
            let
                updatedModel =
                    { model
                        | lifecycle = Spectating [] []
                        , state = Just playerInfo.state
                        , opponents = playerInfo.opponents
                        , round = playerInfo.round
                    }
            in
            ( updatedModel
            , Cmd.batch
                [ saveGame updatedModel
                , Navigation.pushUrl model.urlKey <|
                    gameUrl model.url (gameCodeFromId playerInfo.state.gameId) playerInfo.state.screenName
                ]
            )

        SelectWord newWord selected ->
            let
                newSelected =
                    case selected of
                        [] ->
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
                newSelected =
                    List.filter (\w -> w /= word) selected
            in
            ( { model | lifecycle = Spectating newSelected [] }
            , Cmd.none
            )

        RequestBuyer ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = BecomingBuyer }
                    , becomeBuyer model gameId playerKey
                    )

                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )

        BecomeBuyer (ApiErr errs) ->
            ( { model | lifecycle = Spectating [] errs }
            , Cmd.none
            )

        BecomeBuyer (ApiOk playerInfo) ->
            ( { model
                | lifecycle = Buying (Maybe.withDefault "Couldn't get a role" playerInfo.state.role)
                , state = Just playerInfo.state
                , opponents = playerInfo.opponents
                , round = playerInfo.round
              }
            , Cmd.none
            )

        RelinquishBuyer ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = RelinquishingBuyer }
                    , relinquishBuyer model gameId playerKey
                    )

                Nothing ->
                    ( { model | lifecycle = Error [ "No player key or game ID" ] }, Cmd.none )

        RelinquishBuyerResult (ApiErr errs) ->
            ( { model | lifecycle = Spectating [] errs }
            , Cmd.none
            )

        RelinquishBuyerResult (ApiOk playerInfo) ->
            ( { model
                | lifecycle = Spectating [] []
                , state = Just playerInfo.state
                , opponents = playerInfo.opponents
                , round = playerInfo.round
              }
            , Cmd.none
            )

        AwardPoint role playerName ->
            case keys model of
                Just ( gameId, playerKey ) ->
                    ( { model | lifecycle = AwardingPoint role playerName }
                    , awardPoint model gameId playerKey role playerName
                    )

                Nothing ->
                    ( { model | lifecycle = Error [ "No game ID or Player Key" ] }
                    , Cmd.none
                    )

        AwardedPoint (ApiErr err) ->
            ( { model | lifecycle = Error (List.map .message err) }
            , Cmd.none
            )

        AwardedPoint (ApiOk playerInfo) ->
            ( { model
                | lifecycle = Spectating [] []
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

        PingResult (ApiErr _) ->
            case model.lifecycle of
                Waiting ->
                    ( model, Cmd.none )

                _ ->
                    ( model, Cmd.none )

        PingResult (ApiOk playerInfo) ->
            case model.lifecycle of
                Waiting ->
                    if playerInfo.started then
                        let
                            updatedModel =
                                { model
                                    | lifecycle = Spectating [] []
                                    , state = Just playerInfo.state
                                    , opponents = playerInfo.opponents
                                    , round = playerInfo.round
                                }
                        in
                        ( updatedModel
                        , Cmd.batch
                            [ saveGame updatedModel
                            , Navigation.pushUrl model.urlKey <|
                                gameUrl model.url (gameCodeFromId playerInfo.state.gameId) playerInfo.state.screenName
                            ]
                        )

                    else
                        ( { model
                            | state = Just playerInfo.state
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

                        targetUrl =
                            gameUrl model.url (gameCodeFromId playerInfo.state.gameId) playerInfo.state.screenName

                        currentUrl =
                            Url.toString model.url

                        navigateCmd =
                            if targetUrl /= currentUrl then
                                Navigation.pushUrl model.urlKey targetUrl

                            else
                                Cmd.none
                    in
                    ( { model
                        | lifecycle = prevLifecycle
                        , state = Just playerInfo.state
                        , opponents = playerInfo.opponents
                        , round = playerInfo.round
                      }
                    , navigateCmd
                    )

                _ ->
                    ( { model
                        | state = Just playerInfo.state
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

        LobbyPingResult (ApiErr errs) ->
            case model.lifecycle of
                HostWaiting gameCode _ ->
                    ( { model | lifecycle = HostWaiting gameCode errs }
                    , Cmd.none
                    )

                _ ->
                    ( model
                    , Cmd.none
                    )

        LobbyPingResult (ApiOk playerInfo) ->
            case model.lifecycle of
                HostWaiting _ _ ->
                    ( { model
                        | state = Just playerInfo.state
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
                    , finishPitch model gameId playerKey ( word1, word2 )
                    )

                Nothing ->
                    ( model, Cmd.none )

        FinishedPitchResult (ApiErr err) ->
            ( { model | lifecycle = Error (List.map .message err) }
            , Cmd.none
            )

        FinishedPitchResult (ApiOk playerInfo) ->
            ( { model
                | lifecycle = Spectating [] []
                , state = Just playerInfo.state
                , opponents = playerInfo.opponents
                , round = playerInfo.round
              }
            , Cmd.none
            )


processUrlChange : Url.Url -> Model -> ( Model, Cmd Msg )
processUrlChange url model =
    -- this is separate so it can be called on navigation and when the app starts up
    let
        modelWithUrl =
            { model | url = url }

        maybeCurrentGame =
            parseCurrentGame url

        maybeJoinState =
            parseJoinState url
    in
    case ( url.query, maybeCurrentGame, maybeJoinState ) of
        ( Nothing, _, _ ) ->
            -- no query means we are navigating 'home' i.e. the welcome screen
            ( resetModelForHome modelWithUrl
            , fetchSavedGames ()
            )

        ( _, Just ( gameCode, screenName ), _ ) ->
            -- we are navigating to an existing game
            let
                alreadyNavigating =
                    case model.lifecycle of
                        Rejoining savedGame ->
                            gameCode == gameCodeFromId savedGame.gameId && screenName == savedGame.screenName

                        _ ->
                            False

                currentGameCode =
                    Maybe.withDefault "" <|
                        Maybe.map (.gameId >> gameCodeFromId) model.state

                currentScreenName =
                    Maybe.withDefault "" <|
                        Maybe.map .screenName model.state
            in
            if alreadyNavigating then
                -- we're already travelling into this game, nothing to do
                ( modelWithUrl
                , Cmd.none
                )

            else if gameCode == currentGameCode && screenName == currentScreenName then
                -- we're already on this game, nothing to do
                ( modelWithUrl
                , Cmd.none
                )

            else
                -- need to try and lookup the game in saved games
                case List.Extra.find (\sg -> String.startsWith gameCode sg.gameId && screenName == sg.screenName) model.savedGames of
                    Just savedGame ->
                        -- we found the game registration so we can perform a rejoin
                        update (RejoinGame savedGame) modelWithUrl

                    Nothing ->
                        -- game does not exist
                        -- we could show an error but this is "expected" when players re-open the game after a few days
                        -- so let's just send them home where they'll see the game they're after, or not
                        ( resetModelForHome modelWithUrl
                        , Cmd.batch
                            [ fetchSavedGames ()

                            -- this needs to be a replace so that back works
                            , Navigation.replaceUrl model.urlKey <|
                                homeUrl url
                            ]
                        )

        ( _, _, Just joinState ) ->
            -- if we're navigating to a join page then we can clear the current game state and show the join screen
            let
                blankModel =
                    resetModelForHome model
            in
            ( { blankModel | lifecycle = Join joinState }
            , Cmd.none
            )

        _ ->
            -- no matches so let's assume this isn't a URL change we want to process
            ( modelWithUrl
            , Cmd.none
            )


resetModelForHome : Model -> Model
resetModelForHome model =
    { model
        | lifecycle = Welcome
        , playerKey = Nothing
        , state = Nothing
        , isHost = False
        , opponents = []
        , round = Nothing
    }



-- API calls


wakeServer : Model -> Cmd Msg
wakeServer model =
    sendApiCall BackendAwake (wakeServerRequest model)


createGame : Model -> String -> String -> Cmd Msg
createGame model gameName screenName =
    sendApiCall (CreatedGame gameName screenName) (createGameRequest model gameName screenName)


registerHost : Model -> String -> String -> String -> Cmd Msg
registerHost model gameCode hostCode screenName =
    sendApiCall (JoinedGame gameCode hostCode screenName) (registerHostRequest model gameCode hostCode screenName)


registerPlayer : Model -> String -> String -> Cmd Msg
registerPlayer model gameCode screenName =
    sendApiCall (JoinedGame gameCode "" screenName) (registerPlayerRequest model gameCode screenName)


startGame : Model -> String -> String -> String -> Cmd Msg
startGame model gameCode gameId playerKey =
    sendApiCall (GameStarted gameCode) (startGameRequest model gameId playerKey)


becomeBuyer : Model -> String -> String -> Cmd Msg
becomeBuyer model gameId playerKey =
    sendApiCall BecomeBuyer (becomeBuyerRequest model gameId playerKey)


relinquishBuyer : Model -> String -> String -> Cmd Msg
relinquishBuyer model gameId playerKey =
    sendApiCall RelinquishBuyerResult (relinquishBuyerRequest model gameId playerKey)


awardPoint : Model -> String -> String -> String -> String -> Cmd Msg
awardPoint model gameId playerKey role playerName =
    sendApiCall AwardedPoint (awardPointRequest model gameId playerKey role playerName)


ping : Model -> String -> String -> Cmd Msg
ping model gameId playerKey =
    sendApiCall PingResult (pingRequest model gameId playerKey)


lobbyPing : Model -> String -> String -> Cmd Msg
lobbyPing model gameId playerKey =
    sendApiCall LobbyPingResult (lobbyPingRequest model gameId playerKey)


finishPitch : Model -> String -> String -> ( String, String ) -> Cmd Msg
finishPitch model gameId playerKey words =
    sendApiCall FinishedPitchResult (finishPitchRequest model gameId playerKey words)
