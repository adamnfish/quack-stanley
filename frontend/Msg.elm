module Msg exposing (Msg (..), update)

import Http
import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendAwake (Result Http.Error ())
    | SelectCreateNewGame
    | CreateNewGame
    | SelectJoinGame
    | JoinGame
    | WaitForStart



update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )
        SelectCreateNewGame ->
            ( { model | lifecycle = Create }, Cmd.none )
        CreateNewGame ->
            ( { model | lifecycle = Creating }, Cmd.none ) -- TODO: AJAX Cmd
        SelectJoinGame ->
            ( { model | lifecycle = Join }, Cmd.none )
        JoinGame ->
            ( { model | lifecycle = Joining }, Cmd.none ) -- TODO: AJAX Cmd
        WaitForStart ->
            ( { model | lifecycle = Waiting }, Cmd.none ) -- TODO: Timer and poll
