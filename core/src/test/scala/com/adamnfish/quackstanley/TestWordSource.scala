package com.adamnfish.quackstanley

import com.adamnfish.quackstanley.models.{Role, Word}
import com.adamnfish.quackstanley.persistence.WordSource


class TestWordSource extends WordSource {
  override def words: List[Word] = TestWordSource.words
  override def roles: List[Role] = TestWordSource.roles
}

object TestWordSource {
  val words: List[Word] = List(
    "alpha", "bravo", "charlie", "delta", "echo",
    "foxtrot", "golf", "hotel", "india", "juliet",
    "kilo", "lima", "mike", "november", "oscar",
    "papa", "quebec", "romeo", "sierra", "tango"
  ).map(Word(_))

  val roles: List[Role] = List(
    "role-one", "role-two", "role-three", "role-four", "role-five",
    "role-six", "role-seven", "role-eight", "role-nine", "role-ten"
  ).map(Role(_))
}
