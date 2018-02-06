module Model exposing (Model, Player, Game, Lifecycle(..))


type Lifecycle
    = Welcome    -- initial screen
    | Create     -- create new game
    | Creating   -- waiting for API to confirm creation
    | Join       -- choose existing game to join
    | Joining    -- waiting for API to confirm game entry
    | Waiting    -- waiting for game to start (will have game and screen name)
    | Spectating -- game has started, player is spectating (and can choose words)
    | Pitching   -- player is pitching two cards from hand
    | ChooseRole -- player is given two roles to choose from
    | Buyer      -- player is the buyer, will have a role

type alias Game =
    { gameId : String
    , screenName : String
    , gameName : String
    , playerKey : String
    , players : List String
    }

type alias Player =
    { words : List String
    , discarded : List String
    , points : List String
    , role : Maybe String
    }

type alias Model =
    { lifecycle : Lifecycle
    , backendAwake : Bool
    , game : Maybe Game
    , player : Maybe Player
    }
