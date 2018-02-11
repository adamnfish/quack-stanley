module Msg exposing (Msg (..), update, wakeServer)

import Http
import Api exposing (wakeServerRequest, createGameRequest, joinGameRequest)
import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendAwake (Result Http.Error ())
    | CreatingNewGame String String
    | CreateNewGame String String
    | JoiningGame String String
    | JoinGame String String
    | JoinedGame (Result Http.Error ())
    | WaitForStart


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )
        CreatingNewGame gameName screenName ->
            ( { model | lifecycle = Create gameName screenName }, Cmd.none )
        CreateNewGame gameName screenName ->
            ( { model | lifecycle = Creating }, createGame gameName screenName )
        JoiningGame gameId screenName ->
            ( { model | lifecycle = Join gameId screenName }, Cmd.none )
        JoinGame gameId screenName->
            ( { model | lifecycle = Joining }, joinGame gameId screenName )
        JoinedGame _ ->
            ( { model | lifecycle = Waiting }, Cmd.none ) -- TODO: Timer and poll
        WaitForStart ->
            ( { model | lifecycle = Waiting }, Cmd.none ) -- TODO: Timer and poll


-- API calls

wakeServer : Cmd Msg
wakeServer =
    Http.send BackendAwake (wakeServerRequest)

createGame : String -> String -> Cmd Msg
createGame gameName screenName =
    Http.send JoinedGame (createGameRequest gameName screenName)

joinGame : String -> String -> Cmd Msg
joinGame gameId screenName =
    Http.send JoinedGame (joinGameRequest gameId screenName)
