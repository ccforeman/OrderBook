import scala.collection.mutable.TreeSet
import scala.util.control.Breaks._
import scala.math.BigDecimal
import Fields._

object Fields {
	val Time   = 0
	val Action = 1
	val Id     = 2
	val Side   = 3
	val Price  = 4
	val Size   = 5
}

case class OrderBook (target: Long) {
	var buy = new BookSide("B", target)
	var sell = new BookSide("S", target)

	def processInput(input: Array[String]){
		input(Action) match {
		    case "A" => input(Side) match {
						case "B" => buy.add(Order(input(Time).toLong, input(Id), input(Side), BigDecimal(input(Price)), input(Size).toLong))
						case "S" => sell.add(Order(input(Time).toLong, input(Id), input(Side), BigDecimal(input(Price)), input(Size).toLong))
					}
			case "R" => buy.book.find(order => order.id == input(Id)) match {
						case Some(b) => buy.remove(input(Id), input(Side).toLong, input(Time).toLong)
						case None => sell.remove(input(Id), input(Side).toLong, input(Time).toLong)
					}
		}
	}
}

case class Order(	var time: Long,
					val id: String,
					val side: String,
					val price: BigDecimal,
					var size: Long
					) {
	
	def isBuy = side == "B"
}

class BookSide(side: String, targetSize: Long) {
	
	val sellOrdering = Ordering.by {o: Order => (o.price, o.id) }
	val buyOrdering = sellOrdering.reverse

	var book: TreeSet[Order] = _
	
	if(isBuy)
		book = TreeSet[Order]()(buyOrdering)
	else
		book = TreeSet[Order]()(sellOrdering)


	val transactionSide = side match { case "B" => "S"; case "S" => "B" }
	val target = targetSize

	var oldExpense = BigDecimal("0.0")
	var newExpense = BigDecimal("0.0")
	var newOutcome: String = "NA"
	var oldOutcome: String = "NA"

	var total: Long = 0
	var currentTime: Long = 0
	
	def add(entry: Order) = {
		book += entry
		total += entry.size
		currentTime = entry.time
		update()
	}

	def remove(id: String, amount: Long, newTime: Long) = {
		book.find(order => order.id == id) match {
			case Some(o) => o.size -= amount
							o.time = newTime
							currentTime = newTime
							if(o.size <= 0)
								book.remove(o)
			case None => println("Error: No Removal. Order Not Found.")
		}

		total -= amount
		update()
	}

	def update() = {
		if (total >= target) {
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

	def isBuy = side == "B"

}