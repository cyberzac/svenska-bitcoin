package models


import java.util.{Date}

import play.api.db._
import play.api.Play.current

import anorm._
import anorm.SqlParser._

class AnormTransactionService extends TransactionService {


  // -- Queries


  /**
   * Create a Transaction.
   */
  def create(trans: Transaction) {
    DB.withConnection { implicit connection =>

    // Get the trans id
      val id: Long =
        SQL("select next value for trans_id_seq").as(scalar[Long].single)


      SQL(
        """
          insert into trans values (
            {id}, {userId}, {sek}, {btc}, {note}, {transId}, {created}
          )
        """
      ).on(
        'id -> id,
        'userId -> trans.userId.value,
        'sek -> (trans.sek * 1000).value.toLong,
        'btc -> (trans.btc * 1000*1000*100).value.toLong,
        'transId -> trans.id,
        'created -> trans.date,
        'note -> trans.note
      ).executeUpdate()

      trans.copy(id = Id(id))

    }
  }

  def create(trade: Trade[BTC, SEK]) = null

  def create(userId: UserId, sek: SEK, bankAccount: BankAccount) = null

  def create(userId: UserId, btc: BTC, bitcoinAddress: BitcoinAddress) = null

  def create(userId: UserId, btc: BTC, courtage: Courtage) = null

  def sumByUser(userId: UserId):Balance = Balance()

  def findTransactions(userId: UserId):Seq[Transaction] = {
    DB.withConnection { implicit connection =>
      SQL("select * from trans where user_id = {userId}").on(
        'userId -> userId.value
      ).as(Transaction.simple *)
    }
  }

  /**
   * Retrieve a Transaction from the id.
   */
  def findById(id: Long): Option[Transaction] = {
    DB.withConnection { implicit connection =>
      SQL("select * from trans where id = {id}").on(
        'id -> id
      ).as(Transaction.simple.singleOpt)
    }
  }

  /**
   * Delete a trans
   */
  def delete(id: Long) {
    DB.withConnection { implicit connection =>
      SQL("delete from trans where id = {id}").on(
        'id -> id
      ).executeUpdate()
    }
  }


}
