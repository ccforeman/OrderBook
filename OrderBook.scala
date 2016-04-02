import scala.collection.mutable.TreeSet
import scala.collection.mutable.HashMap
import scala.math.BigDecimal
import Fields._

// Used as indexes for input
object Fields {
   val Time      = 0
   val Action    = 1
   val Id        = 2
   val Side      = 3
   val Price     = 4
   val Size      = 5

   // For removals only
   val RemAmount = 3
}

case class OrderBook (target: Long) {

   // Split OrderBook into 2 sides, for buys and sells
   var buy = new BookSide("B", target)
   var sell = new BookSide("S", target)

   def processInput(input: Array[String]){
      input(Action) match {
          case "A" => input(Side) match {
                  case "B" => buy.add(Order(input(Time).toLong, input(Id), input(Side), BigDecimal(input(Price)), input(Size).toLong))
                  case "S" => sell.add(Order(input(Time).toLong, input(Id), input(Side), BigDecimal(input(Price)), input(Size).toLong))
               }

            // Lookup in bookIndex because it's more efficient than looking up in book
            case "R" => buy.bookIndex.get(input(Id)) match {
                  case Some(buyOrder) => buy.remove(buyOrder, input(RemAmount).toLong, input(Time).toLong)
                  case None => sell.bookIndex.get(input(Id)) match {
                     case Some(sellOrder) => sell.remove(sellOrder, input(RemAmount).toLong, input(Time).toLong)
                     case None => System.err.println("Error: Entry not found for removal.")
                  }
               }
      }
   }
}

case class Order( var time: Long,
                  val id: String,
                  val side: String,
                  val price: BigDecimal,
                  var size: Long
               ) {
   
   // Determine what type of Order this is
   def isBuy = side == "B"
}

class BookSide(side: String, targetSize: Long) {
   
   val transactionSide = side match { case "B" => "S"; case "S" => "B" }
   val target = targetSize

   var oldExpense = BigDecimal("0.0")
   var newExpense = BigDecimal("0.0")
   var newOutcome: String = "NA"
   var oldOutcome: String = "NA"

   var totalShares: Long = 0
   var currentTime: Long = 0

   val sellOrdering = Ordering.by {o: Order => (o.price, o.id) }
   val buyOrdering = sellOrdering.reverse

   // Determine if OrderBook sorted order will be ascended or descending
   var book: TreeSet[Order] = _
   // Effectively constant time lookup to increase scalability; tradeoff for memory footprint
   var bookIndex = HashMap[String, Order]()
   
   // Determine if BookSide sorted order will be ascended or descending
   if(isBuy)
      book = TreeSet[Order]()(buyOrdering)
   else
      book = TreeSet[Order]()(sellOrdering)

   
   def add(entry: Order) = {
      book += entry
      bookIndex += (entry.id -> entry)
      totalShares += entry.size
      currentTime = entry.time
      update()
   }

   def remove(order: Order, amount: Long, newTime: Long) = {
      order.size -= amount
      order.time = newTime
      currentTime = newTime
      if(order.size <= 0) {
         book.remove(order)
         bookIndex.remove(order.id)
      }

      totalShares -= amount
      update()
   }

   // All flags need to be updated to determine if Pricer will print
   def update() = {
      if (totalShares >= target) {
         oldExpense = newExpense
         oldOutcome = newOutcome
         newOutcome = transactionSide
         newExpense = getExpense(target, book.toStream)
      } else {
         oldExpense = newExpense
         oldOutcome = newOutcome
         newOutcome = "NA"
         newExpense = BigDecimal("0.0")
      }

      if((newExpense != oldExpense) || (newOutcome != oldOutcome))
          printOutput
   }

   // Stream is used for the lazy evaluation of tail
   def getExpense(shares: Long, bookCopy: Stream[Order]): BigDecimal = {

      if (shares <= 0 || bookCopy.isEmpty)
         0
      else if((shares - bookCopy.head.size) > 0)
         (bookCopy.head.price * bookCopy.head.size) + getExpense(shares - bookCopy.head.size, bookCopy.tail)
      else
         (bookCopy.head.price * shares) + getExpense(shares - bookCopy.head.size, bookCopy.tail)

   }

   def printOutput = {
      val str = StringBuilder.newBuilder

      if(newOutcome == transactionSide) {
         str.append(currentTime).append(" ").append(transactionSide).append(" ").append(newExpense)
         println(str)
      } else {
         str.append(currentTime).append(" ").append(transactionSide).append(" ").append("NA")
         println(str)
      }
   }

   // Determine if the BookSide object is buy-side or sell-side
   def isBuy = side == "B"

}