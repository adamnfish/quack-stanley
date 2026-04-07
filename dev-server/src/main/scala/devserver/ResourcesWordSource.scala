package devserver

import com.adamnfish.quackstanley.models.{Role, Word}
import com.adamnfish.quackstanley.persistence.WordSource

import scala.io.Source

class ResourcesWordSource extends WordSource {
  override val words: List[Word] =
    Source
      .fromResource("words.txt")
      .getLines()
      .filter(_.nonEmpty)
      .map(Word(_))
      .toList

  override val roles: List[Role] =
    Source
      .fromResource("roles.txt")
      .getLines()
      .filter(_.nonEmpty)
      .map(Role(_))
      .toList
}
