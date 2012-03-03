package models


trait UserService {

  /**
   * Creates a new user
   */
  def create(name: Name, email: Email, password: String): User


  /**
   * Finds an existing user
   */
  def findById(userId: UserId): Option[User]

  /**
   * Finds by email
   */
  def findByEmail(email: Email): Option[User]

  /**
   * Updates a user
   */
  def update(updatedUser: User): User

  /**
   * Removes a user
   */
  def remove(user: User): Boolean

}

