module Api.Api exposing (sendApiCall)

import Api.Codecs exposing (apiErrsDecoder)
import Http
import Json.Decode exposing (decodeString)
import Model exposing (ApiError, ApiResponse(..))


sendApiCall : (ApiResponse a -> msg) -> Http.Request a -> Cmd msg
sendApiCall toMessage request =
    Http.send (toMessage << handleApiResponse) request


handleApiResponse : Result Http.Error a -> ApiResponse a
handleApiResponse result =
    case result of
        Ok a ->
            ApiOk a

        Err (Http.BadStatus response) ->
            case decodeString apiErrsDecoder response.body of
                Ok apiErrors ->
                    ApiErr apiErrors

                Err _ ->
                    ApiErr [ { message = "Invalid response from server", context = Nothing } ]

        Err (Http.BadUrl message) ->
            ApiErr [ { message = message, context = Nothing } ]

        Err Http.Timeout ->
            ApiErr [ { message = "Request to server timed out, please check your internet connection.", context = Nothing } ]

        Err Http.NetworkError ->
            ApiErr [ { message = "Unable to connect to server, please check your internet connection.", context = Nothing } ]

        Err (Http.BadPayload message response) ->
            ApiErr [ { message = message, context = Nothing } ]
