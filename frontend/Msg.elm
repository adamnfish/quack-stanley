module Msg exposing (Msg, update)

import Model exposing (Model, Game, Player, Lifecycle (..))


type Msg
    = BackendReady
    | CreateNewGame



update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendReady ->
            ( { model | backendReady = True }, Cmd.none )
        CreateNewGame ->
            ( model, Cmd.none )
