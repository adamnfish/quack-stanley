module Config exposing (apiUrl)

import Model exposing (Model)


apiUrl : Model -> String
apiUrl model =
    model.apiRoot
