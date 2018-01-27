module Msg exposing (Msg (..), update)

import Http
import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendAwake (Result Http.Error ())
    | CreateNewGame



update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendAwake _ ->
            ( { model | backendAwake = True }, Cmd.none )
        CreateNewGame ->
            ( model, Cmd.none )
