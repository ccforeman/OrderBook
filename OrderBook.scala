import scala.collection.mutable.HashMap
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
			case "R" => buy.book.get(input(Id)) match {
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
	lazy val book = HashMap[String, Order]()
	lazy val transactionSide = side match { case "B" => "S"; case "S" => "B" }
	lazy val target = targetSize

	var oldExpense = BigDecimal("0.0")
	var newExpense = BigDecimal("0.0")
	var newOutcome: String = "NA"
	var oldOutcome: String = "NA"

	var total: Long = 0
	var currentTime: Long = 0
	
	def add(entry: Order) = {
		book += (entry.id -> entry)
		total += entry.size
		currentTime = entry.time
		update()
	}

	def remove(id: String, amount: Long, newTime: Long) = {
		book.get(id) match {
			case Some(o) => o.size -= amount
							o.time = newTime
							currentTime = newTime
							if(o.size <= 0)
								book.remove(o.id)
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
			newExpense = getExpense()
		} else {
			oldOutcome = newOutcome
			newOutcome = "NA"
			newExpense = BigDecimal("0.0")
		}

		if((newExpense != oldExpense) || (newOutcome != oldOutcome))
			printOutput
	}

	def getExpense(): BigDecimal = {
		var shares = target
		var curExpense = BigDecimal("0.0")

		lazy val func = isBuy match {
			case true => (m: HashMap[String, Order]) => m.maxBy(_._2.price)
			case false => (m: HashMap[String, Order]) => m.minBy(_._2.price)
		}

		var mapCopy = book.clone()

		breakable {
			while(shares > 0) {
				var order = func(mapCopy)
				if((shares - order._2.size) > 0) {
					curExpense += (order._2.size * order._2.price)
					shares -= order._2.size
				} else {
					curExpense += (shares * order._2.price)
					shares -= order._2.size
				}
				mapCopy.remove(order._1)
				if(shares <= 0)
					break
			}
		}
		curExpense
	}

	def printOutput = {
		lazy val str = StringBuilder.newBuilder

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