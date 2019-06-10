package net.capellari.julien.threed.annotations.kotlinwriter

enum class Operators(val fname: String) {
    UNARY_PLUS("unaryPlus"), UNARY_MINUS("unaryMinus"), NOT("not"),
    INC("inc"), DEC("dec"),
    PLUS("plus"), MINUS("minus"), TIMES("times"), DIV("div"), REM("rem"), RANGE_TO("rangeTo"),
    CONTAINS("contains"),
    GET("get"), SET("set"),
    INVOKE("invoke"),
    PLUS_ASSIGN("plusAssign"), MINUS_ASSIGN("minusAssign"), TIMES_ASSIGN("timesAssign"), DIV_ASSIGN("divAssign"), REM_ASSIGN("remAssign"),
    COMPARE_TO("compareTo"),
    PROVIDE_DELEGATE("provideDelegate"), GET_VALUE("getValue"), SET_VALUE("setValue")
}