module Api exposing (wakeServer)

import Http exposing (stringBody)
import Json.Decode exposing (succeed)
import Msg exposing (Msg (..))


apiUrl = "https://.../Prod/api"

wakeServerRequest : Http.Request ()
wakeServerRequest =
    Http.post apiUrl (stringBody "application/json" """{ "operation": "wake" }""") (succeed ())

wakeServer : Cmd Msg
wakeServer =
    Http.send BackendAwake (wakeServerRequest)
