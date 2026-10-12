import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._

object HospitalRevenueAggregation {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Hospital Department Revenue Aggregation")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")
    import spark.implicits._

    // Hospital billing records
    val billingData = Seq(
      ("B101", "Cardiology", "Insurance", 25000.0),
      ("B102", "Cardiology", "Cash", 18000.0),
      ("B103", "Cardiology", "Insurance", 32000.0),
      ("B104", "Neurology", "Cash", 15000.0),
      ("B105", "Neurology", "Insurance", 28000.0),
      ("B106", "Neurology", "Cash", 22000.0),
      ("B107", "Orthopedics", "Insurance", 40000.0),
      ("B108", "Orthopedics", "Cash", 12000.0),
      ("B109", "Orthopedics", "Insurance", 35000.0),
      ("B110", "Pediatrics", "Cash", 8000.0),
      ("B111", "Pediatrics", "Insurance", 14000.0),
      ("B112", "Pediatrics", "Cash", 10000.0)
    )

    val billsDF = billingData.toDF(
      "bill_id",
      "department",
      "payment_type",
      "bill_amount"
    )

    println("\\n========== HOSPITAL BILLING DATA ==========")
    billsDF.show(false)

    // 1. Basic aggregation: count, sum, average, minimum and maximum
    println("\\n========== OVERALL REVENUE METRICS ==========")

    billsDF.agg(
      count("bill_id").as("total_bills"),
      sum("bill_amount").as("total_revenue"),
      avg("bill_amount").as("average_bill"),
      min("bill_amount").as("minimum_bill"),
      max("bill_amount").as("maximum_bill")
    ).show(false)

    // 2. Department-wise revenue statistics
    println("\\n========== DEPARTMENT-WISE REVENUE ==========")

    val departmentRevenue = billsDF
      .groupBy("department")
      .agg(
        count("bill_id").as("total_bills"),
        sum("bill_amount").as("total_revenue"),
        round(avg("bill_amount"), 2).as("average_bill"),
        min("bill_amount").as("minimum_bill"),
        max("bill_amount").as("maximum_bill")
      )
      .orderBy(desc("total_revenue"))

    departmentRevenue.show(false)

    // 3. GROUP BY multiple columns
    println("\\n========== DEPARTMENT AND PAYMENT TYPE ==========")

    billsDF
      .groupBy("department", "payment_type")
      .agg(
        count("*").as("bill_count"),
        sum("bill_amount").as("revenue")
      )
      .orderBy("department", "payment_type")
      .show(false)

    // 4. HAVING-like filtering after aggregation
    println("\\n========== DEPARTMENTS WITH REVENUE ABOVE 60000 ==========")

    departmentRevenue
      .filter(col("total_revenue") > 60000)
      .show(false)

    // 5. Department-wise employee salary statistics
    val employeeData = Seq(
      ("Harsha", "Cardiology", 65000.0),
      ("Ravi", "Cardiology", 72000.0),
      ("Priya", "Neurology", 68000.0),
      ("Anil", "Neurology", 75000.0),
      ("Sneha", "Orthopedics", 70000.0),
      ("Kiran", "Orthopedics", 80000.0),
      ("Meena", "Pediatrics", 45000.0),
      ("Arjun", "Pediatrics", 50000.0)
    )

    val employeesDF = employeeData.toDF(
      "employee_name",
      "department",
      "salary"
    )

    println("\\n========== DEPARTMENT-WISE SALARY STATISTICS ==========")

    employeesDF
      .groupBy("department")
      .agg(
        count("employee_name").as("employee_count"),
        sum("salary").as("total_salary"),
        round(avg("salary"), 2).as("average_salary"),
        min("salary").as("minimum_salary"),
        max("salary").as("maximum_salary")
      )
      .orderBy("department")
      .show(false)

    spark.stop()
  }
}
