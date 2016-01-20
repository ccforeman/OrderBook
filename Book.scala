import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.math.BigDecimal
import Fields._

class Book(side: String, targetSize: Long) {
	val book = HashMap[String, BookData]()
	val transactionSide = side match { case "B" => "S"; case "S" => "B" }
	val target = targetSize

	var oldExpense = BigDecimal("0.0")
	var newExpense = BigDecimal("0.0")
	var newOutcome: String = "NA"
	var oldOutcome: String = "NA"

	var total: Long = 0
	var currentTime: Long = 0

	def newOrder(input: Array[String]) = { 
		BookData(input(Time).toLong,
				 input(Id),
			  	 input(Side),
				 BigDecimal(input(Price)),
				 input(Size).toLong
				)
	}
	
	def add(entry: BookData) = {
		book += (entry.id -> entry)
		total += entry.size
		currentTime = entry.time
		checkAndUpdate()
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
		checkAndUpdate()
	}

	def checkAndUpdate() = {
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

		val func = isBuy match {
			case true => (m: HashMap[String,BookData]) => m.maxBy(_._2.price)
			case false => (m: HashMap[String,BookData]) => m.minBy(_._2.price)
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
		val str = StringBuilder.newBuilder

		if(newOutcome == transactionSide) {
			str.append(currentTime).append(" ").append(transactionSide).append(" ").append(newExpense.toString)
			println(str)
		} else {
			str.append(currentTime).append(" ").append(transactionSide).append(" ").append("NA")
			println(str)
		}
	}

	def isBuy = side == "B"

}