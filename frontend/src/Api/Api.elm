module Api.Api exposing (sendApiCall)

import Http
import Model exposing (ApiError, ApiResponse (..))
import Json.Decode exposing (decodeString)
import Api.Codecs exposing (apiErrsDecoder)


sendApiCall : (ApiResponse a -> msg) -> Http.Request a -> Cmd msg
sendApiCall toMessage request =
    Http.send ( toMessage << handleApiResponse ) request


handleApiResponse : Result Http.Error a -> ApiResponse a
handleApiResponse result =
    case result of
        Ok a ->
            ApiOk a
        Err ( Http.BadStatus response ) ->
            case ( decodeString apiErrsDecoder response.body ) of
                Ok apiErrors ->
                    ApiErr apiErrors
                Err _ ->
                    ApiErr [ { message = "Invalid response from server", context = Nothing } ]
        Err ( Http.BadUrl message ) ->
            ApiErr [ { message = message, context = Nothing } ]
        Err Http.Timeout ->
            ApiErr [ { message = "Request timed out", context = Nothing } ]
        Err Http.NetworkError ->
            ApiErr [ { message = "Connection error", context = Nothing } ]
        Err ( Http.BadPayload message response ) ->
            ApiErr [ { message = message, context = Nothing } ]
