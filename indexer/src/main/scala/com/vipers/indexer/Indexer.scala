package com.vipers.indexer

private[indexer] trait Indexer {
  def isStale(lastIndexedOn : Long, staleAfter : Long) : Boolean = System.currentTimeMillis() - lastIndexedOn > staleAfter
}
