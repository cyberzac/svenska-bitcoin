package models

import org.jasypt.digest.PooledStringDigester

object Password {

  val hashAlgorithm = "SHA1"
  val iterations = 50000
  val digester = new PooledStringDigester();
  digester.setPoolSize(4); // This would be a good value for a 4-core system
  digester.setAlgorithm(hashAlgorithm);
  digester.setIterations(iterations);


  def apply(clear: String): Password = {
    val digest = digester.digest(clear)
    Password(PasswordDigest(digest))
  }


}

case class PasswordDigest(value:String)

case class Password(digest: PasswordDigest) {
  def matches(clear: String): Boolean = {
    Password.digester.matches(clear, digest.value)
  }

}
