package com.rvt.storagedemo.customview

import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEach
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import com.rvt.storagedemo.R

class ScoreCardView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val whiteColor = Color.argb(255, 255, 255, 255)
    private val crossColor = Color.argb(255, 31, 42, 49)
    private var cornerRadius = 8f

    private var parData: List<String> = listOf()
    private var dataSource: List<List<String>> = listOf(listOf())
    var layoutWidth = 0
    var layoutHeight = 0
    private var cellHeight = 0
    private var cellPadding = 0
    private var cellData = mutableListOf<ViewGroup>()
    private var cellWidthData = mutableListOf<Int>()

    private var listener: ScoreCardListener? = null

    private var currentSelectedRowIndex = -1
    private var currentSelectedColIndex = -1
    private var rowWithoutHighlights = listOf<Int>()

    private var drawingFrame = RectF(0f, 0f, 0f, 0f)

    private var currentHole = -1

    private var enableScoreCard = true

    init {
        setWillNotDraw(false)
    }

    fun setRowWithoutHighlights(rows: List<Int>) {
        this.rowWithoutHighlights = rows
    }

    fun setOnScoreCardListener(listener: ScoreCardListener) {
        this.listener = listener
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        listener = null
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutWidth = w
        layoutHeight = h
        if (layoutWidth > 0 && layoutHeight > 0) {
            generateCells()
        }
    }

    private fun calculateCellSize() {
        if (layoutWidth > 0 && layoutHeight > 0) {
            cellHeight = (layoutHeight - (cellPadding * (dataSource.size + 1))) / dataSource.size
            if (dataSource.isNotEmpty() && dataSource[0].isNotEmpty()) {
                if (cellWidthData.size != dataSource[0].size) {
                    cellWidthData.clear()
                    val widthWoPadding =
                        (layoutWidth - (cellPadding * (dataSource[0].size))) / (dataSource[0].size + 1)
                    dataSource[0].forEachIndexed { index, _ ->
                        if (index == 0) {
                            //cellWidthData.add(widthWoPadding * 2)
                            cellWidthData.add((widthWoPadding * 1.9).toInt())
                        } else {
                            cellWidthData.add(widthWoPadding + cellPadding)
                        }
                    }
                }
            }
        }
    }

    fun setCurrentHoleToHighLight(currentHole: Int) {
        this.currentHole = currentHole
        generateCells()
    }

    fun setDataSource(dataSource: List<List<String>>) {
        if (this.dataSource.size == dataSource.size &&
            this.dataSource.isNotEmpty() &&
            this.dataSource[0].size == dataSource[0].size
        ) {
            // Do nothing
        } else {
            removeAllViews()
            cellData.clear()
        }
        this.dataSource = dataSource
        generateCells()
    }

    fun setParData(parData: List<String>) {
        this.parData = parData
        generateCells()
    }

    fun enableScoreCard(enableScoreCard: Boolean) {
        if (this.enableScoreCard != enableScoreCard) {
            this.enableScoreCard = enableScoreCard
            generateCells()
        }
    }

    fun setSelectionIndex(rowIndex: Int, colIndex: Int) {
        currentSelectedColIndex = colIndex
        currentSelectedRowIndex = rowIndex
        drawSelection()
    }

    private fun drawSelection() {
        if (currentSelectedColIndex != -1 && currentSelectedRowIndex != -1 && cellWidthData.size > 0) {
            val index = currentSelectedColIndex + (currentSelectedRowIndex * dataSource[0].size)
            val view = cellData[index]
            val textView = findTextView(view)
            drawingFrame = if (view.marginStart > 0 && textView.text == "--") {
                RectF(
                    view.marginStart.toFloat() - cellPadding,
                    view.marginTop.toFloat() - cellPadding,
                    view.marginStart.toFloat() + cellWidthData[currentSelectedColIndex] + cellPadding,
                    view.marginTop.toFloat() + cellHeight + cellPadding
                )
            } else {
                RectF(0f, 0f, 0f, 0f)
            }
            invalidate()
        }
    }

    private fun generateCells() {
        calculateCellSize()
        if (cellData.size == 0) {
            dataSource.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, col ->
                    val cardView = CardView(context)
                    val linearLayout = LinearLayout(context)
                    val textView = TextView(context)
                    val imageView = ImageView(context)
                    textView.setTypeface(null, Typeface.BOLD)
                    if (colIndex == 0) {

                        //for color view
                        val view = View(context)
                        view.layoutParams = LayoutParams(10,ViewGroup.LayoutParams.MATCH_PARENT)
                        view.setBackgroundColor(ContextCompat.getColor(context,R.color.teal_200))

                        //for space
                        val spaceView = View(context)
                        spaceView.layoutParams = LayoutParams(30,ViewGroup.LayoutParams.MATCH_PARENT)

                        //edit player name
                        val layoutParams = LayoutParams(30,30)
                        layoutParams.gravity = Gravity.END or Gravity.CENTER
                        layoutParams.marginEnd = 15
                        imageView.layoutParams = layoutParams
                        imageView.setImageDrawable(ContextCompat.getDrawable(context,R.drawable.ic_pencil))
                        imageView.setOnClickListener {
                            if (enableScoreCard) {
                                listener?.onEditPlayer(id, rowIndex, colIndex)
                            }
                        }

                        linearLayout.gravity = Gravity.CENTER or Gravity.START
                        linearLayout.addView(view)
                        linearLayout.addView(spaceView)

                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22.0f)
                        textView.gravity = Gravity.CENTER or Gravity.START
                        val textLayoutParam = LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1.0f)
                        textView.layoutParams = textLayoutParam

                        cardView.radius = 0f
                    } else {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28.0f)
                        textView.gravity = Gravity.CENTER
                        val textLayoutParam = LinearLayout.LayoutParams(100,100)
                        textView.layoutParams = textLayoutParam

                        linearLayout.gravity = Gravity.CENTER

                        cardView.radius = 0f
                    }
                    val colors = getCellColors(rowIndex, colIndex)
                    textView.setTextColor(colors.first)
                    if (parData.isNotEmpty() && parData.size == row.size) {
                        parData[colIndex].toIntOrNull()?.let { parIntValue ->
                            col.toIntOrNull()?.let { scoreValue ->
                                when {
                                    !enableScoreCard && textView.text == "--" -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    scoreValue > parIntValue -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    scoreValue < parIntValue -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    else -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                }
                            }
                        }
                    }

                    if(col.isEmpty()){
                        textView.setBackgroundColor(ContextCompat.getColor(context,R.color.teal_200))
                    }else{
                        textView.setBackgroundColor(Color.TRANSPARENT)
                    }
                    textView.setBackgroundColor(ContextCompat.getColor(context,R.color.teal_200))
                    textView.text = col
                    textView.invisible()
                    addView(cardView)
                    textView.setOnClickListener {
                        if (enableScoreCard) {
                            listener?.onCellClicked(id, rowIndex, colIndex)
                        }
                    }

                    linearLayout.addView(textView)
                    linearLayout.addView(imageView)

                    cardView.apply {
                        cardElevation = 0f
                        addView(linearLayout)
                    }
                    cellData.add(cardView)
                }
            }
        } else {
            dataSource.forEachIndexed { rowIndex, row ->
                row.forEachIndexed { colIndex, col ->
                    val index = colIndex + (rowIndex * dataSource[0].size)
                    val view = cellData[index]
                    val textView = findTextView(view)
                    val colors = getCellColors(rowIndex, colIndex)
                    textView.setTextColor(colors.first)
                    if (parData.isNotEmpty() && parData.size == row.size) {
                        parData[colIndex].toIntOrNull()?.let { parIntValue ->
                            col.toIntOrNull()?.let { scoreValue ->
                                when {
                                    !enableScoreCard && textView.text == "--" -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    scoreValue > parIntValue -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    scoreValue < parIntValue -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                    else -> {
                                        textView.setTextColor(context.getColor(R.color.white))
                                    }
                                }
                            }
                        }
                    }
                    findCardView(view).setCardBackgroundColor(colors.second)
                    textView.text = col
                }
            }
        }
        Handler(Looper.getMainLooper()).post {
            recalculateViewPosition()
            drawSelection()
        }
    }

    private fun getCellColors(rowIndex: Int, colIndex: Int): Pair<Int, Int> {
        var backgroundRes = R.color.black
        var foregroundRes = R.color.white
        Log.d("TAG", "getCellColors: current hole $currentHole, index: $colIndex, rowindex: $rowIndex")
        rowWithoutHighlights.firstOrNull { row ->
            rowIndex == row
        }?.let {
            // Do nothing
        } ?: run {
            if (rowIndex == 0 &&
                (currentHole <= 9 && currentHole == colIndex) ||
                currentHole >= 10 && currentHole - 9 == colIndex
            ) {
                backgroundRes = R.color.design_default_color_primary
                foregroundRes = R.color.black
            }
        }
        return Pair(resources.getColor(foregroundRes, null), resources.getColor(backgroundRes, null))
    }

    private fun recalculateViewPosition() {
        cellData.forEachIndexed { index, viewGroup ->
            updateLayoutParams(viewGroup, index / dataSource[0].size, index % dataSource[0].size)
        }
    }

    private fun updateLayoutParams(view: ViewGroup, rowIndex: Int, colIndex: Int) {
        if (cellWidthData.size == 0) {
            Log.e("TAG", "updateLayoutParams: Invalid content for cellWidthData; not continuing")
            return
        }

        val cellWidth = cellWidthData[colIndex]
        val newLayoutParams = LayoutParams(cellWidth, cellHeight)
        val top = (rowIndex * cellHeight) + ((rowIndex + 1) * cellPadding)
        val padding = ((colIndex + 1) * cellPadding)

        val left: Int = when (colIndex) {
            0 -> /*padding*/ 10
            1 -> /*padding +*/ cellWidth + (colIndex * cellWidth)
            10 -> /*padding*/10 + cellWidth + (colIndex * cellWidth)
            else -> cellWidth + (colIndex * cellWidth)
        }

        newLayoutParams.marginStart = left
        newLayoutParams.topMargin = top

        findCardView(view).layoutParams = newLayoutParams
        findCardView(view).visible()
    }

    interface ScoreCardListener {
        fun onCellClicked(viewId: Int, rowIndex: Int, colIndex: Int)
        fun onEditPlayer(viewId: Int, rowIndex: Int, colIndex: Int)
    }

    override fun dispatchDraw(canvas: Canvas?) {
        super.dispatchDraw(canvas)
    }



    private fun findTextView(viewGroup : ViewGroup) : TextView{
        return (viewGroup.getChildAt(0) as LinearLayout).children.filter { it is TextView }.first() as TextView
    }

    private fun findLinearLayout(viewGroup : ViewGroup) : LinearLayout{
        return (viewGroup.getChildAt(0) as LinearLayout)
    }

    private fun findCardView(viewGroup : ViewGroup) : CardView{
        return viewGroup as CardView
    }

    //cardView
    //LinearLayout
}