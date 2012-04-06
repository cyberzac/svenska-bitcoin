package models

trait TransactionService {

  /**
   * Create and store a transaction
   * @param transaction
   */
  def create(transaction:Transaction)
  /**
   * Creates and stores two transactions from a trade.
   * One for the buyer and one for the seller
   */
  def create(trade: Trade[BTC, SEK])

  /**
   * Creates and stores a transaction for changes, add or withdraw of a SEK fund
   * @param userId
   * @param sek
   * @param bankAccount
   */
  def create(userId:UserId, sek:SEK, bankAccount:BankAccount)

  /**
   * Creates and stores a transaction for changes, add or withdraw of a BTC fund
   * @param userId
   * @param btc
   * @param bitcoinAddress
   */
  def create(userId:UserId, btc:BTC, bitcoinAddress:BitcoinAddress)

  /**
   * Creates and stores a transaction for the courtage
   * @param userId
   * @param btc
   * @param courtage
   */
  def create(userId:UserId, btc:BTC, courtage:Courtage)

  /**
   * Sums all transactions for a user
   */
  def sumByUser(userId: UserId): Balance

  /**
   * Lists a transactions for a user
   * @param userId
   */
  def findTransactions(userId:UserId):Seq[Transaction]

}

