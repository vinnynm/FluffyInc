package com.enigma.fluffyinc.apps.finance.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.enigma.fluffyinc.apps.finance.data.Expense
import com.enigma.fluffyinc.apps.finance.data.Income
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.opencsv.CSVWriter
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

object FinanceReportUtils {

    fun exportExpensesToCsv(context: Context, expenses: List<Expense>): Uri? {
        val fileName = "Expenses_Report_${System.currentTimeMillis()}.csv"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = CSVWriter(FileWriter(file))
            val header = arrayOf("Date", "Description", "Category", "Source", "Amount")
            writer.writeNext(header)
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currencyFormat = NumberFormat.getCurrencyInstance()
            
            expenses.forEach { expense ->
                writer.writeNext(arrayOf(
                    sdf.format(Date(expense.date)),
                    expense.description,
                    expense.category,
                    expense.source,
                    currencyFormat.format(expense.amount)
                ))
            }
            writer.close()
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportExpensesToPdf(context: Context, expenses: List<Expense>): Uri? {
        val fileName = "Expenses_Report_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)
        
        try {
            val writer = PdfWriter(file)
            val pdf = PdfDocument(writer)
            val document = Document(pdf)
            
            document.add(Paragraph("Expense Report").setFontSize(20f).setBold())
            document.add(Paragraph("Generated on: ${SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())}"))
            document.add(Paragraph("\n"))
            
            val table = Table(UnitValue.createPercentArray(floatArrayOf(20f, 30f, 20f, 15f, 15f))).useAllAvailableWidth()
            table.addHeaderCell("Date")
            table.addHeaderCell("Description")
            table.addHeaderCell("Category")
            table.addHeaderCell("Source")
            table.addHeaderCell("Amount")
            
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currencyFormat = NumberFormat.getCurrencyInstance()
            var totalAmount = 0.0
            
            expenses.forEach { expense ->
                table.addCell(sdf.format(Date(expense.date)))
                table.addCell(expense.description)
                table.addCell(expense.category)
                table.addCell(expense.source)
                table.addCell(currencyFormat.format(expense.amount))
                totalAmount += expense.amount
            }
            
            document.add(table)
            document.add(Paragraph("\nTotal Expenses: ${currencyFormat.format(totalAmount)}").setBold())
            
            document.close()
            return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun shareFile(context: Context, uri: Uri, mimeType: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
