module Msg exposing (Msg (..), update)

import Http
import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendAwake (Result Http.Error ())
    | SelectCreateNewGame
    | CreateNewGame
    | SelectJoinGame



update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )
        CreateNewGame ->
            ( { model | lifecycle = Create }, Cmd.none )
