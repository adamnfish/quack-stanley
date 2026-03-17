package com.adamnfish.quackstanley.persistence

import com.adamnfish.quackstanley.models.{Role, Word}

trait WordSource {
  def words: List[Word]
  def roles: List[Role]
}
