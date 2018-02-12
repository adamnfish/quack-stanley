module Model exposing (Model, PlayerState, PlayerInfo, Registered, Lifecycle(..))


type Lifecycle
    = Welcome               -- initial screen (backend awake)
    | Create String String  -- create new game (gameName, screenName)
    | Creating              -- waiting for API to confirm creation
    | Join String String    -- join existing game (gameId, screenName)
    | Joining               -- waiting for API to confirm game entry
    | Waiting               -- waiting for game to start (will have game and screen name)
    | Spectating            -- game has started, player is spectating (and can choose words)
    | Pitching              -- player is pitching two cards from hand
    | ChooseRole            -- player is given two roles to choose from
    | Buyer                 -- player is the buyer, will have a role
    | Error ( List String ) -- player is the buyer, will have a role

type alias PlayerState =
    { gameId : String
    , gameName : String
    , screenName : String
    , hand : List String
    , discardedWords : List String
    , role : Maybe String -- has role if current player
    , points : List String
    }

type alias Model =
    { lifecycle : Lifecycle
    , backendAwake : Bool
    , state : Maybe PlayerState
    , playerKey : Maybe String
    }

-- API responses

type alias PlayerInfo =
    { state : PlayerState
    , started : Bool
    , otherPlayers : List String
    }

type alias Registered =
    { state : PlayerState
    , playerKey : String
    }
