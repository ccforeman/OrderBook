import scala.collection.mutable.HashMap
import scala.util.control.Breaks._
import scala.math.BigDecimal

class Book(side: String, targetSize: Long) {
	val book = HashMap[String, BookData]()
	val isBuy = side match { case "B" => true; case _ => false }
	val transSide = side match { case "B" => "S"; case "S" => "B"}
	val target = targetSize

	var oldExpense = BigDecimal("0.0")
	var newExpense = BigDecimal("0.0")
	var newOutcome: String = "NA"
	var oldOutcome: String = "NA"

	var total: Long = 0
	var currentTime: Long = 0

	def newOrder(a: Array[String]) = new BookData(a(0).toLong, a(2), a(4), a(5).toLong, a(3)) 

	def add(entry: BookData) = {
		book += (entry.id -> entry)
		total += entry.shares
		currentTime = entry.time
		checkAndUpdate()
	}

	def remove(id: String, amount: Long, time: Long) = {
		book.get(id) match {
			case Some(o) => o.shares -= amount
							o.time = time
							currentTime = time
							if(o.shares <= 0)
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
			newOutcome = transSide
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
		val buf = isBuy match {
			case true => book.toSeq.sortBy(-_._2.price)
			case false => book.toSeq.sortBy(_._2.price)
		}
		breakable {
			buf.foreach{ order => 
				if((shares - order._2.shares) > 0) {
					curExpense += (order._2.shares * order._2.price)
					shares -= order._2.shares
				} else {
					curExpense += (shares * order._2.price)
					shares -= order._2.shares
				}
				if(shares <= 0)
					break
			}
		}
		curExpense
	}

	def printOutput = {
		if(newOutcome == transSide)
			println(currentTime + " " + transSide + " " + newExpense.toString) 

		else
			println(currentTime + " " + transSide + " " + "NA")
	}

}