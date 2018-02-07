module Msg exposing (Msg (..), update, wakeServer)

import Http
import Api exposing (wakeServerRequest, createGameRequest)
import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendAwake (Result Http.Error ())
    | CreatingNewGame String String
    | CreateNewGame String String
    | SelectJoinGame
    | JoinGame
    | JoinedGame (Result Http.Error ())
    | WaitForStart


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )
        CreatingNewGame gameName screenName ->
            ( { model | lifecycle = Create gameName screenName }, Cmd.none )
        CreateNewGame gameName playerName ->
            ( { model | lifecycle = Creating }, createGame gameName playerName )
        SelectJoinGame ->
            ( { model | lifecycle = Join "" "" }, Cmd.none )
        JoinGame ->
            ( { model | lifecycle = Joining }, Cmd.none ) -- TODO: AJAX Cmd
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
