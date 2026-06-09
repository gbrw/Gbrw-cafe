package com.example

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object BluetoothPrinterHelper {

    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    /**
     * Draws a high-fidelity vector Location Pin.
     */
    fun drawLocationPin(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        val originalColor = paint.color
        val originalStyle = paint.style
        val originalStrokeWidth = paint.strokeWidth

        paint.style = Paint.Style.FILL
        paint.color = Color.BLACK
        
        // Pin head circle
        canvas.drawCircle(x, y - size * 0.5f, size * 0.35f, paint)
        
        // Pin pointer triangle
        val path = android.graphics.Path().apply {
            moveTo(x - size * 0.3f, y - size * 0.4f)
            lineTo(x, y + size * 0.35f)
            lineTo(x + size * 0.3f, y - size * 0.4f)
            close()
        }
        canvas.drawPath(path, paint)
        
        // Inner negative space (white eye of pin)
        paint.color = Color.WHITE
        canvas.drawCircle(x, y - size * 0.5f, size * 0.12f, paint)
        
        paint.color = originalColor
        paint.style = originalStyle
        paint.strokeWidth = originalStrokeWidth
    }

    /**
     * Draws a high-fidelity Instagram square camera icon.
     */
    fun drawInstagramLogo(canvas: Canvas, x: Float, y: Float, size: Float, paint: Paint) {
        val originalColor = paint.color
        val originalStyle = paint.style
        val originalStrokeWidth = paint.strokeWidth

        paint.color = Color.BLACK
        
        // Outer rounded rectangle border
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.5f
        val rect = android.graphics.RectF(x - size * 0.4f, y - size * 0.4f, x + size * 0.4f, y + size * 0.4f)
        canvas.drawRoundRect(rect, size * 0.12f, size * 0.12f, paint)
        
        // Lens circle inside
        canvas.drawCircle(x, y, size * 0.17f, paint)
        
        // Small flash indicator dot
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x + size * 0.2f, y - size * 0.2f, size * 0.045f, paint)
        
        paint.color = originalColor
        paint.style = originalStyle
        paint.strokeWidth = originalStrokeWidth
    }

    /**
     * Checks if all necessary Bluetooth permissions are granted.
     */
    fun hasBluetoothPermissions(context: Context): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Required Bluetooth permissions list.
     */
    fun getRequiredPermissions(): Array<String> {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN)
        } else {
            arrayOf(Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN)
        }
    }

    /**
     * Returns the list of paired Bluetooth devices.
     */
    @SuppressLint("MissingPermission")
    fun getPairedDevices(context: Context): List<BluetoothDevice> {
        if (!hasBluetoothPermissions(context)) return emptyList()
        return try {
            val adapter = BluetoothAdapter.getDefaultAdapter()
            adapter?.bondedDevices?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Dynamically generates the 80mm Receipt Bitmap (576 pixels wide).
     */
    fun generate80mmReceiptBitmap(context: Context, items: List<CartItem>, receiptNo: String, dateStr: String, timeStr: String): Bitmap {
        val width = 576 // 80mm standard width (72mm actual printable = 576 pixels at 203 DPI)
        
        // Define paddings & spacing
        val itemHeight = 45
        val headerHeight = 250
        val metaHeight = 110
        val tableHeaderHeight = 50
        val summaryHeight = 120
        val footerHeight = 300
        
        val dynamicHeight = headerHeight + metaHeight + tableHeaderHeight + (items.size * itemHeight) + summaryHeight + footerHeight + 100
        
        val bitmap = Bitmap.createBitmap(width, dynamicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Fill canvas with white background
        canvas.drawColor(Color.WHITE)
        
        val paint = Paint().apply {
            isAntiAlias = true
            color = Color.BLACK
        }
        
        // Load Custom Arabic Tajawal Font if possible
        val tajawalBold = try {
            ResourcesCompat.getFont(context, R.font.tajawal_bold)
        } catch (e: Exception) {
            Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val tajawalRegular = try {
            ResourcesCompat.getFont(context, R.font.tajawal_regular)
        } catch (e: Exception) {
            Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }
        
        var currentY = 30f
        
        // 1. Draw Coffee Shop Logo
        try {
            val logoDrawable = ContextCompat.getDrawable(context, R.drawable.img_lesh_logo)
            if (logoDrawable != null) {
                val logoSize = 100
                val logoLeft = (width - logoSize) / 2
                logoDrawable.setBounds(logoLeft, currentY.toInt(), logoLeft + logoSize, (currentY + logoSize).toInt())
                logoDrawable.draw(canvas)
                currentY += logoSize + 20
            }
        } catch (e: Exception) {
            // Safe fallback if logo fails
            currentY += 10
        }
        
        // 2. Headings
        paint.typeface = tajawalBold
        paint.textSize = 34f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("ليش كافيه", width / 2f, currentY, paint)
        currentY += 40f
        
        paint.typeface = tajawalRegular
        paint.textSize = 18f
        canvas.drawText("أهلاً بك في ليش كافيه.. نورتنا بزيارتك 🤍", width / 2f, currentY, paint)
        currentY += 35f
        
        // Separator Line
        paint.strokeWidth = 2f
        canvas.drawLine(20f, currentY, width - 20f, currentY, paint)
        currentY += 30f
        
        // 3. Metadata details (Receipt No, Date, Time)
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 18f
        paint.typeface = tajawalBold
        canvas.drawText("رقم الفاتورة: $receiptNo", width - 20f, currentY, paint)
        currentY += 24f
        
        paint.typeface = tajawalRegular
        canvas.drawText("التاريخ: $dateStr", width - 20f, currentY, paint)
        currentY += 24f
        canvas.drawText("الوقت: $timeStr", width - 20f, currentY, paint)
        currentY += 30f
        
        // Separator
        canvas.drawLine(20f, currentY, width - 20f, currentY, paint)
        currentY += 30f
        
        // 4. Table Header (RTL Order: المنتج on right, then العدد, then السعر, then المجموع on left)
        paint.typeface = tajawalBold
        paint.textSize = 18f
        
        // Column coordinates (from left to right)
        // [Left/Total: 20] --- [Price: 150] --- [Qty: 300] --- [Name: 556]
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("المجموع", 20f, currentY, paint)
        
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("السعر", 180f, currentY, paint)
        canvas.drawText("العدد", 320f, currentY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("المنتج", width - 20f, currentY, paint)
        
        currentY += 20f
        canvas.drawLine(20f, currentY, width - 20f, currentY, paint)
        currentY += 30f
        
        // 5. Drawing Cart Items
        paint.typeface = tajawalRegular
        paint.textSize = 17f
        val formatter = NumberFormat.getNumberInstance(Locale.US)
        
        for (item in items) {
            val nameText = if (item.size.name != "عادي") {
                "${item.product.name} (${item.size.name})"
            } else {
                item.product.name
            }
            
            val itemPrice = item.size.price
            val itemSum = itemPrice * item.quantity
            
            // Draw Sum
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText("${formatter.format(itemSum)}", 20f, currentY, paint)
            
            // Draw Unit Price
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(formatter.format(itemPrice), 180f, currentY, paint)
            
            // Draw Quantity
            canvas.drawText(item.quantity.toString(), 320f, currentY, paint)
            
            // Draw Item Name with auto wrap or truncation if it exceeds boundaries
            paint.textAlign = Paint.Align.RIGHT
            var displayName = nameText
            if (paint.measureText(displayName) > 200f) {
                // Safeguard against extremely long names
                displayName = displayName.take(18) + ".."
            }
            canvas.drawText(displayName, width - 20f, currentY, paint)
            
            currentY += itemHeight
        }
        
        // Separator
        canvas.drawLine(20f, currentY, width - 20f, currentY, paint)
        currentY += 35f
        
        // 6. Summary Block
        val totalSum = items.sumOf { it.size.price * it.quantity }
        paint.typeface = tajawalBold
        paint.textSize = 22f
        
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("${formatter.format(totalSum)} د.ع", 20f, currentY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("المجموع الكلي:", width - 20f, currentY, paint)
        currentY += 45f
        
        // Separator
        paint.strokeWidth = 1f
        canvas.drawLine(20f, currentY, width - 20f, currentY, paint)
        currentY += 40f
        
        // 7. Footer Greeting
        paint.typeface = tajawalRegular
        paint.textSize = 16f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("بالعافية عليكم.. نسعد دائماً بزيارتكم اللطيفة 🥰", width / 2f, currentY, paint)
        currentY += 45f
        
        // Draw Location Address Info
        paint.style = Paint.Style.FILL
        paint.textSize = 15f
        paint.typeface = tajawalBold
        val addressText = "النجف الكوفة حي كندة قرب مركز كندة الصحي"
        val addressWidth = paint.measureText(addressText)
        val spaceBetweenAddress = 26f // icon size + gap info
        val startAddressX = (width - (addressWidth + spaceBetweenAddress)) / 2f
        
        drawLocationPin(canvas, startAddressX + 10f, currentY - 5f, 20f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(addressText, startAddressX + spaceBetweenAddress, currentY, paint)
        
        currentY += 38f
        
        // Draw Instagram Address Info
        val igText = "cafe__lesh"
        paint.style = Paint.Style.FILL
        val igWidth = paint.measureText(igText)
        val spaceBetweenIg = 26f
        val startIgX = (width - (igWidth + spaceBetweenIg)) / 2f
        
        drawInstagramLogo(canvas, startIgX + 10f, currentY - 5f, 20f, paint)
        
        paint.style = Paint.Style.FILL
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(igText, startIgX + spaceBetweenIg, currentY, paint)
        
        currentY += 50f
        
        // Divider line prior to developer rights
        paint.strokeWidth = 1f
        paint.style = Paint.Style.STROKE
        canvas.drawLine(100f, currentY, width - 100f, currentY, paint)
        currentY += 35f
        
        // Developers rights
        paint.typeface = tajawalRegular
        paint.style = Paint.Style.FILL
        paint.textSize = 13f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("برمجة غيث الراوي - انستغرام gb.rw", width / 2f, currentY, paint)
        
        return bitmap
    }

    /**
     * Compresses a small portion (slice) of ARGB Bitmap into standard 1-bit monochrome ESC/POS graphic raster commands.
     * Keeps chunk size compact (under 10KB) so small Bluetooth printer buffers never overflow.
     */
    fun convertSingleSliceToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8 // Standard is 72 bytes for 576 width
        
        val output = ByteArrayOutputStream()
        
        // Header for standard GS v 0 raster print:
        // GS v 0 m xL xH yL yH d1...dk
        output.write(0x1D)
        output.write(0x76)
        output.write(0x30)
        output.write(0x00) // Normal mode
        
        // widthBytes
        output.write(widthBytes and 0xFF)
        output.write((widthBytes shr 8) and 0xFF)
        
        // height
        output.write(height and 0xFF)
        output.write((height shr 8) and 0xFF)
        
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        for (y in 0 until height) {
            for (xByte in 0 until widthBytes) {
                var currentByte = 0
                for (bit in 0 until 8) {
                    val x = xByte * 8 + bit
                    if (x < width) {
                        val pixel = pixels[y * width + x]
                        val red = (pixel shr 16) and 0xFF
                        val green = (pixel shr 8) and 0xFF
                        val blue = pixel and 0xFF
                        val alpha = (pixel shr 24) and 0xFF
                        
                        // Slightly brighter threshold for crisp text output (160 instead of 140)
                        val luminance = (0.299 * red + 0.587 * green + 0.114 * blue)
                        if (alpha > 120 && luminance < 160) {
                            currentByte = currentByte or (1 shl (7 - bit))
                        }
                    }
                }
                output.write(currentByte)
            }
        }
        
        return output.toByteArray()
    }

    /**
     * Compresses the ARGB Bitmap into standard 1-bit monochrome ESC/POS graphic printable commands.
     */
    fun convertBitmapToEscPos(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val widthBytes = (width + 7) / 8
        
        val output = ByteArrayOutputStream()
        
        // Initialize Printer ESC @
        output.write(0x1B)
        output.write(0x40)
        
        // Centered formatting command
        output.write(0x1B)
        output.write(0x61)
        output.write(0x01) // Center
        
        val sliceBytes = convertSingleSliceToEscPos(bitmap)
        output.write(sliceBytes)
        
        // Feed 5 lines and cut
        output.write(0x1B)
        output.write(0x64)
        output.write(5) // Linefeed
        
        // GS V A 0: cut paper
        output.write(0x1D)
        output.write(0x56)
        output.write(0x41)
        output.write(0x00)
        
        return output.toByteArray()
    }

    /**
     * Connects to the Bluetooth printer, sends receipt bytes in horizontal slices, and closes the buffer.
     * Slicing the bitmap prevents budget printer memory buffers from overloading (which causes the red light of death).
     */
    @SuppressLint("MissingPermission")
    suspend fun printCustomReceipt(
        context: Context,
        device: BluetoothDevice,
        items: List<CartItem>,
        onStatus: (String) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        
        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null
        
        try {
            onStatus("جاري الاتصال بالطابعة...")
            
            // Standard UUID SPP (Serial Port Profile) for Bluetooth printers
            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            
            outputStream = socket.outputStream
            onStatus("جاري توليد الفاتورة المرئية...")
            
            // Generate metadata
            val sdfDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val randNum = (1000..9999).random()
            val receiptNo = "LSH-$randNum"
            val now = Date()
            
            val bitmap = generate80mmReceiptBitmap(
                context = context,
                items = items,
                receiptNo = receiptNo,
                dateStr = sdfDate.format(now),
                timeStr = sdfTime.format(now)
            )
            
            val width = bitmap.width
            val height = bitmap.height
            
            onStatus("جاري تجهيز وتقطيع المعاينة لتجنب امتلاء الذاكرة...")
            
            // Initialize printer session
            outputStream.write(0x1B)
            outputStream.write(0x40) // ESC @: Initialize printer
            outputStream.write(0x1B)
            outputStream.write(0x61)
            outputStream.write(0x01) // Center align
            outputStream.flush()
            
            // Slice height of 100-120 pixels is universally optimal for ultra-low buffers
            val sliceHeight = 110
            val numSlices = (height + sliceHeight - 1) / sliceHeight
            
            for (i in 0 until numSlices) {
                val startY = i * sliceHeight
                val currentSliceHeight = minOf(sliceHeight, height - startY)
                
                onStatus("جاري طباعة الجزء ${i + 1} من $numSlices...")
                
                // Crop the horizontal slice from original bitmap
                val sliceBitmap = Bitmap.createBitmap(bitmap, 0, startY, width, currentSliceHeight)
                val sliceBytes = convertSingleSliceToEscPos(sliceBitmap)
                
                outputStream.write(sliceBytes)
                outputStream.flush()
                
                sliceBitmap.recycle()
                
                // Vital sleep of 80ms to let the hardware buffer catch up and heat the head smoothly
                kotlinx.coroutines.delay(80)
            }
            
            // Send final padding and paper cutting command
            onStatus("جاري إنهاء وإخراج الورقة...")
            val footerStream = ByteArrayOutputStream()
            // Feed 5 lines for clean tear-off
            footerStream.write(0x1B)
            footerStream.write(0x64)
            footerStream.write(4)
            
            // Paper Cut Command
            footerStream.write(0x1D)
            footerStream.write(0x56)
            footerStream.write(0x41)
            footerStream.write(0x00)
            
            outputStream.write(footerStream.toByteArray())
            outputStream.flush()
            
            // Free the big bitmap resource
            bitmap.recycle()
            
            onStatus("تم عملية الطباعة بنجاح تام! 🎉")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            onStatus("حدث خطأ أثناء الطباعة: ${e.localizedMessage}\n(تأكد من شحن الطابعة وإغلاق غطاء الورق جيداً)")
            false
        } finally {
            try {
                outputStream?.close()
                socket?.close()
            } catch (e: Exception) {
                // Ignore socket closures
            }
        }
    }
}
