module Utils exposing (..)


nonEmpty : String -> Bool
nonEmpty s =
    not <| String.isEmpty s


flip : (a -> b -> c) -> b -> a -> c
flip f b a =
    f a b


tuple2 : a -> b -> ( a, b )
tuple2 a b =
    ( a, b )


gameCodeFromId : String -> String
gameCodeFromId id =
    String.left 4 id


uncurry : (a -> b -> c) -> ( a, b ) -> c
uncurry fn ( a, b ) =
    fn a b


curry : (( a, b ) -> c) -> a -> b -> c
curry fn a b =
    fn ( a, b )
