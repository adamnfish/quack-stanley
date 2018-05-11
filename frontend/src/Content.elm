module Content exposing (example)

import Html exposing (Html)
import Markdown exposing (toHtml)
import Msg exposing (Msg)
import Views.Utils exposing (stripMargin)


example : Html Msg
example = toHtml [] ( stripMargin
    """|## Header
       |
       |paragraph
       |
       |* list 1
       |* list 2
       |"""
    )
