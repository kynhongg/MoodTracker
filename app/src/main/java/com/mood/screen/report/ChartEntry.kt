package com.mood.screen.report

enum class ChartEntry(val beanType: Int, val yValue: Float) {
    Value0(8, 0f),
    Value1(7, 5f),
    Value2(3, 10f),
    Value3(2, 15f),
    Value4(4, 20f),
    Value5(6, 25f),
    Value6(1, 30f),
    Value7(5, 35f);

    companion object {
        fun getYValueWithBeanType(beanType: Int) =
            values().find { it.beanType == beanType }?.yValue ?: Value6.yValue
    }
}