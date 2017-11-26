module Msg exposing (Msg, update)

import Model exposing (Model (..))


type Msg
    = BackendReady
    | CreateNewGame 

      

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        BackendReady ->
            ( Welcome True, Cmd.none )
        CreateNewGame ->
            ( model, Cmd.none )
